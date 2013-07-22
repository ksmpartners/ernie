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
import org.slf4j.{ LoggerFactory, Logger }
import com.ksmpartners.ernie.model.{ ReportEntity, DefinitionEntity, ReportType }
import scala.collection._
import com.ksmpartners.ernie.util.Utility._
import com.ksmpartners.ernie.util.MapperUtility._
import com.ksmpartners.ernie.engine.report.ReportManager._
import com.ksmpartners.ernie.engine.report.FileReportManager._

/**
 * Implementation of [[com.ksmpartners.ernie.engine.report.ReportManager]] that stores and loads reports and definitions from the filesystem
 */
class FileReportManager(pathToDefinitions: String, pathToOutputs: String) extends ReportManager {

  private val rptDefDir = new File(pathToDefinitions)
  private val outputDir = new File(pathToOutputs)

  // Validate directories
  if (!(rptDefDir.isDirectory && rptDefDir.canRead) || !(outputDir.isDirectory && outputDir.canWrite)) {
    throw new IOException("Input/output directories do not exist or do not have the correct read/write access. " +
      "Def Dir: " + rptDefDir +
      ". Output Dir: " + outputDir)
  }

  private lazy val definitions: mutable.Map[String, File] = {
    val newMap = new mutable.HashMap[String, File]()
    rptDefDir.listFiles().filter({ _.isFile }).filter({ !_.getName.endsWith("entity") }).foreach({ file =>
      newMap += (file.getName.replaceFirst("[.][^.]+$", "") -> file)
    })
    newMap
  }

  private lazy val reports: mutable.Map[String, File] = {
    val newMap = new mutable.HashMap[String, File]()
    outputDir.listFiles().filter({ _.isFile }).filter({ !_.getName.endsWith("entity") }).foreach({ file =>
      newMap += (file.getName.replaceFirst("[.][^.]+$", "") -> file)
    })
    newMap
  }

  private lazy val definitionEntities: mutable.Map[String, DefinitionEntity] = {
    val newMap = new mutable.HashMap[String, DefinitionEntity]()
    rptDefDir.listFiles().filter({ _.isFile }).filter({ _.getName.endsWith("entity") }).foreach({ file =>
      newMap += (file.getName.replaceFirst("[.][^.]+$", "") -> mapper.readValue(file, classOf[DefinitionEntity]))
    })
    newMap
  }

