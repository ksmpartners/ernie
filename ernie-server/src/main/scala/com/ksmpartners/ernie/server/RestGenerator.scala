/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ksmpartners.ernie.server

import net.liftweb.common.Box
import net.liftweb.http._
import org.slf4j.{ LoggerFactory, Logger }
import net.liftweb.http.rest.RestHelper
import RestGenerator._
import net.liftweb.http.InternalServerErrorResponse
import com.ksmpartners.ernie.server.RestGenerator.Filter
import net.liftweb.http.ResponseWithReason
import net.liftweb.http.InMemoryResponse
import net.liftweb.common.Full
import net.liftweb.http.MethodNotAllowedResponse
import com.ksmpartners.ernie.server.RestGenerator.Variable
import com.ksmpartners.ernie.api.ReportOutputException
import com.ksmpartners.ernie.server.RestGenerator.Resource
import com.ksmpartners.ernie.server.RestGenerator.Package
import net.liftweb.http.auth.{ Role, AuthRole }

/**
 * Companion singleton for RestGenerator trait.
 */
object RestGenerator {
  type restFunc = (() => Box[LiftResponse])
  type restFilter = (restFunc => () => Box[LiftResponse])

  val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.server.RestGenerator")

  /**
   * Represent a parameter to a REST operation. For more information, see the Swagger specification: https://github.com/wordnik/swagger-core/wiki/Parameters
   * @param param name of parameter
   * @param paramType type of parameter. Conforms to Swagger specification. It can be only one of the following: path, query, body, header or form.
   * @param dataType for path, query, and header paramTypes, this field must be a primitive. For body, this can be a complex or container datatype.
   */
  case class Parameter(param: String, paramType: String, dataType: String, defaultValue: String*)

  /**
   * Represent a filter that pre-processes a request. See [[com.ksmpartners.ernie.server.ErnieFilters]]
   */
  case class Filter(name: String, filter: (Req => restFunc => restFunc), param: Option[Parameter], error: ErnieError, values: String*)

  /**
   * Represent a URL parameter for a request.
   */
  case class Variable(data: Any)

  /**
   * Metadata for the result type of an ernie operation.
   * @param accept the HTTP Accept value required for this product
   * @param suffix the suffix of the URL for this product. (e.g. for /resources.json, suffix=json)
   */
  case class Product(accept: String, suffix: String) {
    override def toString: String = accept
  }

  /**
   * Represent an incoming request and a set of parameters to pass from a request handler to an [[com.ksmpartners.ernie.server.RestGenerator.Action]] function
   */
  case class Package(req: Req, params: Variable*)

  /**
   * Represent an error response
   */
  case class ErnieError(resp: LiftResponse, exception: Option[Exception]) {
    /**
     * Attempt to convert an Exception to a descriptive LiftResponse
     * @param reason optionally specify the reason for the exception. Otherwise, use the message on the Exception
     */
    def toResponse(reason: Option[String]): ResponseWithReason =
      if (resp.isInstanceOf[ResponseWithReason]) resp.asInstanceOf[ResponseWithReason]
      else ResponseWithReason(resp, if (reason.isEmpty) { exception.map(e => e.getMessage) getOrElse "" } else reason.get)

    /**
     * Return the boxed response and log
     */
    def send(reason: Option[String]): Box[LiftResponse] = {
      log.info("Response: " + toResponse(reason).response.getClass + ". Reason: {}", toResponse(reason).reason)
      Full(toResponse(reason))
    }

    /**
     * Return the boxed response and log
     */
    def send(): Box[LiftResponse] = send(None)
  }

  /**
   * Represent an action that can be performed by an ernie dependency
   * @param name the plain-text name of the action
   * @param func the anonymous function to invoke that performs the action
   * @param responseClass either void, a primitive, a complex or a container return value.
   *                      Any datatype which is not a primitive must be defined in the models section of the Swagger API Declaration.
   *                      Generally, this will be a class name from the [[com.ksmpartners.ernie.model]] package
   * @param errors that may result from this action
   */
  case class Action(name: String, func: (Package) => Box[LiftResponse], summary: String, notes: String, responseClass: String, errors: ErnieError*)

  /**
   * Safely invoke an action. If no exception is thrown, invoke a post-processing function on the result. Otherwise, convert the exception to a response.
   * @param call an anonymous function that performs an unsafe operation
   * @param then a post-processing function to be executed on successful invocation of call
   * @tparam B the return value of call
   */
  def apiCall[B](a: Action, call: Any => B, then: B => Box[LiftResponse]): Box[LiftResponse] = try {
    val res = call.apply()
    then(res)
  } catch {
    case e: Exception => checkResponse(a, Some(e))
  }

