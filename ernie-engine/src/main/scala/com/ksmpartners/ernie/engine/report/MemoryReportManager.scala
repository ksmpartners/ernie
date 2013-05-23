/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.engine.report

import java.io._
import scala.collection._
import com.ksmpartners.ernie.model.{ ReportEntity, DefinitionEntity }
import com.ksmpartners.ernie.engine.report.ReportManager._
import org.slf4j.{ LoggerFactory, Logger }

/**
 * Implementation of ReportManager that stores reports and definitions in memory
 */
class MemoryReportManager extends ReportManager {

  private val definitions: mutable.Map[String, Array[Byte]] = new mutable.HashMap()
  private val reports: mutable.Map[String, Array[Byte]] = new mutable.HashMap()

  private val definitionEntities: mutable.Map[String, DefinitionEntity] = new mutable.HashMap()
  private val reportEntities: mutable.Map[String, ReportEntity] = new mutable.HashMap()

  private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.engine.MemoryReportManager")

  override def getAllDefinitionIds: List[String] = {
    definitions.keys.toList
  }

  override def getAllReportIds: List[String] = {
    reports.keys.toList
  }

  override def hasDefinition(defId: String): Boolean = {
    definitions.contains(defId)
  }

  override def hasReport(rptId: String): Boolean = {
    reports.contains(rptId)
  }

  override def getDefinition(defId: String): Option[Definition] = {
    definitionEntities.get(defId).map({ ent => new Definition(ent) })
  }

  override def getDefinitionContent(defId: String): Option[InputStream] = {
    definitions.get(defId).map({ new ByteArrayInputStream(_) })
  }

  override def getDefinitionContent(definition: Definition): Option[InputStream] = {
    getDefinitionContent(definition.getDefId)
  }

  override def getReport(rptId: String): Option[Report] = {
    reportEntities.get(rptId).map({ ent => new Report(ent) })
  }

  override def getReportContent(rptId: String): Option[InputStream] = {
    reports.get(rptId).map({ new ByteArrayInputStream(_) })
  }

  override def getReportContent(report: Report): Option[InputStream] = {
    getReportContent(report.getRptId)
  }

  override def putDefinition(entity: Map[String, Any]): OutputStream = {
    val definitionEntity = createDefinitionEntity(entity)
    new LocalBOS(definitionEntity.getDefId, { (id, content) =>
      putDefinition(id, content, definitionEntity)
    })
  }

  override def putReport(entity: Map[String, Any]): OutputStream = {
    val rptEntity = createReportEntity(entity)
    new LocalBOS(rptEntity.getRptId, { (id, content) =>
      putReport(id, content, rptEntity)
    })
  }

  override def deleteDefinition(defId: String) {
    definitions -= defId
    definitionEntities -= defId
  }

  override def deleteReport(rptId: String) {
    reports -= rptId
    reportEntities -= rptId
  }

  private class LocalBOS(id: String, f: (String, Array[Byte]) => Unit) extends ByteArrayOutputStream {

    override def close() {
      super.close()
      f(id, this.toByteArray)
    }

  }

  /**
   * Puts the given content in memory and attaches it to defId.
   * If defId already exists, its content is replaced with the new content
   */
  def putDefinition(defId: String, content: Array[Byte], defEnt: DefinitionEntity) {
    definitionEntities += (defId -> defEnt)
    definitions += (defId -> content)
  }

  /**
   * Puts the given content in memory and attaches it to rptId.
   * If rptId already exists, its content is replaced with the new content
   */
  def putReport(rptId: String, content: Array[Byte], rptEnt: ReportEntity) {
    reportEntities += (rptId -> rptEnt)
    reports += (rptId -> content)
  }

  override def putDefaultRetentionDays(in: Int) { setDefaultRetentionDays(in) }
  override def putMaximumRetentionDays(in: Int) { setMaximumRetentionDays(in) }
  override def getDefaultRetentionDays: Int = ReportManager.getDefaultRetentionDays
  override def getMaximumRetentionDays: Int = ReportManager.getMaximumRetentionDays

}