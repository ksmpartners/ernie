/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.server.service

import com.ksmpartners.ernie.{ model, api }
import java.util
import net.liftweb.http._
import com.ksmpartners.ernie.server.{ DispatchRestAPI, JsonTranslator }
import com.ksmpartners.ernie.model.{ ParameterEntity, DeleteStatus, DefinitionEntity }
import java.io.ByteArrayInputStream
import com.ksmpartners.ernie.engine.report.{ Definition, BirtReportGenerator }
import net.liftweb.http.BadResponse
import net.liftweb.common.{ Box, Full }
import org.slf4j.{ LoggerFactory, Logger }
import com.ksmpartners.ernie.server.RestGenerator._
import com.ksmpartners.ernie.server.filter.AuthUtil
import com.ksmpartners.ernie.api.MissingArgumentException
import com.ksmpartners.ernie.server.DispatchRestAPI.TimeoutResponse

/**
 * Dependencies for interacting with report definitions
 */
trait DefinitionDependencies extends RequiresAPI {

  /**
   * Resource for handling HTTP requests at /defs
   */
  class DefsResource extends JsonTranslator {
    private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.server.DefsResource")
    val getDefsAction: Action = Action("getDefinition", get(_: Package), "Retrieve a mapping of definition IDs to URIs", "", "ReportDefinitionMapResponse",
      DispatchRestAPI.timeoutErnieError("Defs list"))
    def get(p: Package): Box[LiftResponse] = get("/defs")
    def get(uriPrefix: String) = {
      val (list, error) = ernie.getDefinitionList
      checkResponse(getDefsAction, error) or {
        val defMap: util.Map[String, String] = new util.HashMap
        list.foreach({ defId =>
          defMap.put(defId, uriPrefix + "/" + defId)
        })
        getJsonResponse(new model.ReportDefinitionMapResponse(defMap))
      }
    }

    val postDefAction = Action("postDefinition", post(_), "Post a DefinitionEntity", "", "byte",
      ErnieError(ResponseWithReason(BadResponse(), "No DefinitionEntity in request body"), None),
      ErnieError(ResponseWithReason(BadResponse(), "Malformed DefinitionEntity"), None),
      DispatchRestAPI.timeoutErnieError("Definition creation"))
    def post(p: Package): Box[LiftResponse] = post(p.req)
    def post(req: net.liftweb.http.Req): Box[LiftResponse] = {
      if (req.body.isEmpty) {
        log.info("Response: Bad Response. Reason: No DefinitionEntity in request body")
        Full(ResponseWithReason(BadResponse(), "No DefinitionEntity in request body"))
      } else try {
        var defEnt: DefinitionEntity = deserialize(req.body.open_!, classOf[DefinitionEntity]).asInstanceOf[DefinitionEntity]
        val resp = ernie.createDefinition(None, defEnt.getDefDescription, AuthUtil.getUserName(req))
        checkResponse(postDefAction, resp) or {
          if (resp.defEnt.isDefined)
            getJsonResponse(resp.defEnt.get, 201, List(("Location", req.hostAndPath + "/defs/" + resp.defEnt.get.getDefId)))
          else {
            log.info("Response: Internal server error.")
            Full(InternalServerErrorResponse())
          }
        }
      } catch {
        case e: Exception => {
          log.info("Caught exception while posting definition: {}", e.getMessage + "\n" + e.getStackTraceString)
          log.info("Response: Bad Response. Reason: Malformed DefinitionEntity")
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

    val getDefDetailAction: Action = Action("getDefinitionDetail", get(_), "Retrieve the DefinitionEntity for a specific Definition ID", "", "DefinitionEntity",
      ErnieError(NotFoundResponse(), Some(com.ksmpartners.ernie.api.NotFoundException("Definition ID not found"))),
      DispatchRestAPI.timeoutErnieError("Get definition"))
    def get(p: Package): Box[LiftResponse] = if (p.params.length != 1) Full(ResponseWithReason(BadResponse(), "Invalid def id")) else get(p.params(0).data.toString)
    def get(defId: String) = {
      val definition = ernie.getDefinitionEntity(defId)
      // val defEnt = reportManager.getDefinition(defId)
      checkResponse(getDefDetailAction, definition) or {
        if (definition.defEnt.isDefined) getJsonResponse(definition.defEnt.get)
        else {
          log.debug("Response: Not Found Response.")
          Full(NotFoundResponse())
        }
      }
    }

    val deleteDefAction: Action = Action("deleteDefinition", del(_: Package), "Deletes a specific definition", "", "DefinitionDeleteResponse",
      DispatchRestAPI.timeoutErnieError("Definition delete"),
      ErnieError(NotFoundResponse(), Some(api.NotFoundException("Definition not found"))),
      ErnieError(ResponseWithReason(ConflictResponse(), "Definition in use"), None),
      ErnieError(ResponseWithReason(BadResponse(), "Definition deletion failed"), None))
    def del(p: Package): Box[LiftResponse] = if (p.params.length != 1) Full(ResponseWithReason(BadResponse(), "Invalid job id")) else del(p.params(0).data.toString)
    def del(defId: String) = {
      val (delete, error) = ernie.deleteDefinition(defId)
      checkResponse(deleteDefAction, error) or {
        if (delete == DeleteStatus.SUCCESS) getJsonResponse(new model.DeleteDefinitionResponse(delete))
        else if (delete == DeleteStatus.NOT_FOUND) {
          log.debug("Response: Not Found Response.")
          Full(NotFoundResponse("Definition not found"))
        } else if (delete == DeleteStatus.FAILED_IN_USE) {
          log.debug("Response: Conflict Response")
          Full(ConflictResponse())
        } else {
          log.debug("Response: Bad Response. Reason: Definition deletion failed.")
          Full(ResponseWithReason(BadResponse(), "Definition deletion failed"))
        }
      }
    }

    val putDefAction: Action = Action("putDefinition", put(_), "Put definition rptdesign", "", "DefinitionEntity",
      ErnieError(ResponseWithReason(BadResponse(), "Unacceptable Content-Type"), None),
      ErnieError(ResponseWithReason(BadResponse(), "No report design in request body"), Some(MissingArgumentException("No report design in request body"))),
      ErnieError(NotFoundResponse(), Some(com.ksmpartners.ernie.api.NotFoundException("Definition not found"))),
      ErnieError(BadResponse(), Some(com.ksmpartners.ernie.api.InvalidDefinitionException("Unable to validate report design"))),
      ErnieError(InternalServerErrorResponse(), None))
    def put(p: Package): Box[LiftResponse] = if (p.params.length != 1) Full(ResponseWithReason(BadResponse(), "Invalid job id")) else put(p.params(0).data.toString, p.req)

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
        val resp = ernie.updateDefinition(defId, api.Definition(None, Some(req.body.open_!), None))
        checkResponse(putDefAction, resp) or {
          if (resp.defEnt.isEmpty) {
            log.debug("Response: Internal server error")
            Full(InternalServerErrorResponse())
          } else {
            getJsonResponse(resp.defEnt.get, 201)
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
