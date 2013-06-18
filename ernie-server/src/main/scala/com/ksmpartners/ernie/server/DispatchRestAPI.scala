/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.server

import net.liftweb.common.{ Box, Full }
import com.ksmpartners.ernie.server.filter.AuthUtil._
import com.ksmpartners.ernie.server.filter.SAMLConstants._

import net.liftweb.http._
import org.slf4j.{ Logger, LoggerFactory }
import rest.RestHelper
import com.ksmpartners.ernie.model.ModelObject
import service.ServiceRegistry
import RestGenerator._
import ErnieRequestTemplates._
import net.liftweb.json._
import com.ksmpartners.ernie.api.ReportOutputException
import net.liftweb.json.JsonDSL._
/**
 * Object containing the stateless dispatch definition for an ernie server
 */
object DispatchRestAPI extends RestGenerator with JsonTranslator {

  private val log = LoggerFactory.getLogger("com.ksmpartners.ernie.server.DispatchRestAPI")

  /**
   * Method that verifies that the requesting user is in the given role
   * @param req - The request being handled
   * @param role - The role to verify
   * @param f - The function to be called if the user is in the role
   * @return the function f, or a ForbiddenResponse if the user is not in the specified role
   */
  private def authFilter(req: Req, role: String*)(f: () => Box[LiftResponse]): () => Box[LiftResponse] = {
    if (role.foldLeft(false)((b, s) => b || isUserInRole(req, s))) f else () => {
      log.debug("Response: Forbidden Response. Reason: User is not authorized to perform that action")
      Full(ForbiddenResponse("User is not authorized to perform that action"))
    }
  }
  val readAuthFilter = Filter("Read Authorization Filter", authFilter(_: Req, readRole)_, Some(Parameter("Authorization", "header", "string")), ErnieError(ResponseWithReason(ForbiddenResponse(), "User is not authorized to perform that action"), None))
  val writeAuthFilter = Filter("Write Authorization Filter", authFilter(_: Req, writeRole)_, Some(Parameter("Authorization", "header", "string")), ErnieError(ResponseWithReason(ForbiddenResponse(), "User is not authorized to perform that action"), None))
  val writeRunAuthFilter = Filter("Write Authorization Filter", authFilter(_: Req, runRole, writeRole)_, Some(Parameter("Authorization", "header", "string")), ErnieError(ResponseWithReason(ForbiddenResponse(), "User is not authorized to perform that action"), None))

  /**
   * Method that verifies that the requesting user accepts the correct ctype
   * @param req - The request being handled
   * @param f - The function to be called if the user accepts the correct ctype
   * @return the function f, or a NotAcceptableResponse if the user does not accept the correct ctype
   */
  private def ctypeFilter(req: Req)(f: () => Box[LiftResponse]): () => Box[LiftResponse] = {
    if (acceptsErnieJson(req)) f else () => {
      log.debug("Response: Not Acceptable Response. Reason: Resource only serves " + ModelObject.TYPE_FULL)
      Full(NotAcceptableResponse("Resource only serves " + ModelObject.TYPE_FULL))
    }
  }
  val jsonFilter = Filter("JSON Content Type Filter", ctypeFilter(_: Req)_, Some(Parameter("Accept", "header", "string")), ErnieError(ResponseWithReason(NotAcceptableResponse(), "Resource only serves " + ModelObject.TYPE_FULL), None))

  val idFilter = Filter("ID is long filter", idIsLongFilter(_: Req)_, None, ErnieError(ResponseWithReason(BadResponse(), "Job ID provided is not a number"), None))
  private def idIsLongFilter(req: Req)(f: () => Box[LiftResponse]): () => Box[LiftResponse] = try {
    req.path(0).toLong
    f
  } catch {
    case _ => () => {
      log.debug("Response: Bad Response. Reason: Job ID provided is not a number")
      Full(ResponseWithReason(BadResponse(), ("Job ID provided is not a number: " + req.path(0))))
    }
  }

