/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.engine.report

import collection.mutable
import java.io._
import org.slf4j.{ LoggerFactory, Logger }
import com.ksmpartners.ernie.model.ReportType

/**
 * Implementation of ReportManager that stores and loads reports and definitions from the filesystem
 */
class FileReportManager(pathToDefinitions: String, pathToOutputs: String) extends ReportManager {

  private val log: Logger = LoggerFactory.getLogger(this.getClass)

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
    rptDefDir.listFiles().filter({ _.isFile }).foreach({ file =>
      newMap += (file.getName.replaceFirst("[.][^.]+$", "") -> file)
    })
    newMap
  }

  private lazy val reports: mutable.Map[String, File] = {
    val newMap = new mutable.HashMap[String, File]()
    outputDir.listFiles().filter({ _.isFile }).foreach({ file =>
      newMap += (file.getName.replaceFirst("[.][^.]+$", "") -> file)
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

  override def getDefinition(defId: String): Option[InputStream] = {
    definitions.get(defId).map({ new FileInputStream(_) })
  }

  override def getReport(rptId: String): Option[InputStream] = {
    reports.get(rptId).map({ new FileInputStream(_) })
  }

  override def putDefinition(defId: String): OutputStream = {
    val file = new File(rptDefDir, defId + ".rptdesign")
    log.info("Putting new definition: {}", file)
    definitions += (defId -> file)
    new FileOutputStream(file)
  }

  override def putReport(rptId: String, rptType: ReportType): OutputStream = {
    val ext = rptType match {
      case ReportType.CSV => ".csv"
      case ReportType.HTML => ".html"
      case ReportType.PDF => ".pdf"
    }
    val file = new File(outputDir, rptId + ext)
    log.info("Putting new report: {}", file)
    reports += (rptId -> file)
    new FileOutputStream(file)
  }

  override def deleteDefinition(defId: String) {
    log.info("Deleting definition file {}", defId)
    if (definitions.contains(defId)) {
      val file = definitions.get(defId).get
      if (file.delete()) {
        log.info("Definition file {} was deleted successfully.", defId)
        definitions -= defId
      } else {
        log.warn("Definition file {} did not delete successfully.", defId)
      }
    } else {
      log.warn("Definition file {} does not exist, skipping delete.", defId)
    }
  }

  override def deleteReport(rptId: String) {
    log.info("Deleting report file {}", rptId)
    if (reports.contains(rptId)) {
      val file = reports.get(rptId).get
      if (file.delete()) {
        log.info("Report file {} was deleted successfully.", rptId)
        reports -= rptId
      } else {
        log.warn("Report file {} did not delete successfully.", rptId)
      }
    } else {
      log.warn("Report file {} does not exist, skipping delete.", rptId)
    }
  }

}
