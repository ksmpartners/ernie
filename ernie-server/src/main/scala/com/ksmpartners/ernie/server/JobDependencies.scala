/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.server

import net.liftweb.common.{ Box, Full, Empty }
import net.liftweb.http._
import com.ksmpartners.ernie.engine.{ Coordinator, ReportRequest => RReq, ReportResponse => RResp, ResultRequest, ResultResponse, StatusRequest => SReq, StatusResponse => SResp, ShutDownRequest }
import com.ksmpartners.ernie.model.{ StatusResponse, ReportResponse, ReportRequest }
import com.fasterxml.jackson.databind.ObjectMapper
import net.liftweb.util.Props
import java.io.{ FileInputStream, File, IOException }

trait JobDependencies {

  val coordinator = new Coordinator(Props.get("rpt.def.dir").open_!, Props.get("output.dir").open_!).start()

  class JobsResource extends JsonTranslator {
    def get = {
      Full(OkResponse())
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

/**
 * Trait containing methods for serializing/deserializing JSONs
 */
trait JsonTranslator {
  private val mapper = new ObjectMapper

  /**
   * Serializes an object into a JSON String
   */
  def serialize[A](obj: A): String = {
    mapper.writeValueAsString(obj)
  }

  /**
   * Deserializes the given JSON String into an object of the type clazz represents
   */
  def deserialize[A](json: String, clazz: Class[A]): A = {
    mapper.readValue(json, clazz) match {
      case a: A => a
      case _ => throw new ClassCastException
    }
  }

  /**
   * Deserializes the given JSON Array[Byte] into an object of the type clazz represents
   */
  def deserialize[A](json: Array[Byte], clazz: Class[A]): A = {
    mapper.readValue(json, clazz) match {
      case a: A => a
      case _ => throw new ClassCastException
    }
  }

  /**
   * Serializes the given response object into a Full[PlainTextResponse] with a content-type of application/json and
   * an HTTP code of 200
   */
  def getJsonResponse[A](response: A): Box[LiftResponse] = {
    Full(PlainTextResponse(serialize(response), List(("Content-Type", "application/json")), 200))
  }
}