  case class TimeoutResponse() extends LiftResponse with HeaderDefaults {
    def toResponse = InMemoryResponse(Array(), headers, cookies, 504)
  }

  def timeoutErnieError(src: String = null): ErnieError = ErnieError(TimeoutResponse(), Some(com.ksmpartners.ernie.api.TimeoutException(if (src != null) src + " timed out" else "Timeout")))

  def shutdown() {
    //   ServiceRegistry.shutdownResource.shutdown()
  }

  val reportDetail = Resource(Left("detail"), "Report details", false, List(getReportDetail, headReportDetail))
  val jobResult = Resource(Left("result"), "Job results", false, List(getJobResult, headJobResult, deleteJobResult), reportDetail)
  val jobStatus = Resource(Left("status"), "Job status", false, List(getJobStatus, headJobStatus))
  val job = Resource(Right(Variable("job_id")), "Job resource", false, List(getJob, headJob), jobStatus, jobResult)
  val expiredCatalog = Resource(Left("expired"), "Expired catalog", false, List(purgeExpired, getExpiredCatalog, headExpiredCatalog))
  val failedCatalog = Resource(Left("failed"), "Failed catalog", false, List(getFailedCatalog, headFailedCatalog))
  val deletedCatalog = Resource(Left("deleted"), "Deleted catalog", false, List(getDeletedCatalog, headDeletedCatalog))
  val completeCatalog = Resource(Left("complete"), "Complete catalog", false, List(getCompleteCatalog, headCompleteCatalog))
  val jobsCatalog = Resource(Left("catalog"), "Full catalog", false, List(getCatalog, headCatalog))
  val jobs = Resource(Left("jobs"), "Jobs api", true, List(getJobsList, headJobsList, postJob), job, jobsCatalog, completeCatalog, expiredCatalog, failedCatalog, deletedCatalog)

  val design = Resource(Left("rptdesign"), "Definition rptdesign", false, List(putDesign))
  val defi = Resource(Right(Variable("def_id")), "Definition resource", false, List(getDef, headDef, deleteDef), design)
  val defs = Resource(Left("defs"), "Definitions api", true, List(getDefs, headDefs, postDef), defi)

  protected val api = jobs :: defs :: Nil

  var jobsAPI: JObject = null
  var defsAPI: JObject = null
  var resourceListing: JObject = null

  def init() {

    ServiceRegistry.init()

    ErnieModels.addModels()
    jobsAPI = buildSwaggerApi(".1", "1.1", "http://localhost:8080", jobs)
    defsAPI = buildSwaggerApi(".1", "1.1", "http://localhost:8080", defs)
    resourceListing = buildSwaggerResourceListing(".1", "1.1", "http://localhost:8080")
    serve {
      case Req("api" :: Nil, "json", GetRequest) => resourceListing
      case Req("jobs" :: Nil, "json", GetRequest) => jobsAPI
      case Req("defs" :: Nil, "json", GetRequest) => defsAPI
    }
    super.serveApi()

  }

}

object ErnieModels {
  def addModels() {
    DispatchRestAPI.addSwaggerModel("DefinitionEntity", definitionEntity)
    DispatchRestAPI.addSwaggerModel("DefinitionResponse", definitionResponse)
    DispatchRestAPI.addSwaggerModel("DeleteResponse", deleteResponse)
    DispatchRestAPI.addSwaggerModel("JobEntity", jobEntity)
    DispatchRestAPI.addSwaggerModel("JobsCatalogResponse", jobsCatalogResponse)
    DispatchRestAPI.addSwaggerModel("JobsMapResponse", jobsMapResponse)
    DispatchRestAPI.addSwaggerModel("ReportDefinitionMapResponse", reportDefinitionMapResponse)
    DispatchRestAPI.addSwaggerModel("ReportEntity", reportEntity)
    DispatchRestAPI.addSwaggerModel("ReportResponse", reportResponse)
    DispatchRestAPI.addSwaggerModel("StatusResponse", statusResponse)
  }

