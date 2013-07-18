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

import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import com.ksmpartners.ernie.server.RestGenerator.{ Parameter, Variable, RequestTemplate, Resource }
import net.liftweb.http._
import org.slf4j.{ LoggerFactory, Logger }

/**
 * Utilities for generating a Swagger specification
 * For more information, see
 * - https://github.com/wordnik/swagger-core/wiki/Resource-Listing
 * - https://github.com/wordnik/swagger-core/wiki/API-Declaration
 */
object SwaggerUtils {

  val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.server.SwaggerUtils")

  /**
   * Convert a [[com.ksmpartners.ernie.server.RestGenerator.Resource.path]] to a String representation usable by Swagger
   * @param path
   * @return
   */
  def toSwaggerPath(path: Either[String, Variable]) = "/" + {
    if (path.isLeft) path.left.get
    else "{" + path.right.get.data + "}"
  }

  /**
   * Return a JSON representation of a Swagger operation given a RequestTemplate
   */
  def requestTemplateToSwaggerOperation(rT: RequestTemplate): JObject = {
    val requestType = rT.requestType
    val produces = rT.produces
    val filters = rT.filters
    val action = rT.action
    val params = rT.params
    ("httpMethod" -> requestTypeToSwagger(requestType)) ~ ("nickname" -> action.name) ~ ("produces" -> produces.map(f => f.accept)) ~
      ("responseClass" -> action.responseClass) ~ ("parameters" -> {
        filters.filter(p => p.param.isDefined).map(f => {
          buildSwaggerParam(f.param.get)
        }) ++ params.map(f => buildSwaggerParam(f))
      }) ~ ("summary" -> action.summary) ~
      ("notes" -> action.notes) ~ ("errorResponses" -> (action.errors ++ filters.map(f => f.error)).foldLeft(List.empty[JObject])((list, e) => list.::(("code" -> e.toResponse(None).toResponse.code) ~ ("reason" -> e.toResponse(None).reason))))
  }

  /**
   * Return a String representation of an HTTP method
   */
  def requestTypeToSwagger(r: RequestType): String = r match {
    case GetRequest => "GET"
    case PostRequest => "POST"
    case PutRequest => "PUT"
    case DeleteRequest => "DELETE"
    case HeadRequest => "HEAD"
    case _ => ""
  }

  /**
   * Return a JSON representation of a parameter
   * See https://github.com/wordnik/swagger-core/wiki/Parameters
   */
  def buildSwaggerParam(p: Parameter): JObject = {
    buildSwaggerParam(p.param, p.paramType, p.defaultValue.headOption)
  }
  /**
   * Covert a Variable to a path (URL) parameter and return a JSON representation
   * See https://github.com/wordnik/swagger-core/wiki/Parameters
   */
  def buildSwaggerParam(v: Variable): JObject = buildSwaggerParam(v.data.toString, "path")

  /**
   * Construct a JSON representation of a URL Swagger parameter
   * See https://github.com/wordnik/swagger-core/wiki/Parameters
   */
  def buildSwaggerParam(name: String, pT: String = "path"): JObject = buildSwaggerParam(name, pT, "string", None)

  /**
   * Construct a JSON representation of a Swagger parameter
   * See https://github.com/wordnik/swagger-core/wiki/Parameters
   * @param name parameter name
   * @param pT the parameter type
   * @param default optionally, specify the default value
   */
  def buildSwaggerParam(name: String, pT: String, default: Option[String]): JObject = buildSwaggerParam(name, pT, "string", None)

  /**
   * Construct a JSON representation of a Swagger parameter
   * See https://github.com/wordnik/swagger-core/wiki/Parameters
   * @param name parameter name
   * @param pT the parameter type
   * @param default optionally, specify the default value
   */
  def buildSwaggerParam(name: String, pT: String, dataType: String, default: Option[String]): JObject =
    (("paramType" -> pT) ~ ("name" -> name) ~ ("description" -> name) ~ ("dataType" -> dataType) ~ ("required" -> false) ~ ("allowMultiple" -> false) ~ ("defaultValue" -> (default getOrElse null)))

  /**
   * Construct a JSON representation of a URL Swagger parameter
   * See https://github.com/wordnik/swagger-core/wiki/Parameters
   * @param s parameter name
   */
  def buildSwaggerParam(s: String): JObject = buildSwaggerParam(s, "path")

  /**
   * Construct a Swagger API declaration from base level Resource r
   * See: https://github.com/wordnik/swagger-core/wiki/API-Declaration
   * @param version API version
   * @param swaggerVersion version of the Swagger spec
   * @param basePath base URL of the API
   * @param r root level resource
   */
  def buildSwaggerApi(version: String, swaggerVersion: String, basePath: String, r: Resource) = {
    var tree: List[List[Resource]] = Nil
    def traverse(res: Resource, path: List[Resource]): Unit = {
      if (res == null) tree = tree
      else if (res.children.isEmpty)
        tree = tree.::((path.::(res)).reverse)
      else {
        res.children.foreach(f => {
          traverse(f, path.::(res))
        })
        tree = tree.::((path.::(res)).reverse)
      }
    }
    traverse(r, Nil)
    ("apiVersion" -> version) ~ ("swaggerVersion" -> swaggerVersion) ~ ("basePath" -> basePath) ~
      ("resourcePath" -> SwaggerUtils.toSwaggerPath(r.path)) ~
      ("apis" -> tree.map[JObject, List[JObject]](api => if (api.length > 0) {
        val leaf = api(api.length - 1)
        ("path" -> api.foldLeft("")((path, part) => path + SwaggerUtils.toSwaggerPath(part.path))) ~
          ("description" -> leaf.description) ~
          ("operations" -> leaf.requestTemplates.filter(f => f.requestType != HeadRequest).map(f => {
            val op = SwaggerUtils.requestTemplateToSwaggerOperation(f)
            op.replace(List("parameters"), api.find(res =>
              res.path.isRight).map[JArray](res => List(buildSwaggerParam(res.path.right.get)) ::: (op \ "parameters").children).getOrElse((op \ "parameters")))
          }).toList)
      } else Nil)) ~ ("models" -> ErnieModels.models)
  }

  /**
   * Construct a Swagger resource listing from a list of root-level Resources
   * See: https://github.com/wordnik/swagger-core/wiki/Resource-Listing
   * @param api a list of root-level resources. For each, there must be a second, corresponding resource that serves an API declaration using [[com.ksmpartners.ernie.server.SwaggerUtils.buildSwaggerApi)]]
   * @param version API version
   * @param swaggerVersion version of the Swagger spec
   * @param basePath base URL of the API
   */
  def buildSwaggerResourceListing(api: List[Resource], version: String, swaggerVersion: String, basePath: String) = {
    ("apiVersion" -> version) ~ ("swaggerVersion" -> swaggerVersion) ~ ("basePath" -> basePath) ~
      ("apis" -> api.map[JObject, List[JObject]](f =>
        (("path" -> (SwaggerUtils.toSwaggerPath(f.path) + "api.json")) ~ ("description" -> f.description))))
  }
}
