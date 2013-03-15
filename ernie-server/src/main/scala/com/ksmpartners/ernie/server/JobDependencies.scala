/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.server

import net.liftweb.common.{ Box, Full, Empty }
import net.liftweb.http._
import com.ksmpartners.ernie.engine._
import com.ksmpartners.ernie.engine.{ ReportRequest => RReq, ReportResponse => RResp, StatusRequest => SReq, StatusResponse => SResp }
import com.ksmpartners.ernie.model.{ StatusResponse, ReportResponse, ReportRequest }
import net.liftweb.util.Props
import java.io.{ FileInputStream, File, IOException }

trait JobDependencies {

  val coordinator = new Coordinator(Props.get("rpt.def.dir").open_!, Props.get("output.dir").open_!).start()

  class JobsResource extends JsonTranslator {
    def get = {
      val response = (coordinator !? JobStatusMapRequest).asInstanceOf[JobStatusMapResponse]
      getJsonResponse(response.jobStatusMap)
    }
    def put(body: Box[Array[Byte]]) = {
      try {
        val req = deserialize(body.open_!, classOf[ReportRequest])
        val response = (coordinator !? RReq(req.getReportDefId)).asInstanceOf[RResp]
        getJsonResponse(new ReportResponse(response.jobId))
      } catch {
        case e: IOException => Full(BadResponse())
      }
    }
  }

  class JobStatusResource extends JsonTranslator {
    def get(jobId: String) = {
      val response = (coordinator !? SReq(jobId.toLong)).asInstanceOf[SResp]
      getJsonResponse(new StatusResponse(response.jobStatus))
    }
  }

  class JobResultsResource {
    def get(jobId: String) = {
      val response = (coordinator !? ResultRequest(jobId.toLong)).asInstanceOf[ResultResponse]

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
      coordinator ! ShutDownRequest
    }
  }

}