  val definitionEntity = ("DefinitionEntity" ->
    (("properties" -> JNothing) ~
      ("id" -> "DefinitionEntity")))
  val definitionResponse = ("DefinitionResponse" ->
    (("properties" -> JNothing) ~
      ("id" -> "DefinitionResponse")))
  val deleteResponse = ("DeleteResponse" ->
    (("properties" -> JNothing) ~
      ("id" -> "DeleteResponse")))

  //val jobsCatalogResponse = ("JobsCatalogResponse" ->
  // ("type" -> "Array") ~ ("description" -> "Jobs Catalog") ~ ("$ref" -> "JobEntity"))
  val jobsMapResponse = ("jobStatusMap" -> ("id" -> "jobStatusMap") ~ ("properties" -> ("jobStatusMap" ->
    (("type" -> "Array") ~ ("items" -> ("type" -> "string")) ~ ("description" -> "Jobs map")))))
  val jobsCatalogResponse = ("JobsCatalogResponse" -> ("id" -> "JobsCatalogResponse") ~ ("properties" -> ("jobsCatalog" ->
    (("type" -> "Array") ~ ("items" -> ("$ref" -> "JobEntity"))))))
  val reportDefinitionMapResponse = ("ReportDefinitionMapResponse" ->
    (("properties" -> JNothing) ~
      ("id" -> "ReportDefinitionMapResponse")))
  val reportEntity = ("ReportEntity" ->
    (("properties" ->
      ("createdDate" -> ("type" -> "Date")) ~
      ("startDate" -> ("type" -> "Date")) ~
      ("finishDate" -> ("type" -> "Date")) ~
      ("retentionDate" -> ("type" -> "Date")) ~
      ("rptId" -> ("type" -> "string")) ~
      ("sourceDefId" -> ("type" -> "string")) ~
      ("createdUser" -> ("type" -> "string")) ~
      ("params" -> ("type" -> "Array") ~ ("description" -> "Report parameters") ~ ("items" -> ("type" -> "string"))) ~
      ("reportType" -> ("type" -> "string"))) ~
      ("id" -> "ReportEntity")))
  val jobEntity = ("JobEntity" ->
    ("properties" ->
      ("jobId" -> ("type" -> "long")) ~
      ("jobStatus" -> ("type" -> "string")) ~
      ("submitDate" -> ("type" -> "Date")) ~
      ("rptId" -> ("type" -> "string")) ~
      ("rptEntity" -> ("type" -> "ReportEntity"))) ~
      ("id" -> "JobEntity"))
  val reportResponse = ("ReportResponse" ->
    (("properties" -> JNothing) ~
      ("id" -> "ReportResponse")))
  val statusResponse = ("StatusResponse" ->
    (("properties" -> JNothing) ~
      ("id" -> "StatusResponse")))
  val models = definitionEntity ~ definitionResponse ~ deleteResponse ~ jobEntity ~ jobsCatalogResponse ~ jobsMapResponse ~
    reportDefinitionMapResponse ~ reportEntity ~ reportResponse ~ statusResponse
}

object ErnieRequestTemplates {
  import DispatchRestAPI._

  val justJSON = List(ModelObject.TYPE_FULL)
  val anything = List("*")