  /**
   * Attempt to find an ErnieError representation in a of e. If found, convert the ErnieError to a boxed LiftResponse
   */
  def checkResponse(a: Action, e: Option[Exception]): Box[LiftResponse] = if (e.isDefined) {
    val errors = a.errors
    var result: List[ResponseWithReason] = Nil
    errors.foreach(f => {
      if ((f.exception.isDefined) && (e.get.getClass == f.exception.get.getClass)) {
        if (e.get.isInstanceOf[ReportOutputException]) {
          if (e.get.asInstanceOf[ReportOutputException].status.toList.contains(f.exception.get.asInstanceOf[ReportOutputException].status.getOrElse(null)))
            result.::=(f.toResponse(e.map(f => f.getMessage)))
        } else result.::=(f.toResponse(e.map(f => f.getMessage)))
      }
    })
    if (result.isEmpty) {
      log.debug("Response: Internal Server Error. Reason: {}", e.get.getMessage)
      Full(ResponseWithReason(InternalServerErrorResponse(), e.get.getMessage))
    } else {
      log.debug("Response: " + result.head.response.getClass.getSimpleName + ", reason: {}", result.head.reason)
      Full(result.head)
    }
  } else net.liftweb.common.Empty

  /**
   * Represent an HTTP operation supported by some [[com.ksmpartners.ernie.server.RestGenerator.Resource]]
   * @param requestType the HTTP method
   * @param produces optionally specify a list of [[com.ksmpartners.ernie.server.RestGenerator.Product]] to describe the result of this operation
   * @param filters a list of [[com.ksmpartners.ernie.server.RestGenerator.Filter]]s to pre-process a request
   * @param action the [[com.ksmpartners.ernie.server.RestGenerator.Action]] to invoke for this operation
   */
  case class RequestTemplate(requestType: RequestType, produces: List[Product], filters: List[Filter], action: Action, params: Parameter*)

  /**
   * Represent a resource to serve. Note that each Resource corresponds to a member of the "apis" array in a <a href="https://github.com/wordnik/swagger-core/wiki/API-Declaration">Swagger API declaration</a>
   * @param path either a variable in the URL or a static name
   *             (e.g. for the resource representing a specific job in the jobs collection, the URL would be /jobs/{jobId}; for example, /jobs/5.
   *              To represent that resource, use Right.
   *             For the resource representing the jobs collection, the URL would be /jobs
   *              To represent that resource, use Left.
   * @param requestTemplates a list of supported operations
   * @param children optionally specify child resources; for instance, /countries/usa would be represented:
   *                 {{{
   *                  Resource(Left("countries"), "Countries resource", true, Nil, Resource(Left("usa"), "United States of America", false, Nil))
   *                 }}}
   */
  case class Resource(path: Either[String, Variable], description: String, isResourceGroup: Boolean, requestTemplates: List[RequestTemplate], children: Resource*) {
    def swaggerPath = "/" + {
      if (path.isLeft) path.left.get
      else "{" + path.right.get.data + "}"
    }
  }

  /**
   * Convert a GET operation to a HEAD operation
   */
  def getToHead(r: RequestTemplate): RequestTemplate = RequestTemplate(HeadRequest, r.produces, r.filters, getToHead(r.action))
  /**
   * Convert a GET operation to a HEAD operation
   */
  def getToHead(a: Action): Action = Action(a.name + "Head", headFilter(a.func), a.summary, a.notes, "void", a.errors: _*)

  private def headFilter(f: (Package) => Box[LiftResponse]): (Package) => Box[LiftResponse] = { pck: Package =>
    {
      val respBox = f(pck)
      val resp: LiftResponse = respBox.open_!
      val response = InMemoryResponse(Array(), resp.toResponse.headers, resp.toResponse.cookies, resp.toResponse.code)
      Full(response)
    }
  }
}

/**
 * Provides the logic for serving an abstract list of [[com.ksmpartners.ernie.server.RestGenerator.Resource]]s
 */
trait RestGenerator extends RestHelper {
  private def baseFilter(r: Req)(f: restFunc): restFunc = f

  private def baseAction() = Full(NotFoundResponse())

  protected val api: List[Resource]
  private var tree: List[List[Resource]] = Nil
  def getTree = tree
  def setTree(t: List[List[Resource]]) { tree = t }

  private def traverse(r: Resource, path: List[Resource]) {
    if (r == null) tree = tree
    else if (r.children.isEmpty)
      tree = tree.::((path.::(r)).reverse)
    else {
      r.children.foreach(f => {
        traverse(f, path.::(r))
      })
      tree = tree.::((path.::(r)).reverse)
    }
  }

  private def foldFilters(req: Req, filterClasses: List[Filter]): restFilter =
    filterClasses.map(f => f.filter).foldLeft[restFilter](baseFilter(req) _)((all: restFilter, one: (Req => restFilter)) => all andThen one(req))

