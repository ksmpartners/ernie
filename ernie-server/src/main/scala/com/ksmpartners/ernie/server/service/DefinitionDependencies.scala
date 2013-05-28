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
import com.ksmpartners.ernie.model.{ DeleteStatus, DefinitionEntity, JobStatus }
import java.io.{ ByteArrayInputStream, IOException }
import scala.collection.mutable
import com.ksmpartners.ernie.engine.report.{ BirtReportGenerator, ReportManager }
import net.liftweb.http.BadResponse
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

      if (!req.headers.contains(("Content-Type", "application/rptdesign+xml"))) Full(ResponseWithReason(BadResponse(), "Unacceptable Content-Type"))
      else if (req.headers.find(f => f._1 == "DefinitionEntity").isDefined) {
        try {
          val defEnt: DefinitionEntity = deserialize(req.header("DefinitionEntity").open_!, classOf[DefinitionEntity]).asInstanceOf[DefinitionEntity]
          if (!BirtReportGenerator.isValidDefinition(new ByteArrayInputStream(req.body.open_!))) Full(BadResponse())
          else if (reportManager.getAllDefinitionIds.contains(defEnt.getDefId)) {
            Full(ConflictResponse())
          } else {
            reportManager.putDefinition(defEnt).write(req.body.open_!)
            getJsonResponse(defEnt, 201, List(("Location", req.hostAndPath + "/defs/" + defEnt.getDefId)))
          }
        } catch {
          case e: Exception => Full(ResponseWithReason(BadResponse(), "Malformed DefinitionEntity header"))
        }
      } else Full(ResponseWithReason(BadResponse(), "No DefinitionEntity header"))
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
        Full(BadResponse())
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
      val response = (coordinator !? engine.DeleteDefinitionRequest(defId)).asInstanceOf[engine.DeleteDefinitionResponse]
      if (response.deleteStatus == DeleteStatus.SUCCESS) getJsonResponse(new model.DeleteDefinitionResponse(response.deleteStatus))
      else if (response.deleteStatus == DeleteStatus.NOT_FOUND) Full(NotFoundResponse("Definition not found"))
      else Full(BadResponse())
    }
    def put(defId: String, req: net.liftweb.http.Req) = {

      if (!req.headers.contains(("Content-Type", "application/rptdesign+xml"))) Full(ResponseWithReason(BadResponse(), "Unacceptable Content-Type"))
      else if (req.headers.find(f => f._1 == "DefinitionEntity").isDefined) {
        try {
          val defEnt: DefinitionEntity = deserialize(req.header("DefinitionEntity").open_!, classOf[DefinitionEntity]).asInstanceOf[DefinitionEntity]
          val bAIS = new ByteArrayInputStream(req.body.open_!)
          if (!BirtReportGenerator.isValidDefinition(bAIS)) Full(BadResponse())
          else if (!reportManager.getAllDefinitionIds.contains(defEnt.getDefId)) Full(NotFoundResponse())
          else {
            reportManager.updateDefinition(defId, defEnt).write(req.body.open_!)
            getJsonResponse(defEnt, 201)
          }
        } catch {
          case e: Exception => Full(ResponseWithReason(BadResponse(), "Malformed DefinitionEntity header"))
        }
      } else Full(ResponseWithReason(BadResponse(), "No DefinitionEntity header"))
    }
  }

  case class ConflictResponse() extends LiftResponse with HeaderDefaults {
    def toResponse = InMemoryResponse(Array(), headers, cookies, 409)
  }

  case class DefinitionCreatedResponse() extends LiftResponse with HeaderDefaults {
    def toResponse = InMemoryResponse(Array(), headers, cookies, 201)
  }
}