  val getJobsList = RequestTemplate(GetRequest, justJSON, List(readAuthFilter, jsonFilter), ServiceRegistry.jobsResource.getJobsListAction)
  val headJobsList = getToHead(getJobsList)
  val postJob = RequestTemplate(PostRequest, justJSON, List(writeRunAuthFilter, jsonFilter), ServiceRegistry.jobsResource.postJobAction, Parameter("ReportRequest", "body", "ReportRequest"))
  val getCatalog = RequestTemplate(GetRequest, justJSON, List(readAuthFilter, jsonFilter), ServiceRegistry.jobsResource.getJobsCatalogAction)
  val headCatalog = getToHead(getCatalog)
  val getCompleteCatalog = RequestTemplate(GetRequest, justJSON, List(readAuthFilter, jsonFilter), ServiceRegistry.jobsResource.getCompleteCatalogAction)
  val headCompleteCatalog = getToHead(getCompleteCatalog)
  val getFailedCatalog = RequestTemplate(GetRequest, justJSON, List(readAuthFilter, jsonFilter), ServiceRegistry.jobsResource.getFailedCatalogAction)
  val headFailedCatalog = getToHead(getFailedCatalog)
  val getDeletedCatalog = RequestTemplate(GetRequest, justJSON, List(readAuthFilter, jsonFilter), ServiceRegistry.jobsResource.getDeletedCatalogAction)
  val headDeletedCatalog = getToHead(getDeletedCatalog)
  val getExpiredCatalog = RequestTemplate(GetRequest, justJSON, List(readAuthFilter, jsonFilter), ServiceRegistry.jobsResource.getExpiredCatalogAction)
  val headExpiredCatalog = getToHead(getExpiredCatalog)
  val purgeExpired = RequestTemplate(DeleteRequest, justJSON, List(writeAuthFilter, jsonFilter), ServiceRegistry.jobsResource.purgeAction)
  val getJob = RequestTemplate(GetRequest, justJSON, List(readAuthFilter, jsonFilter, idFilter), ServiceRegistry.jobsResource.getJobDetailAction)
  val headJob = getToHead(getJob)
  val getJobStatus = RequestTemplate(GetRequest, justJSON, List(readAuthFilter, jsonFilter, idFilter), ServiceRegistry.jobStatusResource.getJobStatusAction)
  val headJobStatus = getToHead(getJobStatus)
  val getJobResult = RequestTemplate(GetRequest, anything, List(readAuthFilter, idFilter), ServiceRegistry.jobResultsResource.getJobResultAction, Parameter("Accept", "header", "string"))
  val headJobResult = getToHead(getJobResult)
  val deleteJobResult = RequestTemplate(DeleteRequest, justJSON, List(writeAuthFilter, jsonFilter, idFilter), ServiceRegistry.jobResultsResource.deleteReportAction)
  val getReportDetail = RequestTemplate(GetRequest, justJSON, List(readAuthFilter, jsonFilter, idFilter), ServiceRegistry.jobResultsResource.getDetailAction)
  val headReportDetail = getToHead(getReportDetail)

  val getDefs = RequestTemplate(GetRequest, justJSON, List(readAuthFilter, jsonFilter), ServiceRegistry.defsResource.getDefsAction)
  val headDefs = getToHead(getDefs)
  val postDef = RequestTemplate(PostRequest, justJSON, List(writeAuthFilter, jsonFilter), ServiceRegistry.defsResource.postDefAction, Parameter("DefinitionEntity", "body", "DefinitionEntity"))
  val getDef = RequestTemplate(GetRequest, justJSON, List(readAuthFilter, jsonFilter), ServiceRegistry.defDetailResource.getDefDetailAction)
  val headDef = getToHead(getDef)
  val putDesign = RequestTemplate(PutRequest, justJSON, List(writeAuthFilter, jsonFilter), ServiceRegistry.defDetailResource.putDefAction, Parameter("Rptdesign", "body", "byte"))
  val deleteDef = RequestTemplate(DeleteRequest, justJSON, List(writeAuthFilter, jsonFilter), ServiceRegistry.defDetailResource.deleteDefAction)
}

