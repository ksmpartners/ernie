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
import com.ksmpartners.ernie.model.{ DeleteStatus, JobStatus }
import com.ksmpartners.ernie.server.service.JobDependencies._
import com.ksmpartners.ernie.engine.{ PurgeResponse, PurgeRequest }

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
    def get(uriPrefix: String) = {
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
     * Sends the given ReportRequest to the Coordinator to be scheduled
     *
     * @return the jobId returned by the Coordinator associated with the request
     */
    def post(body: Box[Array[Byte]], hostAndPath: String): Box[LiftResponse] = {
      try {
        if (body.isEmpty) {
          log.debug("Response: Bad Response. Reason: Undefined byte array")
          Full(BadResponse())
        } else {
          val req = deserialize(body.open_!, classOf[model.ReportRequest])
          val respOpt = (coordinator !? (timeout, engine.ReportRequest(req.getDefId, req.getRptType, if (req.getRetentionDays == 0) None else Some(req.getRetentionDays),
            { val params: collection.immutable.Map[String, String] = if (req.getReportParameters != null) req.getReportParameters.toMap else Map.empty[String, String]; params }))).asInstanceOf[Option[engine.ReportResponse]]
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
          Full(BadResponse())
        }
      }
    }
    def post(body: Box[Array[Byte]]): Box[LiftResponse] = post(body, "")
    def post(req: Req): Box[LiftResponse] = post(req.body, req.hostAndPath)

    def purge(): Box[LiftResponse] = {
      val purgeResp = (coordinator !? PurgeRequest()).asInstanceOf[PurgeResponse]
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
        } else if (statusResponse.jobStatus != JobStatus.COMPLETE) {
          log.debug("Response: Bad Response. Reason: Get attempted on incomplete job.")
          Full(BadResponse())
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
              Full(BadResponse())
            }
          }
        }
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
          Full(BadResponse())
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
