/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.server.service

import net.liftweb.common.{ Box, Full }
import net.liftweb.http._
import com.ksmpartners.ernie.model
import com.ksmpartners.ernie.engine
import java.io.IOException
import java.util
import org.slf4j.{ LoggerFactory, Logger }
import com.ksmpartners.ernie.server.JsonTranslator

/**
 * Dependencies for starting and interacting with jobs for the creation of reports
 */
trait JobDependencies extends RequiresCoordinator
    with RequiresReportManager {

  private val log: Logger = LoggerFactory.getLogger(classOf[JobDependencies])

  /**
   * Resource for handling HTTP requests at /jobs
   */
  class JobsResource extends JsonTranslator {
    /**
     * Returns a Box[ListResponse] containing a map of jobId to URI for that jobId
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
    def post(body: Box[Array[Byte]]) = {
      try {
        val req = deserialize(body.open_!, classOf[model.ReportRequest])
        val response = (coordinator !? engine.ReportRequest(req.getDefId, req.getRptType)).asInstanceOf[engine.ReportResponse]
        getJsonResponse(new model.ReportResponse(response.jobId), 201)
      } catch {
        case e: IOException => {
          log.error("Caught exception while handling request: {}", e.getMessage)
          Full(BadResponse())
        }
      }
    }
  }

  /**
   * Resource for handling HTTP requests at /jobs/<JOB_ID>/status
   */
  class JobStatusResource extends JsonTranslator {
    /**
     * Returns a Box[ListResponse] containing status for the given jobId
     */
    def get(jobId: String) = {
      val response = (coordinator !? engine.StatusRequest(jobId.toLong)).asInstanceOf[engine.StatusResponse]
      getJsonResponse(new model.StatusResponse(response.jobStatus))
    }
  }

  /**
   * Resource for handling HTTP requests at /jobs/<JOB_ID>/result
   */
  class JobResultsResource {
    /**
     * Returns a Box[StreamingResponse] containing the result content for the given jobId
     */
    def get(jobId: String) = {
      val response = (coordinator !? engine.ResultRequest(jobId.toLong)).asInstanceOf[engine.ResultResponse]

      if (response.rptId.isDefined) {
        val fileStream = reportManager.getReportContent(response.rptId.get).get
        val header: List[(String, String)] =
          ("Content-Type" -> "application/pdf") ::
            ("Content-Length" -> fileStream.available.toString) ::
            ("Content-Disposition" -> ("attachment; filename=\"" + response.rptId.get + ".pdf\"")) :: Nil
        Full(StreamingResponse(
          fileStream,
          () => { fileStream.close() }, // On end method.
          fileStream.available,
          header, Nil, 200))
      } else {
        Full(BadResponse())
      }
    }
  }

}
