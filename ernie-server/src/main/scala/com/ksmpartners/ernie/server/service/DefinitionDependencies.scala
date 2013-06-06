/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.server.service

import com.ksmpartners.ernie.{ engine, model }
import java.util
import net.liftweb.common.{ Box, Full }
import net.liftweb.http._
import com.ksmpartners.ernie.server.JsonTranslator
import com.ksmpartners.ernie.model.{ ParameterEntity, DeleteStatus, DefinitionEntity, JobStatus }
import java.io.{ ByteArrayInputStream, IOException }
import scala.collection.{ JavaConversions, mutable }
import com.ksmpartners.ernie.engine.report.{ Definition, BirtReportGenerator, ReportManager }
import net.liftweb.http.BadResponse
import scala.collection.JavaConversions._
import net.liftweb.common.Full
import org.slf4j.{ LoggerFactory, Logger }

/**
 * Dependencies for interacting with report definitions
 */
trait DefinitionDependencies extends RequiresReportManager with RequiresCoordinator {

  /**
   * Resource for handling HTTP requests at /defs
   */
  class DefsResource extends JsonTranslator {
    private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.server.DefsResource")
    def get(uriPrefix: String) = {
      val defMap: util.Map[String, String] = new util.HashMap
      reportManager.getAllDefinitionIds.foreach({ defId =>
        defMap.put(defId, uriPrefix + "/" + defId)
      })
      getJsonResponse(new model.ReportDefinitionMapResponse(defMap))
    }

    def post(req: net.liftweb.http.Req) = {

      if (req.body.isEmpty) {
        log.debug("Response: Bad Response. Reason: No DefinitionEntity in request body")
        Full(ResponseWithReason(BadResponse(), "No DefinitionEntity in request body"))
      } else try {
        val defEnt: DefinitionEntity = deserialize(req.body.open_!, classOf[DefinitionEntity]).asInstanceOf[DefinitionEntity]
        if (reportManager.getAllDefinitionIds.contains(defEnt.getDefId)) {
          log.debug("Response: Conflict Response.")
          Full(ConflictResponse())
        } else {
          reportManager.putDefinition(defEnt).write(req.body.open_!)
          getJsonResponse(defEnt, 201, List(("Location", req.hostAndPath + "/defs/" + defEnt.getDefId)))
        }
      } catch {

        case e: Exception => {
          log.debug("Response: Bad Response. Reason: Malformed DefinitionEntity")
          Full(ResponseWithReason(BadResponse(), "Malformed DefinitionEntity"))
        }

      }
    }
  }

  /**
   * Resource for handling HTTP requests at /defs/<DEF_ID>
   */
  class DefDetailResource extends JsonTranslator {
    private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.server.DefinitionDependencies")

    def get(defId: String) = {
      val defEnt = reportManager.getDefinition(defId)
      if (defEnt.isDefined) {
        val defEntity = defEnt.get.getEntity
        getJsonResponse(defEntity)
      } else {
        log.debug("Response: Not Found Response.")
        Full(NotFoundResponse())
      }
    }

    /* def del(defId: String) = try {
      reportManager.deleteDefinition(defId)
      Full(OkResponse())
    } catch {
      case e: Exception => {
        Full(BadResponse())
      }
    }*/
    def del(defId: String) = {

      val respOpt = (coordinator !? (timeout, engine.DeleteDefinitionRequest(defId))).asInstanceOf[Option[engine.DeleteDefinitionResponse]]
      if (respOpt.isEmpty) {
        log.debug("Response: Timeout Response.")
        Full(TimeoutResponse())
      } else {
        val response = respOpt.get
        if (response.deleteStatus == DeleteStatus.SUCCESS) getJsonResponse(new model.DeleteDefinitionResponse(response.deleteStatus))
        else if (response.deleteStatus == DeleteStatus.NOT_FOUND) {
          log.debug("Response: Not Found Response.")
          Full(NotFoundResponse("Definition not found"))
        } else if (response.deleteStatus == DeleteStatus.FAILED_IN_USE) {
          log.debug("Response: Conflict Response")
          Full(ConflictResponse())
        } else {
          log.debug("Response: Bad Response. Reason: Definition deletion failed.")
          Full(ResponseWithReason(BadResponse(), "Definition deletion failed"))
        }
      }
    }
    def put(defId: String, req: net.liftweb.http.Req) = {
      var ctype = ""
      req.headers.foreach({ tup =>
        if (tup._1.equalsIgnoreCase("Content-Type"))
          ctype = tup._2
      })

      if (!ctype.contains("application/rptdesign+xml")) {
        log.debug("Response: Bad Response. Reason: Unacceptable Content-Type")
        Full(ResponseWithReason(BadResponse(), "Unacceptable Content-Type"))
      } else if (req.body.isEmpty) {
        log.debug("Response: Bad Response. Reason: No report design in request body")
        Full(ResponseWithReason(BadResponse(), "No report design in request body"))
      } else {
        val defOpt: Option[Definition] = reportManager.getDefinition(defId)
        if (defOpt.isEmpty) {
          log.debug("Response: Not Found Response.")
          Full(NotFoundResponse("Definition not found"))
        } else {
          val defEnt = defOpt.get.getEntity
          try {
            val bAIS = new ByteArrayInputStream(req.body.open_!)

            if (!BirtReportGenerator.isValidDefinition(bAIS)) {
              log.debug("Response: Bad Response. Reason: Unable to validate report design")
              Full(ResponseWithReason(BadResponse(), "Unable to validate report design"))
            } else {
              val rptDesign = scala.xml.XML.load(new ByteArrayInputStream(req.body.open_!))

              var paramList: java.util.List[ParameterEntity] = if (defEnt.getParams == null) new java.util.ArrayList[ParameterEntity]() else defEnt.getParams

              (rptDesign \\ "parameters").foreach(f => f.child.foreach(g => {
                var param = new ParameterEntity()
                param.setParamName((g \ "@name").text)
                g.child.foreach(prop => (prop \ "@name").text match {
                  case "allowBlank" => param.setAllowNull(prop.text == "true")
                  case "dataType" => param.setDataType(prop.text)
                  case "defaultValue" => param.setDefaultValue(prop.text)
                  case _ =>
                })
                if ((param.getParamName != "") && (param.getDataType != "") && (param.getDefaultValue != "") && (param.getAllowNull != null)) paramList.add(param)
              }))

              defEnt.setParams(paramList)

              reportManager.updateDefinition(defId, defEnt).write(req.body.open_!)
              getJsonResponse(defEnt, 201)
            }
          } catch {
            case _ => {
              log.debug("Response: Bad Response. Reason: Malformed report design")
              Full(ResponseWithReason(BadResponse(), "Malformed report design"))
            }
          }
        }
      }
    }
  }

  case class DefinitionCreatedResponse() extends LiftResponse with HeaderDefaults {
    def toResponse = InMemoryResponse(Array(), headers, cookies, 201)
  }
}
case class ConflictResponse() extends LiftResponse with HeaderDefaults {
  def toResponse = InMemoryResponse(Array(), headers, cookies, 409)
}
