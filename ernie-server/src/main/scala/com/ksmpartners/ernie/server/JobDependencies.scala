package com.ksmpartners.ernie.server

import net.liftweb.common.{Box, Full, Empty}
import net.liftweb.http.{LiftResponse, PlainTextResponse, OkResponse}
import com.ksmpartners.ernie.engine.{StatusRequest => SReq, Coordinator, ReportGenerator, ReportRequest => RReq}
import com.ksmpartners.ernie.model.{Notification, ReportRequest}
import com.fasterxml.jackson.databind.ObjectMapper

trait JobDependencies {

  val reportGenerator = new ReportGenerator(".", ".") // TODO: Make directories a configuration
  val coordinator = new Coordinator(reportGenerator).start()

  class JobsResource extends JsonTranslator {
    def get = {
      Full(OkResponse())
    }
    def put(body: Box[Array[Byte]]) = {
      val obj = deserialize(body.open_!, classOf[ReportRequest])
      val response = (coordinator !! RReq(obj.getReportDefId))
      getJsonResponse(response.apply().asInstanceOf[Notification])
    }
  }

  class JobStatusResource extends JsonTranslator {
    def get(jobId: String) = {
      val response = (coordinator !! SReq(jobId.toInt))
      getJsonResponse(response.apply().asInstanceOf[Notification])
    }
  }

  class JobResultsResource {
    def get(jobId: String) = {
      Full(OkResponse())
    }
  }

}

trait JsonTranslator {
  private val MAPPER = new ObjectMapper

  def serialize[T](obj: T): String = {
    MAPPER.writeValueAsString(obj)
  }

  def deserialize[T](json: String, clazz: Class[T]): T = {
    MAPPER.readValue(json, clazz) match {
      case t: T => t
      case _ => throw new ClassCastException
    }
  }

  def deserialize[T](json: Array[Byte], clazz: Class[T]): T = {
    MAPPER.readValue(json, clazz) match {
      case t: T => t
      case _ => throw new ClassCastException
    }
  }

  def getJsonResponse[T](response: T): Box[LiftResponse] = {
    Full(PlainTextResponse(serialize(response), List(("Content-Type", "application/json")), 200))
  }
}