object RestGenerator {
  type restFunc = (() => Box[LiftResponse])
  type restFilter = (restFunc => () => Box[LiftResponse])
  //  trait Filter(name: String, filter: (Req => restFunc => restFunc), error: (Int, String))
  case class Parameter(param: String, paramType: String, dataType: String)
  case class Filter(name: String, filter: (Req => restFunc => restFunc), param: Option[Parameter], error: ErnieError)
  case class Variable(data: Any)
  case class Package(req: Req, params: Variable*)
  case class ErnieError(resp: LiftResponse, exception: Option[Exception]) {
    def toResponse(reason: String = null): ResponseWithReason =
      if (resp.isInstanceOf[ResponseWithReason]) resp.asInstanceOf[ResponseWithReason]
      else ResponseWithReason(resp, if (reason == null) exception.map(e => e.getMessage) getOrElse "" else reason)
  }

  case class Action(name: String, func: (Package) => Box[LiftResponse], summary: String, notes: String, responseClass: String, errors: ErnieError*)

  def checkResponse(a: Action, e: Option[Exception]): Box[LiftResponse] = if (e.isDefined) {
    val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.server.RestGenerator")
    val errors = a.errors
    var result: List[ResponseWithReason] = Nil
    errors.foreach(f => {
      if ((f.exception.isDefined) && (e.get.getClass == f.exception.get.getClass)) {
        if (e.get.isInstanceOf[ReportOutputException]) {
          if (e.get.asInstanceOf[ReportOutputException].status.toList.contains(f.exception.get.asInstanceOf[ReportOutputException].status.getOrElse(null)))
            result.::=(f.toResponse(e.get.getMessage))
        } else result.::=(f.toResponse(e.get.getMessage))
      }
    })
    if (result.isEmpty) {
      log.debug("Response: Internal Server Error. Reason: {}", e.get.getMessage)
      Full(ResponseWithReason(InternalServerErrorResponse(), e.get.getMessage))
    } else {
      log.debug("Response: " + result.head.response.getClass + ", reason: {}", result.head.reason)
      Full(result.head)
    }
  } else net.liftweb.common.Empty

  def checkResponse(a: Action, e: com.ksmpartners.ernie.api.ErnieResponse): Box[LiftResponse] = checkResponse(a, e.errorOpt)

  case class RequestTemplate(requestType: RequestType, produces: List[String], filters: List[Filter], action: Action, params: Parameter*) {
    def toSwaggerOperation: JObject = ("httpMethod" -> requestTypeToSwagger(requestType)) ~ ("nickname" -> action.name) ~ ("produces" -> produces) ~
      ("responseClass" -> action.responseClass) ~ ("parameters" -> {
        filters.filter(p => p.param.isDefined).map(f => {
          DispatchRestAPI.buildSwaggerParam(f.param.get)
        }) ++ params.map(f => DispatchRestAPI.buildSwaggerParam(f))
      }) ~ ("summary" -> action.summary) ~
      ("notes" -> action.notes) ~ ("errorResponses" -> (action.errors ++ filters.map(f => f.error)).foldLeft(List.empty[JObject])((list, e) => list.::(("code" -> e.toResponse(null).toResponse.code) ~ ("reason" -> e.toResponse(null).reason))))
  }
  case class Resource(path: Either[String, Variable], description: String, isResourceGroup: Boolean, requestTemplates: List[RequestTemplate], children: Resource*) {
    def swaggerPath = "/" + {
      if (path.isLeft) path.left.get
      else "{" + path.right.get.data + "}"
    }
  }

  def getToHead(r: RequestTemplate): RequestTemplate = RequestTemplate(HeadRequest, r.produces, r.filters, getToHead(r.action))
  def getToHead(a: Action): Action = Action(a.name + "Head", headFilter(a.func), a.summary, a.notes, a.responseClass, a.errors: _*)

  private def headFilter(f: () => Box[LiftResponse]): () => Box[LiftResponse] = { () =>
    {
      val respBox = f()
      val resp: LiftResponse = respBox.open_!
      val response = InMemoryResponse(Array(), resp.toResponse.headers, resp.toResponse.cookies, resp.toResponse.code)
      Full(response)
    }
  }
  private def requestTypeToSwagger(r: RequestType): String = r match {
    case GetRequest => "GET"
    case PostRequest => "POST"
    case PutRequest => "PUT"
    case DeleteRequest => "DELETE"
    case HeadRequest => "HEAD"
    case _ => ""
  }

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

