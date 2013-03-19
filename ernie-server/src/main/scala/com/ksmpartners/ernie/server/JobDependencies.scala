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
import com.ksmpartners.ernie.engine._
import net.liftweb.util.Props
import java.io.{ FileInputStream, File, IOException }

trait JobDependencies {

  val coordinator = new Coordinator(Props.get("rpt.def.dir").open_!, Props.get("output.dir").open_!).start()

  class JobsResource extends JsonTranslator {
    def get = {
      val response = (coordinator !? engine.JobStatusMapRequest()).asInstanceOf[engine.JobStatusMapResponse]
      getJsonResponse(new model.JobStatusMap(response.jobStatusMap))
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

  class ShutdownResource {
    def shutdown() {
      coordinator ! engine.ShutDownRequest
    }
  }

}
