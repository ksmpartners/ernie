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

import com.ksmpartners.ernie.model.ModelObject
import com.ksmpartners.ernie.server.RestGenerator._
import net.liftweb.http._
import com.ksmpartners.ernie.server.service.ServiceRegistry
import net.liftweb.json._
import JsonDSL._
import net.liftweb.common.Box
import com.ksmpartners.ernie.server.filter.AuthUtil._
import com.ksmpartners.ernie.server.filter.SAMLConstants._
import com.ksmpartners.ernie.server.RestGenerator.Parameter
import com.ksmpartners.ernie.server.RestGenerator.Filter
import com.ksmpartners.ernie.server.RestGenerator.RequestTemplate
import net.liftweb.http.ResponseWithReason
import net.liftweb.common.Full
import scala.Some
import net.liftweb.http.BadResponse
import com.ksmpartners.ernie.server.RestGenerator.ErnieError
import ErnieFilters._
import net.liftweb.http.auth.userRoles

/**
 * Contains filters that pre-process ernie requests
 */
object ErnieFilters {

  /**
   * Method that verifies that the requesting user is in the given role
   * @param req - The request being handled
   * @param role - The role to verify
   * @param f - The function to be called if the user is in the role
   * @return the function f, or a ForbiddenResponse if the user is not in the specified role
   */
  private def authFilter(req: Req, role: String*)(f: () => Box[LiftResponse]): () => Box[LiftResponse] = {
    if (role.foldLeft(false)((b, s) => b || isUserInRole(req, s)) ||
      (if ((!userRoles.isEmpty) && (userRoles.is.size > 0)) {
        role.foldLeft(true)((r: Boolean, theRole: String) => r &&
          (userRoles.is.find(f => f.name == theRole).isDefined ||
            userRoles.is.foldLeft(true)((res: Boolean, userRole: net.liftweb.http.auth.Role) => res && (userRole.name == theRole))))
      } else false)) f
    else () => {
      log.debug("Response: Forbidden Response. Reason: User is not authorized to perform that action")
      Full(ForbiddenResponse("User is not authorized to perform that action"))
    }
  }
  val readAuthFilter = Filter("Read Authorization Filter", authFilter(_: Req, readRole)_, Some(Parameter("Authorization", "header", "string")), ErnieError(ResponseWithReason(ForbiddenResponse(), "User is not authorized to perform that action"), None), readRole)
  val writeAuthFilter = Filter("Write Authorization Filter", authFilter(_: Req, writeRole)_, Some(Parameter("Authorization", "header", "string")), ErnieError(ResponseWithReason(ForbiddenResponse(), "User is not authorized to perform that action"), None), writeRole)
  val writeRunAuthFilter = Filter("Write Authorization Filter", authFilter(_: Req, runRole, writeRole)_, Some(Parameter("Authorization", "header", "string")), ErnieError(ResponseWithReason(ForbiddenResponse(), "User is not authorized to perform that action"), None), writeRole, runRole)
  val authFilters: List[Filter] = List(readAuthFilter, writeAuthFilter, writeRunAuthFilter)

  /**
   * Return true if the given request accepts an ernie response as defined in ModelObject
   */
  def acceptsErnieJson(req: Req): Boolean = req.weightedAccept.find(_.matches(ModelObject.TYPE_PREFIX -> ModelObject.TYPE_POSTFIX)).isDefined

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

  val jsonFilter = Filter("JSON Content Type Filter", ctypeFilter(_: Req)_, Some(Parameter("Accept", "header", "string", ModelObject.TYPE_FULL)), ErnieError(ResponseWithReason(NotAcceptableResponse(), "Resource only serves " + ModelObject.TYPE_FULL), None))
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
}

/**
 * Contains JSON representations of the various responses returned in Ernie operations
 */
object ErnieModels {

  val definitionEntity = ("DefinitionEntity" ->
    (("properties" -> JNothing) ~
      ("id" -> "DefinitionEntity")))
  val definitionResponse = ("DefinitionResponse" ->
    (("properties" -> JNothing) ~
      ("id" -> "DefinitionResponse")))
  val deleteResponse = ("DeleteResponse" ->
    (("properties" -> JNothing) ~
      ("id" -> "DeleteResponse")))
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

/**
 * Contains the  [[com.ksmpartners.ernie.server.RestGenerator.RequestTemplate]]s that define the valid operations for a [[com.ksmpartners.ernie.server.RestGenerator.Resource]]
 */
object ErnieRequestTemplates {

  val justJSON = Some(Product(ModelObject.TYPE_FULL, ""))
  val anything = None
  val jsonFile = Some(Product("json", "json"))

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
  val getJob = RequestTemplate(GetRequest, justJSON, List(readAuthFilter, jsonFilter, idFilter), ServiceRegistry.jobEntityResource.getJobDetailAction)
  val headJob = getToHead(getJob)
  val getJobStatus = RequestTemplate(GetRequest, justJSON, List(readAuthFilter, jsonFilter, idFilter), ServiceRegistry.jobStatusResource.getJobStatusAction)
  val headJobStatus = getToHead(getJobStatus)
  val getJobResult = RequestTemplate(GetRequest, anything, List(readAuthFilter, idFilter), ServiceRegistry.jobResultsResource.getJobResultAction, Parameter("Accept", "header", "string"))
  val headJobResult = getToHead(getJobResult)
  val deleteJobResult = RequestTemplate(DeleteRequest, justJSON, List(writeAuthFilter, jsonFilter, idFilter), ServiceRegistry.jobResultsResource.deleteReportAction)
  val getReportDetail = RequestTemplate(GetRequest, justJSON, List(readAuthFilter, jsonFilter, idFilter), ServiceRegistry.jobResultsResource.getDetailAction)
  val headReportDetail = getToHead(getReportDetail)

  val resourcesJSON = RequestTemplate(GetRequest, jsonFile, Nil, Action("Get Swagger Resources JSON", (_: Package) => Full(JsonResponse(DispatchRestAPI.resourceListing)), "", "", "byte"))
  val jobsJSON = RequestTemplate(GetRequest, jsonFile, Nil, Action("Get Swagger Jobs JSON", (_: Package) => Full(JsonResponse(DispatchRestAPI.jobsAPI)), "", "", "byte"))
  val defsJSON = RequestTemplate(GetRequest, jsonFile, Nil, Action("Get Swagger Defs JSON", (_: Package) => Full(JsonResponse(DispatchRestAPI.defsAPI)), "", "", "byte"))

  val getDefs = RequestTemplate(GetRequest, justJSON, List(readAuthFilter, jsonFilter), ServiceRegistry.defsResource.getDefsAction)
  val headDefs = getToHead(getDefs)
  val postDef = RequestTemplate(PostRequest, justJSON, List(writeAuthFilter, jsonFilter), ServiceRegistry.defsResource.postDefAction, Parameter("DefinitionEntity", "body", "DefinitionEntity"))
  val getDef = RequestTemplate(GetRequest, justJSON, List(readAuthFilter, jsonFilter), ServiceRegistry.defDetailResource.getDefDetailAction)
  val headDef = getToHead(getDef)
  val putDesign = RequestTemplate(PutRequest, justJSON, List(writeAuthFilter, jsonFilter), ServiceRegistry.defDetailResource.putDefAction, Parameter("Rptdesign", "body", "byte"))
  val deleteDef = RequestTemplate(DeleteRequest, justJSON, List(writeAuthFilter, jsonFilter), ServiceRegistry.defDetailResource.deleteDefAction)
}

