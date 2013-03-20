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
import java.io.{ FileInputStream, File, IOException }

/**
 * Dependencies for starting and interacting with jobs for the creation of reports
 */
trait JobDependencies extends ActorTrait {

  class JobsResource extends JsonTranslator {
    def get(uriPrefix: String) = {
      val response = (coordinator !? engine.JobsMapRequest(uriPrefix)).asInstanceOf[engine.JobsMapResponse]
      getJsonResponse(new model.JobsMapResponse(response.jobsMap))
    }
    def post(body: Box[Array[Byte]]) = {
      try {
        val req = deserialize(body.open_!, classOf[model.ReportRequest])
        val response = (coordinator !? engine.ReportRequest(req.getReportDefId)).asInstanceOf[engine.ReportResponse]
        getJsonResponse(new model.ReportResponse(response.jobId))
      } catch {
        case e: IOException => Full(BadResponse())
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

      if (response.filePath.isDefined) {
        val file = new File(response.filePath.get)
        val fileStream = new FileInputStream(file)
        val header: List[(String, String)] =
          ("Content-type" -> "application/pdf") ::
            ("Content-length" -> file.length.toString) ::
            ("Content-disposition" -> ("attachment; filename=\"" + file.getName + "\"")) :: Nil
        Full(StreamingResponse(
          fileStream,
          () => { fileStream.close() }, // On end method.
          file.length,
          header, Nil, 200))
      } else {
        Full(BadResponse())
      }
    }
  }

}