  private var protect: List[net.liftweb.http.LiftRules.HttpAuthProtectedResourcePF] = Nil

  /**
   * Return a list of PartialFunctions that can be added to [[net.liftweb.http.LiftRules.httpAuthProtectedResource]].
   * A resource will be protected for a given operation if its RequestTemplate has one of the authorization Filters in [[com.ksmpartners.ernie.server.ErnieFilters.authFilters]]
   */
  def protectedResources: net.liftweb.http.LiftRules.HttpAuthProtectedResourcePF = {
    serveApi
    protect.foldLeft(PartialFunction.empty[Req, Box[net.liftweb.http.auth.Role]]) { (f: PartialFunction[Req, Box[net.liftweb.http.auth.Role]], d: LiftRules.HttpAuthProtectedResourcePF) =>
      f orElse d
    }
  }

  /**
   * Serve the resources in API. This method must be called before adding the class extending this trait to [[net.liftweb.http.LiftRules.statelessDispatch]].
   */
  def serveApi() {
    api.map(res => traverse(res, Nil))
    tree.foreach(path => if (path.length > 0) {
      val leaf = path(path.length - 1)
      val Path = path.map(f => if (f.path.isLeft) f.path.left.get else "")
      if (Path.contains("")) {
        val Path1 = Path.slice(0, Path.indexOf(""))
        val Path2 = Path.slice(Path.indexOf("") + 1, Path.length)
        leaf.requestTemplates.foreach(requestTemplate => {
          if (leaf isResourceGroup) serve(Path.slice(0, Path.indexOf("")) prefix {
            genPf(Path2, requestTemplate, true)
          })
          serve(Path.slice(0, Path.indexOf("")) prefix {
            genPf(Path2, requestTemplate, true)
          })
          ErnieFilters.authFilters.foreach(f => if (requestTemplate.filters.contains(f)) protect.::=({
            genPf(f, Path1 ::: List("") ::: Path2, requestTemplate, true)
          }))
        })
        serve(Path.slice(0, Path.indexOf("")) prefix {
          case req @ Req(variable :: Path2, _, _) => {
            Full(MethodNotAllowedResponse())
          }
        })
      } else {
        leaf.requestTemplates.foreach(requestTemplate => {

          if (leaf isResourceGroup) serve(Path prefix genPf(Path, requestTemplate))
          serve(genPf(Path, requestTemplate))

          ErnieFilters.authFilters.foreach(f => if (requestTemplate.filters.contains(f)) {
            protect.::=({
              genPf(f, Path, requestTemplate, false)
            })
          })
        })
        serve {
          case req @ Req(Path, suffix, _) => {
            Full(MethodNotAllowedResponse())
          }
        }
      }
    })
  }

  private def genPf(f: Filter, path: List[String], requestTemplate: RequestTemplate, variable: Boolean) = new PartialFunction[Req, Box[Role]] {

    val produces = requestTemplate.produces
    val rT = requestTemplate.requestType

    def apply(v1: Req): Box[Role] = {
      if (f.values.size > 1)
        Full(AuthRole(f.values.foldLeft("")((s: String, d: String) => if (s == "") d else s + "," + d), f.values.map(n => new net.liftweb.http.auth.Role {
          def name = n
        }).toList: _*))
      else if (f.values.size == 1) Full(net.liftweb.http.auth.AuthRole(f.values(0)))
      else net.liftweb.common.Empty
    }

    def isDefinedAt(x: Req): Boolean = {

      val pathBool = if (variable) {
        (x.path.partPath.containsSlice(path.slice(0, path.indexOf("")))) && (x.path.partPath.containsSlice(path.slice(path.indexOf("") + 1, path.size)))
      } else x.path.partPath == path

      (pathBool) && {
        if (produces.isEmpty) true else {
          if (produces.find(f => f.suffix == x.path.suffix).isDefined) true
          else false
        }
      } && (rT == x.requestType)
    }
  }

  private def genPf(path: List[String], requestTemplate: RequestTemplate, variable: Boolean = false) = new PartialFunction[Req, () => Box[LiftResponse]] {

    val produces = requestTemplate.produces
    val rT = requestTemplate.requestType

    def apply(v1: Req): () => Box[LiftResponse] = {
      val pack = if (variable) {
        Package(v1, Variable(v1.path.partPath(0)))
      } else Package(v1)
      foldFilters(v1, requestTemplate.filters) apply (() => requestTemplate.action.func(pack))
    }

    def isDefinedAt(x: Req): Boolean = {

      val pathBool = if (variable) {
        (x.path.partPath.slice(1, x.path.partPath.size) == (path))
      } else x.path.partPath == path

      (pathBool) && {
        if (produces.isEmpty) true else {
          if (produces.find(f => f.suffix == x.path.suffix).isDefined) true
          else false
        }
      } && (rT == x.requestType)
    }
  }

}