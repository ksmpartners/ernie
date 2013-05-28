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
import org.slf4j.{ LoggerFactory, Logger }
import com.ksmpartners.ernie.server.JsonTranslator
import com.ksmpartners.ernie.model.{ DeleteStatus, JobStatus }
import com.ksmpartners.ernie.server.service.JobDependencies._

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
      val response = (coordinator !? engine.JobsListRequest()).asInstanceOf[engine.JobsListResponse]
      val jobsMap: util.Map[String, String] = new util.HashMap
      response.jobsList.foreach({ jobId =>
        jobsMap.put(jobId, uriPrefix + "/" + jobId)
      })
      getJsonResponse(new model.JobsMapResponse(jobsMap))
    }
    /**
     * Sends the given ReportRequest to the Coordinator to be scheduled
     *
     * @return the jobId returned by the Coordinator associated with the request
     */
    def post(body: Box[Array[Byte]], hostAndPath: String): Box[LiftResponse] = {
      try {
        val req = deserialize(body.open_!, classOf[model.ReportRequest])
        val response = (coordinator !? engine.ReportRequest(req.getDefId, req.getRptType, if (req.getRetentionDays == 0) None else Some(req.getRetentionDays))).asInstanceOf[engine.ReportResponse]
        if (response.jobStatus == JobStatus.IN_PROGRESS)
          getJsonResponse(new model.ReportResponse(response.jobId, response.jobStatus), 201, List(("Location", hostAndPath + "/jobs/" + response.jobId)))
        else if (response.jobStatus == JobStatus.FAILED_RETENTION_DATE_EXCEEDS_MAXIMUM)
          Full(ResponseWithReason(BadResponse(), "Retention date exceeds maximum"))
        else if (response.jobStatus == JobStatus.FAILED_RETENTION_DATE_PAST)
          Full(ResponseWithReason(BadResponse(), "Retention date before request time"))
        else Full(BadResponse())
      } catch {
        case e: IOException => {
          log.error("Caught exception while handling request: {}", e.getMessage)
          Full(BadResponse())
        }
      }
    }
    def post(body: Box[Array[Byte]]): Box[LiftResponse] = post(body, "")
  }

  /**
   * Resource for handling HTTP requests at /jobs/<JOB_ID>/status
   */
  class JobStatusResource extends JsonTranslator {
    /**
     * Return a Box[ListResponse] containing status for the given jobId
     */
    def get(jobId: String) = {
      val response = (coordinator !? engine.StatusRequest(jobId.toLong)).asInstanceOf[engine.StatusResponse]
      if (response.jobStatus == JobStatus.DELETED) Full(GoneResponse())
      else getJsonResponse(new model.StatusResponse(response.jobStatus))
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
      val statusResponse = (coordinator !? engine.StatusRequest(jobId.toLong)).asInstanceOf[engine.StatusResponse]
      if (statusResponse.jobStatus == JobStatus.DELETED) Full(GoneResponse())
      else if (statusResponse.jobStatus == JobStatus.NO_SUCH_JOB) Full(NotFoundResponse())
      else if (statusResponse.jobStatus != JobStatus.COMPLETE) Full(BadResponse())
      else {
        val response = (coordinator !? engine.ResultRequest(jobId.toLong)).asInstanceOf[engine.ResultResponse]

        if (response.rptId.isDefined) {
          val rptId = response.rptId.get
          val report = reportManager.getReport(rptId).get
          val fileStream = reportManager.getReportContent(report).get
          val fileName = report.getReportName

          val header: List[(String, String)] =
            ("Content-Type" -> ("application/" + report.getReportType.toString.toLowerCase)) ::
              ("Content-Length" -> fileStream.available.toString) ::
              ("Content-Disposition" -> ("attachment; filename=\"" + fileName + "\"")) :: Nil

          if (!req.isEmpty && !req.open_!.headers.contains(("Accept", header(0)._2))) Full(NotAcceptableResponse("Resource only serves " + report.getReportType.toString))
          else Full(StreamingResponse(
            fileStream,
            () => { fileStream.close() }, // On end method.
            fileStream.available,
            header, Nil, 200))

        } else {
          Full(BadResponse())
        }
      }
    }

    /**
     * Purges the report output for a given jobId
     */
    def del(jobId: String): Box[LiftResponse] = {
      val response = (coordinator !? engine.DeleteRequest(jobId.toLong)).asInstanceOf[engine.DeleteResponse]
      if (response.deleteStatus == DeleteStatus.SUCCESS) getJsonResponse(new model.DeleteResponse(response.deleteStatus))
      else if (response.deleteStatus == DeleteStatus.NOT_FOUND) Full(NotFoundResponse("Job ID not found"))
      else Full(BadResponse())
    }
  }

}

object JobDependencies {
  private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.server.service.JobDependencies")
}
