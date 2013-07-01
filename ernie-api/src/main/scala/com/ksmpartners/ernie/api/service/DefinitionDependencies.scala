/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.api.service

import com.ksmpartners.ernie.{ engine, model }
import com.ksmpartners.ernie.model.{ ParameterEntity, DeleteStatus, DefinitionEntity }
import java.io.{ InputStream, ByteArrayInputStream }
import com.ksmpartners.ernie.engine.report.{ BirtReportGenerator }
import org.slf4j.{ LoggerFactory, Logger }
import com.ksmpartners.ernie.api._
import org.apache.cxf.helpers.{ FileUtils, IOUtils }
import scala.Some
import com.ksmpartners.ernie.util.Utility._
import com.ksmpartners.ernie.engine.DeleteDefinitionResponse
import akka.actor._
import ActorDSL._
import akka.pattern.ask
import scala.concurrent.Await
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit

/**
 * Dependencies for interacting with report definitions
 */
trait DefinitionDependencies extends RequiresReportManager with RequiresCoordinator {

  /**
   * Resource for handling HTTP requests at /defs
   */
  class DefsResource {
    private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.server.DefsResource")

    def putDefinition(defId: Option[String], rptDesign: Option[ByteArrayInputStream], definitionEntity: Option[DefinitionEntity]): DefinitionEntity = {
      defId.map(f => if (!reportManager.getDefinition(f).isDefined) throw new NotFoundException(f + " not found"))
      if (!(definitionEntity.isDefined || rptDesign.isDefined)) throw new MissingArgumentException("Must specify at least a definition entity or design")
      var defEnt = definitionEntity.getOrElse(new DefinitionEntity)
      if (rptDesign.isDefined) {
        if (rptDesign.get == null) throw new InvalidDefinitionException("Definition null")
        if (!BirtReportGenerator.isValidDefinition(rptDesign.get))
          throw new InvalidDefinitionException("Definition invalid")
        else try {
          rptDesign.get.reset
          val designXml = scala.xml.XML.load(rptDesign.get)

          var paramList: java.util.List[ParameterEntity] = if (defEnt.getParams == null) new java.util.ArrayList[ParameterEntity]() else defEnt.getParams

          (designXml \\ "parameters").foreach(f => f.child.foreach(g => {
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
        } catch {
          case e: Exception => throw InvalidDefinitionException("Malformed report design while extracting parameters: " + e.getMessage)
        }
      }
      if (!defId.isDefined) {
        val (defEntRes: DefinitionEntity, stream: java.io.OutputStream) = reportManager.putDefinition(defEnt)
        defEnt = defEntRes
        rptDesign.map(r => { r.reset; IOUtils.copy(r, stream) })
        stream.close
        defEnt
      } else {
        defEnt.setDefId(defId.get)
        val result = reportManager.updateDefinition(defId.get, defEnt)
        rptDesign.map(r => { r.reset; IOUtils.copy(r, result) })
        result.close
        defEnt
      }
    }

    def getList(): List[String] = reportManager.getAllDefinitionIds

    def getCatalog() = {
      reportManager.getAllDefinitionIds.foldLeft[List[DefinitionEntity]](Nil)((list, dId) =>
        list ::: (reportManager.getDefinition(dId).map(f => f.getEntity).toList))
    }

    def getDefinition(defId: String): Option[DefinitionEntity] = reportManager.getDefinition(defId).map(de => de.getEntity)
    def getDefinitionEntity(defId: String): Option[DefinitionEntity] = reportManager.getDefinition(defId).map(de => de.getEntity)
    def getDefinitionDesign(defId: String): Option[InputStream] = reportManager.getDefinitionContent(defId)

    /* private def getDefinition(defId: String, entity: Boolean, design: Boolean): Definition = {
      if (defId == null) throw new MissingArgumentException("Definition ID null")
      else {
        Definition(if (entity) reportManager.getDefinition(defId).map(f => f.getEntity) else None,
          if (design) reportManager.getDefinitionContent(defId).map(f => org.apache.commons.io.IOUtils.toByteArray(f)) else None,
          None)
      }
    }     */

    def deleteDefinition(defId: String): DeleteStatus = {
      if (defId == null) throw new MissingArgumentException("Definition ID null")
      Await.result((coordinator ? (engine.DeleteDefinitionRequest(defId))).mapTo[DeleteDefinitionResponse], timeoutDuration).deleteStatus
    }
  }
}
