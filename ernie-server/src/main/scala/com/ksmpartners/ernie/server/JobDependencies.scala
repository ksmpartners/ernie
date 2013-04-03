/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.server

import net.liftweb.common.{ Box, Full, Empty }
import net.liftweb.http._
import com.ksmpartners.ernie.model
import com.ksmpartners.ernie.engine
import java.io.IOException
import java.util
import org.slf4j.{ LoggerFactory, Logger }

/**
 * Dependencies for starting and interacting with jobs for the creation of reports
 */
trait JobDependencies extends ActorTrait {

  private val log: Logger = LoggerFactory.getLogger(this.getClass)

  class JobsResource extends JsonTranslator {
    def get(uriPrefix: String) = {
      val response = (coordinator !? engine.JobsListRequest()).asInstanceOf[engine.JobsListResponse]
      val jobsMap: util.Map[String, String] = new util.HashMap
      response.jobsList.foreach({ jobId =>
        jobsMap.put(jobId, uriPrefix + "/" + jobId)
      })
      getJsonResponse(new model.JobsMapResponse(jobsMap))
    }
    def post(body: Box[Array[Byte]]) = {
      try {
        val req = deserialize(body.open_!, classOf[model.ReportRequest])
        val response = (coordinator !? engine.ReportRequest(req.getDefId, req.getRptType)).asInstanceOf[engine.ReportResponse]
        getJsonResponse(new model.ReportResponse(response.jobId), 201)
      } catch {
        case e: IOException => {
          log.error("Caught exception while handling request: {}", e)
          Full(BadResponse())
        }
      }
    }
  }

  class JobStatusResource extends JsonTranslator {
    def get(jobId: String) = {
      val response = (coordinator !? engine.StatusRequest(jobId.toLong)).asInstanceOf[engine.StatusResponse]
      getJsonResponse(new model.StatusResponse(response.jobStatus))
    }
  }

  class JobResultsResource {
    def get(jobId: String) = {
      val response = (coordinator !? engine.ResultRequest(jobId.toLong)).asInstanceOf[engine.ResultResponse]

      if (response.rptId.isDefined) {
        val fileStream = reportManager.getReport(response.rptId.get)
        val header: List[(String, String)] =
          ("Content-type" -> "application/pdf") ::
            ("Content-length" -> fileStream.available.toString) ::
            ("Content-disposition" -> ("attachment; filename=\"" + response.rptId.get + ".pdf\"")) :: Nil
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
