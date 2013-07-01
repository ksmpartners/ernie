/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.server

import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import com.ksmpartners.ernie.server.RestGenerator.{ Parameter, Variable, RequestTemplate, Resource }
import net.liftweb.http._
import org.slf4j.{ LoggerFactory, Logger }

package object SwaggerUtils {

  val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.server.SwaggerUtils")

  def toSwaggerPath(path: Either[String, Variable]) = "/" + {
    if (path.isLeft) path.left.get
    else "{" + path.right.get.data + "}"
  }

  def requestTemplateToSwaggerOperation(rT: RequestTemplate): JObject = {
    val requestType = rT.requestType
    val produces = rT.produces
    val filters = rT.filters
    val action = rT.action
    val params = rT.params
    ("httpMethod" -> requestTypeToSwagger(requestType)) ~ ("nickname" -> action.name) ~ ("produces" -> produces) ~
      ("responseClass" -> action.responseClass) ~ ("parameters" -> {
        filters.filter(p => p.param.isDefined).map(f => {
          buildSwaggerParam(f.param.get)
        }) ++ params.map(f => buildSwaggerParam(f))
      }) ~ ("summary" -> action.summary) ~
      ("notes" -> action.notes) ~ ("errorResponses" -> (action.errors ++ filters.map(f => f.error)).foldLeft(List.empty[JObject])((list, e) => list.::(("code" -> e.toResponse(None).toResponse.code) ~ ("reason" -> e.toResponse(None).reason))))
  }

  def requestTypeToSwagger(r: RequestType): String = r match {
    case GetRequest => "GET"
    case PostRequest => "POST"
    case PutRequest => "PUT"
    case DeleteRequest => "DELETE"
    case HeadRequest => "HEAD"
    case _ => ""
  }

  def buildSwaggerParam(p: Parameter): JObject = {
    buildSwaggerParam(p.param, p.paramType, p.defaultValue.headOption)
  }
  def buildSwaggerParam(v: Variable): JObject = buildSwaggerParam(v.data.toString, "path")
  def buildSwaggerParam(name: String, pT: String = "path"): JObject = buildSwaggerParam(name, pT, "string", None)
  def buildSwaggerParam(name: String, pT: String, default: Option[String]): JObject = buildSwaggerParam(name, pT, "string", None)
  def buildSwaggerParam(name: String, pT: String, dataType: String, default: Option[String]): JObject =
    (("paramType" -> pT) ~ ("name" -> name) ~ ("description" -> name) ~ ("dataType" -> dataType) ~ ("required" -> false) ~ ("allowMultiple" -> false) ~ ("defaultValue" -> (default getOrElse null)))
  def buildSwaggerParam(s: String): JObject = buildSwaggerParam(s, "path")
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
          ("operations" -> leaf.requestTemplates.map(f => {
            val op = SwaggerUtils.requestTemplateToSwaggerOperation(f)
            op.replace(List("parameters"), api.find(res =>
              res.path.isRight).map[JArray](res => List(buildSwaggerParam(res.path.right.get)) ::: (op \ "parameters").children).getOrElse((op \ "parameters")))
          }).toList)
      } else Nil)) ~ ("models" -> ErnieModels.models)
  }
  def buildSwaggerResourceListing(api: List[Resource], version: String, swaggerVersion: String, basePath: String) = {
    ("apiVersion" -> version) ~ ("swaggerVersion" -> swaggerVersion) ~ ("basePath" -> basePath) ~
      ("apis" -> api.map[JObject, List[JObject]](f =>
        (("path" -> (SwaggerUtils.toSwaggerPath(f.path) + ".json")) ~ ("description" -> f.description))))
  }
}
