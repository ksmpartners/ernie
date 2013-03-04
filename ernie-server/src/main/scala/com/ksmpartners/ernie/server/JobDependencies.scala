package com.ksmpartners.ernie.server

import net.liftweb.common.{Box, Full, Empty}
import net.liftweb.http.{BadResponse, LiftResponse, PlainTextResponse, OkResponse}
import com.ksmpartners.ernie.engine.{StatusRequest => SReq, Coordinator, ReportGenerator, ReportRequest => RReq}
import com.ksmpartners.ernie.model.{Notification, ReportRequest}
import com.fasterxml.jackson.databind.ObjectMapper
import net.liftweb.util.Props
import java.io.IOException

trait JobDependencies {

  val reportGenerator = new ReportGenerator(Props.get("rpt.def.dir").open_!, Props.get("output.dir").open_!)
  val coordinator = new Coordinator(reportGenerator).start()

  class JobsResource extends JsonTranslator {
    def get = {
      Full(OkResponse())
    }
    def put(body: Box[Array[Byte]]) = {
      var req: ReportRequest = null
      try {
        req = deserialize(body.open_!, classOf[ReportRequest])
        val response = (coordinator !! RReq(req.getReportDefId))
        getJsonResponse(response.apply().asInstanceOf[Notification])
      } catch {
        case e: IOException => Full(BadResponse())
      }
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

  class ShutdownResource {
    def shutdown() {
      reportGenerator.shutdown
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