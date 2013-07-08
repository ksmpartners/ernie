/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package com.ksmpartners.ernie.engine.report

import java.io._
import scala.collection._
import com.ksmpartners.ernie.model.{ ReportEntity, DefinitionEntity }
import com.ksmpartners.ernie.engine.report.ReportManager._
import org.slf4j.{ LoggerFactory, Logger }

/**
 * Implementation of [[com.ksmpartners.ernie.engine.report.ReportManager]] that stores reports and definitions in memory
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
    definitions.get(defId).map(b => { new ByteArrayInputStream(b) })
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

  override def putDefinition(entity: Map[String, Any]): (DefinitionEntity, OutputStream) = putDefinition(Left(entity))

  override def putDefinition(entity: DefinitionEntity): (DefinitionEntity, OutputStream) = {
    //  if ((entity.getDefId == null) || (entity.getDefId.length <= 0))
    //    throw new IllegalArgumentException("Entity must contain defId")
    if ((entity.getCreatedUser == null) || (entity.getCreatedUser.length <= 0))
      throw new IllegalArgumentException("Entity must contain createdUser")
    putDefinition(Right(entity))
  }

  override def putDefinition(entityEither: Either[Map[String, Any], DefinitionEntity]): (DefinitionEntity, OutputStream) = {
    val entity = if (entityEither.isLeft) createDefinitionEntity(entityEither.left.get) else entityEither.right.get
    if (entity.getDefDescription != null) entity.setDefDescription(entity.getDefDescription.trim())
    val defId = generateDefId.toString
    entity.setDefId(defId)
    (entity, new LocalBOS(defId, { (id, content) =>
      putDefinition(id, content, entity)
    }))
  }

  override def updateDefinition(defId: String, entity: Either[Map[String, Any], DefinitionEntity], entityOnly: Boolean): OutputStream = {
    val definitionEntity = if (entity.isLeft) createDefinitionEntity(entity.left.get) else entity.right.get
    if (definitionEntity.getDefDescription != null) definitionEntity.setDefDescription(definitionEntity.getDefDescription.trim())
    if (entityOnly) {
      definitionEntities += (defId -> definitionEntity)
      null
    } else new LocalBOS(definitionEntity.getDefId, { (id, content) =>
      {
        putDefinition(id, content, definitionEntity)
      }
    })
  }

  override def updateDefinition(defId: String, entity: Map[String, Any]): OutputStream = updateDefinition(defId, Left(entity), false)

  override def updateDefinitionEntity(defId: String, entity: Map[String, Any]) {
    updateDefinition(defId, Left(entity), true)
  }

  override def updateDefinition(defId: String, entity: DefinitionEntity): OutputStream = updateDefinition(defId, Right(entity), false)

  override def updateDefinitionEntity(defId: String, entity: DefinitionEntity) {
    updateDefinition(defId, Right(entity), true)
  }
  override def putReport(entity: Map[String, Any]): OutputStream = {
    val rptEntity = createReportEntity(entity)
    new LocalBOS(rptEntity.getRptId, { (id, content) =>
      putReport(id, content, rptEntity)
    })
  }

  override def updateReportEntity(entity: Map[String, Any]): ReportEntity = {
    val rptEntity = createReportEntity(entity)
    reportEntities += (rptEntity.getRptId -> rptEntity)
    rptEntity
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