  private var swaggerModels: Map[String, JObject] = Map.empty[String, JObject]

  def addSwaggerModel(s: String, obj: JObject) {
    swaggerModels += (s -> obj)
  }

  def getSwaggerModel(s: String): JObject = swaggerModels.getOrElse(s, null)

  protected val api: List[Resource]
  private var tree: List[List[Resource]] = Nil
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
  def buildSwaggerParam(p: Parameter): JObject = buildSwaggerParam(p.param, p.paramType)
  def buildSwaggerParam(v: Variable): JObject = buildSwaggerParam(v.data.toString, "path")
  def buildSwaggerParam(name: String, pT: String = "path"): JObject = buildSwaggerParam(name, pT, "string")
  def buildSwaggerParam(name: String, pT: String, dataType: String): JObject =
    (("paramType" -> pT) ~ ("name" -> name) ~ ("description" -> name) ~ ("dataType" -> dataType) ~ ("required" -> false) ~ ("allowMultiple" -> false))
  def buildSwaggerParam(s: String): JObject = buildSwaggerParam(s, "path")
  def buildSwaggerApi(version: String, swaggerVersion: String, basePath: String, r: Resource) = {
    tree = Nil
    traverse(r, Nil)
    ("apiVersion" -> version) ~ ("swaggerVersion" -> swaggerVersion) ~ ("basePath" -> basePath) ~
      ("resourcePath" -> r.swaggerPath) ~
      ("apis" -> tree.map[JObject, List[JObject]](api => if (api.length > 0) {
        val leaf = api(api.length - 1)
        ("path" -> api.foldLeft("")((path, part) => path + part.swaggerPath)) ~
          ("description" -> leaf.description) ~
          ("operations" -> leaf.requestTemplates.map(f => {
            val op = f.toSwaggerOperation
            op.replace(List("parameters"), api.find(res =>
              res.path.isRight).map[JArray](res => List(buildSwaggerParam(res.path.right.get)) ::: (op \ "parameters").children).getOrElse((op \ "parameters")))
          }).toList)
      } else Nil)) ~ ("models" -> ErnieModels.models)
  }
  def buildSwaggerResourceListing(version: String, swaggerVersion: String, basePath: String) = {
    ("apiVersion" -> version) ~ ("swaggerVersion" -> swaggerVersion) ~ ("basePath" -> basePath) ~
      ("apis" -> api.map[JObject, List[JObject]](f =>
        (("path" -> (f.swaggerPath + ".json")) ~ ("description" -> f.description))))
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
            case req@Req(variable :: Path2 :: Nil, _, requestTemplate.requestType) => foldFilters(req, requestTemplate.filters) apply (() => requestTemplate.action.func(Package(req, Variable(variable))))
          })
          serve(Path.slice(0, Path.indexOf("")) prefix {
            case req@Req(variable :: Path2, _, requestTemplate.requestType) => foldFilters(req, requestTemplate.filters) apply (() => requestTemplate.action.func(Package(req, Variable(variable))))
          })

        })
        serve(Path.slice(0, Path.indexOf("")) prefix {
          case req@Req(variable :: Path2, _, _) => Full(MethodNotAllowedResponse())
        })
      } else {
        leaf.requestTemplates.foreach(requestTemplate => {
          if (leaf isResourceGroup) serve(Path prefix {
            case req@Req(Nil, _, requestTemplate.requestType) => foldFilters(req, requestTemplate.filters) apply (() => requestTemplate.action.func(Package(req)))
          })
          serve {
            case req@Req(Path, _, requestTemplate.requestType) => foldFilters(req, requestTemplate.filters) apply (() => requestTemplate.action.func(Package(req)))
          }

        })
        serve {
          case req@Req(Path, _, _) => Full(MethodNotAllowedResponse())
        }
      }
    })
  }

}
