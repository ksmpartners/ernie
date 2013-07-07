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
import net.liftweb.http.auth.{ AuthRole, userRoles }
import com.ksmpartners.ernie.server.filter.SAMLConstants

/**
 * Object containing the stateless dispatch definition for an ernie server
 */
object DispatchRestAPI extends RestGenerator with JsonTranslator {

  private val log = LoggerFactory.getLogger("com.ksmpartners.ernie.server.DispatchRestAPI")

  case class TimeoutResponse() extends LiftResponse with HeaderDefaults {
    def toResponse = InMemoryResponse(Array(), headers, cookies, 504)
  }

  def timeoutErnieError(src: String = null): ErnieError = ErnieError(TimeoutResponse(), Some(new java.util.concurrent.TimeoutException(if (src != null) src + " timed out" else "Timeout")))

  def shutdown() {
    ServiceRegistry.shutDown
  }

  var basicAuthentication: PartialFunction[(String, String, Req), Boolean] = PartialFunction.empty[(String, String, Req), Boolean]

  val reportDetail = Resource(Left("detail"), "Report details", false, List(getReportDetail, headReportDetail))
  val jobResult = Resource(Left("result"), "Job results", false, List(getJobResult, headJobResult, deleteJobResult), reportDetail)
  val jobStatus = Resource(Left("status"), "Job status", false, List(getJobStatus, headJobStatus))
  val job = Resource(Right(Variable("job_id")), "Job resource", false, List(getJob, headJob), jobStatus, jobResult)
  val expiredCatalog = Resource(Left("expired"), "Expired catalog", false, List(purgeExpired, getExpiredCatalog, headExpiredCatalog))
  val failedCatalog = Resource(Left("failed"), "Failed catalog", false, List(getFailedCatalog, headFailedCatalog))
  val deletedCatalog = Resource(Left("deleted"), "Deleted catalog", false, List(getDeletedCatalog, headDeletedCatalog))
  val completeCatalog = Resource(Left("complete"), "Complete catalog", false, List(getCompleteCatalog, headCompleteCatalog))
  val jobsCatalog = Resource(Left("catalog"), "Full catalog", false, List(getCatalog, headCatalog))
  val jobsSwagger = Resource(Left("jobsapi"), "Jobs JSON", false, jobsJSON :: Nil)
  val jobs = Resource(Left("jobs"), "Jobs api", true, List(getJobsList, headJobsList, postJob), job, jobsCatalog, completeCatalog, expiredCatalog, failedCatalog, deletedCatalog)

  val design = Resource(Left("rptdesign"), "Definition rptdesign", false, List(putDesign))
  val defi = Resource(Right(Variable("def_id")), "Definition resource", false, List(getDef, headDef, deleteDef), design)
  val defsSwagger = Resource(Left("defsapi"), "Defs JSON", false, defsJSON :: Nil)
  val defs = Resource(Left("defs"), "Definitions api", true, List(getDefs, headDefs, postDef), defi)

  val swagger = Resource(Left("resources"), "Resources JSON", false, resourcesJSON :: Nil)

  protected val api = jobs :: defs :: swagger :: jobsSwagger :: defsSwagger :: Nil

  var jobsAPI: JObject = null
  var defsAPI: JObject = null
  var resourceListing: JObject = null

  def init() {

    ServiceRegistry.init()

    jobsAPI = SwaggerUtils.buildSwaggerApi(".1", "1.1", "http://localhost:8080", jobs)
    defsAPI = SwaggerUtils.buildSwaggerApi(".1", "1.1", "http://localhost:8080", defs)
    resourceListing = SwaggerUtils.buildSwaggerResourceListing(List(jobs, defs), ".1", "1.1", "http://localhost:8080")

    super.serveApi()

    /* serve {
      case Req("resources" :: Nil, "json", GetRequest) => resourceListing
      case Req("jobs" :: Nil, "json", GetRequest) => jobsAPI
      case Req("defs" :: Nil, "json", GetRequest) => defsAPI
    }*/

  }

}

