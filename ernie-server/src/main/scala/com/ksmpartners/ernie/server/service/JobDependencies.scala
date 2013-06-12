/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.server.service

import net.liftweb.common.{ Empty, Box, Full }
import net.liftweb.http._
import com.ksmpartners.ernie.model
import com.ksmpartners.ernie.engine
import java.io.IOException
import java.util
import collection.JavaConversions._
import org.slf4j.{ LoggerFactory, Logger }
import com.ksmpartners.ernie.server.JsonTranslator
import com.ksmpartners.ernie.model._
import com.ksmpartners.ernie.server.service.JobDependencies._
import net.liftweb.http.StreamingResponse
import net.liftweb.http.InternalServerErrorResponse
import net.liftweb.http.ResponseWithReason
import net.liftweb.http.OkResponse
import net.liftweb.http.InMemoryResponse
import net.liftweb.common.Full
import scala.Some
import com.ksmpartners.ernie.engine.PurgeResponse
import net.liftweb.http.BadResponse
import net.liftweb.http.GoneResponse
import com.ksmpartners.ernie.engine.PurgeRequest
import com.ksmpartners.ernie.server.filter.AuthUtil

/**
 * Dependencies for starting and interacting with jobs for the creation of reports
 */
trait JobDependencies extends RequiresCoordinator
    with RequiresReportManager {

  /**
   * Resource for handling HTTP requests at /jobs
   */
  class JobsResource extends JsonTranslator {
    /**
     * Return a Box[ListResponse] containing a map of jobId to URI for that jobId
     */
    def getMap(uriPrefix: String) = {
      val respOpt = (coordinator !? (timeout, engine.JobsListRequest())).asInstanceOf[Option[engine.JobsListResponse]]
      if (respOpt.isEmpty) {
        log.debug("Response: Timeout Response.")
        Full(TimeoutResponse())
      } else {
        val response = respOpt.get
        val jobsMap: util.Map[String, String] = new util.HashMap
        response.jobsList.foreach({ jobId =>
          jobsMap.put(jobId, uriPrefix + "/" + jobId)
        })
        getJsonResponse(new model.JobsMapResponse(jobsMap))
      }
    }

    /**
     * Return a Box[ListResponse] containing a catalog of all JobEntities
     */
    def getCatalog(): Box[LiftResponse] = getCatalog(None)
    def getCatalog(catalog: Option[String]): Box[LiftResponse] = {
      val respOpt: Option[engine.JobsCatalogResponse] = catalog.getOrElse("").toLowerCase match {
        case "failed" => (coordinator !? (timeout, engine.JobsCatalogRequest(Some(JobCatalog.FAILED)))).asInstanceOf[Option[engine.JobsCatalogResponse]]
        case "complete" => (coordinator !? (timeout, engine.JobsCatalogRequest(Some(JobCatalog.COMPLETE)))).asInstanceOf[Option[engine.JobsCatalogResponse]]
        case "expired" => (coordinator !? (timeout, engine.JobsCatalogRequest(Some(JobCatalog.EXPIRED)))).asInstanceOf[Option[engine.JobsCatalogResponse]]
        case "deleted" => (coordinator !? (timeout, engine.JobsCatalogRequest(Some(JobCatalog.DELETED)))).asInstanceOf[Option[engine.JobsCatalogResponse]]
        case "" => (coordinator !? (timeout, engine.JobsCatalogRequest(None))).asInstanceOf[Option[engine.JobsCatalogResponse]]
        case _ => None
      }
      if (respOpt.isEmpty) {
        log.debug("Response: Timeout Response.")
        Full(TimeoutResponse())
      } else {
        val jobsCatalog: util.ArrayList[JobEntity] = new util.ArrayList
        respOpt.get.catalog.foreach(f => jobsCatalog.add(f))
        getJsonResponse(new JobsCatalogResponse(jobsCatalog))
      }
    }

    /**
     * Return a Box[ListResponse] containing a JobEntity
     */
    def get(jobId: String) = {
      val respOpt = (coordinator !? (timeout, engine.JobDetailRequest(jobId.toLong))).asInstanceOf[Option[engine.JobDetailResponse]]
      if (respOpt.isEmpty) {
        log.debug("Response: Timeout Response.")
        Full(TimeoutResponse())
      } else {
        val response = respOpt.get
        if (response.jobEntity isDefined)
          getJsonResponse(response.jobEntity.get)
        else Full(NotFoundResponse())
      }
    }

    /**
     * Sends the given ReportRequest to the Coordinator to be scheduled
     *
     * @return the jobId returned by the Coordinator associated with the request
     */
    def post(body: Box[Array[Byte]], hostAndPath: String, userName: String): Box[LiftResponse] = {
      try {
        if (body.isEmpty) {
          log.debug("Response: Bad Response. Reason: Undefined byte array")
          Full(ResponseWithReason(BadResponse(), "Undefined byte array"))
        } else {
          val req = deserialize(body.open_!, classOf[model.ReportRequest])
          val respOpt = (coordinator !? (timeout, engine.ReportRequest(req.getDefId, req.getRptType, if (req.getRetentionDays == 0) None else Some(req.getRetentionDays),
            { val params: collection.immutable.Map[String, String] = if (req.getReportParameters != null) req.getReportParameters.toMap else Map.empty[String, String]; params }, userName))).asInstanceOf[Option[engine.ReportResponse]]
          if (respOpt.isEmpty) {
            log.debug("Response: Timeout Response.")
            Full(TimeoutResponse())
          } else {
            val response = respOpt.get
            if (response.jobStatus == JobStatus.FAILED_RETENTION_DATE_EXCEEDS_MAXIMUM) {
              log.debug("Response: Bad Response. Reason: Retention date exceeds maximum")
              Full(ResponseWithReason(BadResponse(), "Retention date exceeds maximum"))
            } else if (response.jobStatus == JobStatus.FAILED_RETENTION_DATE_PAST) {
              log.debug("Response: Bad Response. Reason: Retention date before request time")
              Full(ResponseWithReason(BadResponse(), "Retention date before request time"))
            } else if (response.jobStatus == JobStatus.FAILED_NO_SUCH_DEFINITION) {
              log.debug("Response: Bad Response. Reason: No such definition ID")
              Full(ResponseWithReason(BadResponse(), "No such definition ID"))
            } else
              getJsonResponse(new model.ReportResponse(response.jobId, response.jobStatus), 201, List(("Location", hostAndPath + "/jobs/" + response.jobId)))
          }
        }
      } catch {
        case e: IOException => {
          log.error("Caught exception while handling request: {}", e.getMessage)
          log.debug("Response: Bad Response. Reason: Exception thrown in post request")
          Full(ResponseWithReason(BadResponse(), "Exception thrown in post request"))
        }
      }
    }
    def post(body: Box[Array[Byte]], userName: String): Box[LiftResponse] = post(body, "", userName)
    def post(req: Req): Box[LiftResponse] = post(req.body, req.hostAndPath, AuthUtil.getUserName(req))

    def purge(): Box[LiftResponse] = {
      val respOpt = coordinator !? (timeout, PurgeRequest())
      if (respOpt.isEmpty) {
        log.debug("Response: Timeout Response.")
        Full(TimeoutResponse())
      }
      val purgeResp = respOpt.get.asInstanceOf[PurgeResponse]
      if (purgeResp.deleteStatus == DeleteStatus.SUCCESS) {
        log.debug("Response: Ok Response.")
        Full(OkResponse())
      } else {
        log.debug("Response: Internal server error response.")
        Full(InternalServerErrorResponse())
      }
    }
  }

  /**
   * Resource for handling HTTP requests at /jobs/<JOB_ID>/status
   */
  class JobStatusResource extends JsonTranslator {
    /**
     * Return a Box[ListResponse] containing status for the given jobId
     */
    def get(jobId: String) = {
      val respOpt = (coordinator !? (timeout, engine.StatusRequest(jobId.toLong))).asInstanceOf[Option[engine.StatusResponse]]
      if (respOpt.isEmpty) {
        log.debug("Response: Timeout Response.")
        Full(TimeoutResponse())
      } else {
        val response = respOpt.get
        if (response.jobStatus == JobStatus.DELETED) {
          log.debug("Response: Gone Response.")
          Full(GoneResponse())
        } else getJsonResponse(new model.StatusResponse(response.jobStatus))
      }
    }
  }

  /**
   * Resource for handling HTTP requests at /jobs/<JOB_ID>/result
   */
  class JobResultsResource extends JsonTranslator {
    /**
     * Return a Box[StreamingResponse] containing the result content for the given jobId
     */
    def get(jobId: String): Box[LiftResponse] = get(jobId, Empty)

    /**
     * Return a Box[StreamingResponse] containing the result content for the given jobId
     * Overloaded function to include the web service request details to ensure correct Accept
     */
    def get(jobId: String, req: Box[Req]): Box[LiftResponse] = {
      val statusRespOpt = (coordinator !? (timeout, engine.StatusRequest(jobId.toLong))).asInstanceOf[Option[engine.StatusResponse]]
      if (statusRespOpt.isEmpty) {
        log.debug("Response: Timeout Response")
        Full(TimeoutResponse())
      } else {
        val statusResponse = statusRespOpt.get
        if (statusResponse.jobStatus == JobStatus.DELETED) {
          log.debug("Response: Gone Response")
          Full(GoneResponse())
        } else if (statusResponse.jobStatus == JobStatus.NO_SUCH_JOB) {
          log.debug("Response: Not Found Response:")
          Full(NotFoundResponse())
        } else if (statusResponse.jobStatus == JobStatus.EXPIRED) {
          log.debug("Response: Report is expired -- Gone Response.")
          Full(ResponseWithReason(GoneResponse(), "Report expired"))
        } else if (statusResponse.jobStatus != JobStatus.COMPLETE) {
          log.debug("Response: Bad Response. Reason: Get request on incomplete job.")
          Full(ResponseWithReason(BadResponse(), "Get request on incomplete job"))

        } else {
          val respOpt = (coordinator !? (timeout, engine.ResultRequest(jobId.toLong))).asInstanceOf[Option[engine.ResultResponse]]
          if (respOpt.isEmpty) {
            log.debug("Response: Timeout Response")
            Full(TimeoutResponse())
          } else {
            val response = respOpt.get
            if (response.rptId.isDefined) {
              val rptId = response.rptId.get
              val report = reportManager.getReport(rptId).get
              val fileStream = reportManager.getReportContent(report).get
              val fileName = report.getReportName

              val header: List[(String, String)] =
                ("Content-Type" -> ("application/" + report.getReportType.toString.toLowerCase)) ::
                  ("Content-Length" -> fileStream.available.toString) ::
                  ("Content-Disposition" -> ("attachment; filename=\"" + fileName + "\"")) :: Nil

              if (!req.isEmpty && !req.open_!.headers.contains(("Accept", header(0)._2))) {
                fileStream.close
                log.debug("Response: Not Acceptable Response. Reason: Resource only serves " + report.getReportType.toString)
                Full(NotAcceptableResponse("Resource only serves " + report.getReportType.toString))
              } else {
                log.debug("Response: Streaming Response.")
                Full(StreamingResponse(
                  fileStream,
                  () => { fileStream.close() }, // On end method.
                  fileStream.available,
                  header, Nil, 200))
              }
            } else {
              log.debug("Response: Bad Response. Reason: Report ID is undefined.")
              Full(ResponseWithReason(BadResponse(), "Report ID is undefined"))
            }
          }
        }
      }
    }

    /**
     * Retrieves details for output from a given jobId
     */
    def getDetail(jobId: String, req: Box[Req]): Box[LiftResponse] = {
      val respOpt = (coordinator !? (timeout, engine.ReportDetailRequest(jobId.toLong))).asInstanceOf[Option[engine.ReportDetailResponse]]
      if (respOpt.isDefined) {
        if (respOpt.get.rptEntity.isDefined) {
          log.debug("Response: Report Entity")
          getJsonResponse(respOpt.get.rptEntity.get)
        } else {
          log.debug("Response: Not Found Response")
          Full(NotFoundResponse())
        }
      } else {
        log.debug("Response: Internal Server Error Response")
        Full(InternalServerErrorResponse())
      }
    }

    /**
     * Purges the report output for a given jobId
     */
    def del(jobId: String): Box[LiftResponse] = {
      val respOpt = (coordinator !? (timeout, engine.DeleteRequest(jobId.toLong))).asInstanceOf[Option[engine.DeleteResponse]]
      if (respOpt.isEmpty) {
        log.debug("Response: Timeout Response")
        Full(TimeoutResponse())
      } else {
        val response = respOpt.get
        if (response.deleteStatus == DeleteStatus.SUCCESS) getJsonResponse(new model.DeleteResponse(response.deleteStatus))
        else if (response.deleteStatus == DeleteStatus.NOT_FOUND) {
          log.debug("Response: Not Found Response. Reason: Job ID not found")
          Full(NotFoundResponse("Job ID not found"))
        } else if (response.deleteStatus == DeleteStatus.FAILED_IN_USE) {
          log.debug("Response: Conflict Response")
          Full(ConflictResponse())
        } else {
          log.debug("Response: Bad Response. Reason: Definition deletion failed")
          Full(ResponseWithReason(BadResponse(), "Definition deletion failed"))
        }
      }
    }
  }

}

object JobDependencies {
  private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.server.service.JobDependencies")
}

case class TimeoutResponse() extends LiftResponse with HeaderDefaults {
  def toResponse = InMemoryResponse(Array(), headers, cookies, 504)
}
