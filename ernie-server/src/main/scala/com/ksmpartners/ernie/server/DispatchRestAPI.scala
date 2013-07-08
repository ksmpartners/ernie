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

import net.liftweb.http._
import service.ServiceRegistry
import RestGenerator._
import ErnieRequestTemplates._
import net.liftweb.json._

/**
 * Object containing the stateless dispatch definition for an ernie server.
 * Generates a dispatch table using the resource tree defined in the api value.
 */
object DispatchRestAPI extends RestGenerator with JsonTranslator {

  case class TimeoutResponse() extends LiftResponse with HeaderDefaults {
    def toResponse = InMemoryResponse(Array(), headers, cookies, 504)
  }

  /**
   * Wrap a TimeoutResponse and message with in an ErnieError case class
   */
  def timeoutErnieError(src: String = null): ErnieError = ErnieError(TimeoutResponse(), Some(new java.util.concurrent.TimeoutException(if (src != null) src + " timed out" else "Timeout")))

  /**
   * Shutdown the ernie server
   */
  def shutdown() {
    ServiceRegistry.shutDown
  }

  /**
   * Set this variable for BASIC HTTP authentication.
   * PartialFunction should:
   * 1. Attempt to authenticate the user
   * 1. If successful, populate userRoles RequestVar with roles (see [[com.ksmpartners.ernie.server.filter.SAMLConstants.]]).
   * 1. Return the result of authentication as a Boolean
   * For example:
   * {{{
   *   basicAuthentication = ({
   *     case (user:String, pass:String, req:Req) =>
   *       MyUserCollection.getUser(user, pass).map( u => {
   *         userRoles(u.getRoles)
   *         true
   *       }) getOrElse false
   *   })
   * }}}
   */
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

  /**
   * Initialize the ServiceRegistry and serve the API.
   */
  def init() {

    ServiceRegistry.init()

    jobsAPI = SwaggerUtils.buildSwaggerApi(".1", "1.1", "http://localhost:8080", jobs)
    defsAPI = SwaggerUtils.buildSwaggerApi(".1", "1.1", "http://localhost:8080", defs)
    resourceListing = SwaggerUtils.buildSwaggerResourceListing(List(jobs, defs), ".1", "1.1", "http://localhost:8080")

    super.serveApi()

  }

}