  private lazy val reportEntities: mutable.Map[String, ReportEntity] = {
    val newMap = new mutable.HashMap[String, ReportEntity]()
    outputDir.listFiles().filter({ _.isFile }).filter({ _.getName.endsWith("entity") }).foreach({ file =>
      newMap += (file.getName.replaceFirst("[.][^.]+$", "") -> mapper.readValue(file, classOf[ReportEntity]))
    })
    newMap
  }

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
    definitions.get(defId).map({ new FileInputStream(_) })
  }

  override def getDefinitionContent(definition: Definition): Option[InputStream] = {
    getDefinitionContent(definition.getDefId)
  }

  override def getReport(rptId: String): Option[Report] = {
    reportEntities.get(rptId).map({ ent => new Report(ent) })
  }

  override def getReportContent(rptId: String): Option[InputStream] = {
    reports.get(rptId).map({ new FileInputStream(_) })
  }

  override def getReportContent(report: Report): Option[InputStream] = {
    getReportContent(report.getRptId)
  }

  override def putDefinition(entity: Map[String, Any]): (DefinitionEntity, OutputStream) = putDefinition(Left(entity))
  override def putDefinition(entity: DefinitionEntity): (DefinitionEntity, OutputStream) = {
    if ((entity.getCreatedUser == null) || (entity.getCreatedUser.length <= 0))
      throw new IllegalArgumentException("Entity must contain createdUser")
    putDefinition(Right(entity))
  }

  override def putDefinition(entityEither: Either[Map[String, Any], DefinitionEntity]): (DefinitionEntity, OutputStream) = {
    val entity = if (entityEither.isLeft) createDefinitionEntity(entityEither.left.get) else entityEither.right.get
    log.info("Putting definition from entity: {}", entity)
    val defEnt = entity
    val defId = generateDefId.toString
    defEnt.setDefId(defId)
    if (defEnt.getDefDescription != null) defEnt.setDefDescription(defEnt.getDefDescription.trim())
    val defEntFile = new File(rptDefDir, defId + ".entity")
    try_(new FileOutputStream(defEntFile)) { fos =>
      mapper.writeValue(fos, defEnt)
    }
    val file = new File(rptDefDir, defId + ".rptdesign")
    log.info("Putting new definition: {}", file)
    definitions += (defId -> file)
    definitionEntities += (defId -> defEnt)
    (defEnt, new FileOutputStream(file))
  }

  override def updateDefinition(defId: String, entity: Either[Map[String, Any], DefinitionEntity], entityOnly: Boolean): OutputStream = {
    log.info("Updating definition from entity: {}", entity)
    val defEnt = if (entity.isLeft) createDefinitionEntity(entity.left.get) else entity.right.get
    val defId = defEnt.getDefId
    if (defEnt.getDefDescription != null) defEnt.setDefDescription(defEnt.getDefDescription.trim())
    val defEntFile = new File(rptDefDir, defId + ".entity")
    defEntFile.setWritable(true, false)
    defEntFile.delete
    defEntFile.createNewFile

    try_(new FileOutputStream(defEntFile, false)) { fos =>
      mapper.writeValue(fos, defEnt)
    }

    definitionEntities += (defId -> defEnt)

    if (!entityOnly) {
      val file = new File(rptDefDir, defId + ".rptdesign")
      log.info("Updating definition: {}", file)
      definitions += (defId -> file)
      new FileOutputStream(file, false)
    } else null
  }

  override def updateDefinition(defId: String, entity: Map[String, Any]): OutputStream = updateDefinition(defId, Left(entity), false)

  override def updateDefinition(defId: String, entity: DefinitionEntity): OutputStream = updateDefinition(defId, Right(entity), false)

  override def updateDefinitionEntity(defId: String, entity: Map[String, Any]) {
    updateDefinition(defId, Left(entity), true)
  }

  override def updateDefinitionEntity(defId: String, entity: DefinitionEntity) {
    updateDefinition(defId, Right(entity), true)
  }

  override def putReport(entity: Map[String, Any]): OutputStream = {
    log.info("Putting report from entity: {}", entity)
    val rptEnt = createReportEntity(entity)
    val rptType = rptEnt.getReportType
    val rptId = rptEnt.getRptId
    val rptEntFile = new File(outputDir, rptId + ".entity")

    try_(new FileOutputStream(rptEntFile)) { fos =>
      mapper.writeValue(fos, rptEnt)
    }
    val ext = rptType match {
      case ReportType.CSV => ".csv"
      case ReportType.HTML => ".html"
      case ReportType.PDF => ".pdf"
    }
    val file = new File(outputDir, rptId + ext)
    log.info("Putting new report: {}", file)
    reports += (rptId -> file)
    reportEntities += (rptId -> rptEnt)
    new FileOutputStream(file)
  }

  override def updateReportEntity(entity: Map[String, Any]): ReportEntity = {
    log.info("Putting report from entity: {}", entity)
    val rptEnt = createReportEntity(entity)
    val rptType = rptEnt.getReportType
    val rptId = rptEnt.getRptId
    val rptEntFile = new File(outputDir, rptId + ".entity")

    try_(new FileOutputStream(rptEntFile)) { fos =>
      mapper.writeValue(fos, rptEnt)
    }
    rptEnt
  }

  override def deleteDefinition(defId: String) {
    log.info("Deleting definition file {}", defId)
    if (definitions.contains(defId)) {
      val file = definitions.get(defId).get
      file.setWritable(true, false)
      // if (file.delete()) {
      file.deleteOnExit
      log.info("Definition file {} was deleted successfully.", defId)
      definitions -= defId
      deleteDefinitionEntity(defId)
      //} else {
      //  log.warn("Definition file {} did not delete successfully.", defId)
      // }
    } else {
      log.warn("Definition file {} does not exist, skipping delete.", defId)
    }
  }

  private def deleteDefinitionEntity(defId: String) {
    val entFile = new File(rptDefDir, defId + ".entity")
    if (entFile.exists()) {
      entFile.setWritable(true, false)
      //  if (entFile.delete()) {
      entFile.deleteOnExit
      log.info("Definition entity file {} was deleted successfully.", defId)
      definitionEntities -= defId
      //   } else {
      //    log.warn("Definition entity file {} did not delete successfully.", defId)
      //  }
    } else {
      log.warn("Definition entity file {} does not exist, skipping delete.", defId)
    }
  }

  override def deleteReport(rptId: String) {
    log.info("Deleting report file {}", rptId)
    if (reports.contains(rptId)) {
      val file = reports.get(rptId).get
      file.setWritable(true, false)
      //  if (file.delete()) {
      file.deleteOnExit
      log.info("Report file {} was deleted successfully.", rptId)
      reports -= rptId
      deleteReportEntity(rptId)
      //  } else {
      // log.warn("Report file {} did not delete successfully.", rptId)
      // }
    } else {
      log.warn("Report file {} does not exist, skipping delete.", rptId)
    }
  }

  private def deleteReportEntity(rptId: String) {
    val entFile = new File(outputDir, rptId + ".entity")
    if (entFile.exists()) {
      entFile.setWritable(true, false)
      // if (entFile.delete()) {
      entFile.deleteOnExit
      log.info("Report entity file {} was deleted successfully.", rptId)
      reportEntities -= rptId
      //  } else {
      //    log.warn("Report entity file {} did not delete successfully.", rptId)
      //  }
    } else {
      log.warn("Report entity file {} does not exist, skipping delete.", rptId)
    }
  }

  override def putDefaultRetentionDays(in: Int) { ReportManager.setDefaultRetentionDays(in) }
  override def putMaximumRetentionDays(in: Int) { ReportManager.setMaximumRetentionDays(in) }
  override def getDefaultRetentionDays: Int = ReportManager.getDefaultRetentionDays
  override def getMaximumRetentionDays: Int = ReportManager.getMaximumRetentionDays

}

object FileReportManager {
  private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.engine.report.FileReportManager")
}
