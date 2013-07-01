/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.server

import net.liftweb.common.{ Full, Box }
import net.liftweb.http._
import org.slf4j.{ LoggerFactory, Logger }
import com.ksmpartners.ernie.api._
import net.liftweb.common.Full
import net.liftweb.common.Full
import net.liftweb.common.Full
import net.liftweb.http.rest.RestHelper
import net.liftweb.common.Full
import net.liftweb.http.InternalServerErrorResponse
import net.liftweb.http.ResponseWithReason
import net.liftweb.http.InMemoryResponse
import net.liftweb.common.Full
import net.liftweb.http.MethodNotAllowedResponse
import RestGenerator._
import com.ksmpartners.ernie.server.RestGenerator.Parameter
import net.liftweb.http.InternalServerErrorResponse
import com.ksmpartners.ernie.server.RestGenerator.Filter
import com.ksmpartners.ernie.server.RestGenerator.RequestTemplate
import net.liftweb.http.ResponseWithReason
import net.liftweb.http.InMemoryResponse
import net.liftweb.common.Full
import net.liftweb.http.MethodNotAllowedResponse
import com.ksmpartners.ernie.server.RestGenerator.Variable
import com.ksmpartners.ernie.server.RestGenerator.Action
import com.ksmpartners.ernie.api.ReportOutputException
import com.ksmpartners.ernie.server.RestGenerator.Resource
import com.ksmpartners.ernie.server.RestGenerator.ErnieError
import com.ksmpartners.ernie.server.RestGenerator.Package

object RestGenerator {
  type restFunc = (() => Box[LiftResponse])
  type restFilter = (restFunc => () => Box[LiftResponse])

  val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.server.RestGenerator")
  case class Parameter(param: String, paramType: String, dataType: String, defaultValue: String*)
  case class Filter(name: String, filter: (Req => restFunc => restFunc), param: Option[Parameter], error: ErnieError)
  case class Variable(data: Any)
  case class Package(req: Req, params: Variable*)
  case class ErnieError(resp: LiftResponse, exception: Option[Exception]) {
    def toResponse(reason: Option[String]): ResponseWithReason =
      if (resp.isInstanceOf[ResponseWithReason]) resp.asInstanceOf[ResponseWithReason]
      else ResponseWithReason(resp, if (reason.isEmpty) { exception.map(e => e.getMessage) getOrElse "" } else reason.get)

    def send(reason: Option[String]): Box[LiftResponse] = {
      log.info("Response: " + toResponse(reason).response.getClass + ". Reason: {}", toResponse(reason).reason)
      Full(toResponse(reason))
    }
    def send(): Box[LiftResponse] = send(None)
  }

  case class Action(name: String, func: (Package) => Box[LiftResponse], summary: String, notes: String, responseClass: String, errors: ErnieError*)

  def apiCall[B](a: Action, call: Any => B, then: B => Box[LiftResponse]): Box[LiftResponse] = try {
    val res = call.apply()
    then(res)
  } catch {
    case e: Exception => checkResponse(a, Some(e))
  }

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

  //def checkResponse(a: Action, e: ErnieResponse): Box[LiftResponse] = checkResponse(a, e.errorOpt)

  case class RequestTemplate(requestType: RequestType, produces: List[String], filters: List[Filter], action: Action, params: Parameter*)
  case class Resource(path: Either[String, Variable], description: String, isResourceGroup: Boolean, requestTemplates: List[RequestTemplate], children: Resource*) {
    def swaggerPath = "/" + {
      if (path.isLeft) path.left.get
      else "{" + path.right.get.data + "}"
    }
  }

  def getToHead(r: RequestTemplate): RequestTemplate = RequestTemplate(HeadRequest, r.produces, r.filters, getToHead(r.action))
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
  def serveApi() {
    api.map(res => traverse(res, Nil))
    tree.foreach(path => if (path.length > 0) {
      val leaf = path(path.length - 1)
      val Path = path.map(f => if (f.path.isLeft) f.path.left.get else "")
      if (Path.contains("")) {
        val Path2 = Path.slice(Path.indexOf("") + 1, Path.length)
        leaf.requestTemplates.foreach(requestTemplate => {
          if (leaf isResourceGroup) serve(Path.slice(0, Path.indexOf("")) prefix {
            case req @ Req(variable :: Path2 :: Nil, _, requestTemplate.requestType) => foldFilters(req, requestTemplate.filters) apply (() => requestTemplate.action.func(Package(req, Variable(variable))))
          })
          serve(Path.slice(0, Path.indexOf("")) prefix {
            case req @ Req(variable :: Path2, _, requestTemplate.requestType) => foldFilters(req, requestTemplate.filters) apply (() => requestTemplate.action.func(Package(req, Variable(variable))))
          })

        })
        serve(Path.slice(0, Path.indexOf("")) prefix {
          case req @ Req(variable :: Path2, _, _) => Full(MethodNotAllowedResponse())
        })
      } else {
        leaf.requestTemplates.foreach(requestTemplate => {
          if (leaf isResourceGroup) serve(Path prefix {
            case req @ Req(Nil, _, requestTemplate.requestType) => foldFilters(req, requestTemplate.filters) apply (() => requestTemplate.action.func(Package(req)))
          })
          serve {
            case req @ Req(Path, _, requestTemplate.requestType) => foldFilters(req, requestTemplate.filters) apply (() => requestTemplate.action.func(Package(req)))
          }

        })
        serve {
          case req @ Req(Path, _, _) => Full(MethodNotAllowedResponse())
        }
      }
    })
  }

}