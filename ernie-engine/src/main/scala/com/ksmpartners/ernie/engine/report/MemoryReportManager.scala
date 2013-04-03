/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.engine.report

import java.io._
import scala.collection._
import com.ksmpartners.ernie.model.ReportType

/**
 * Implementation of ReportManager that stores reports and definitions in memory
 */
class MemoryReportManager extends ReportManager {

  private val definitions: mutable.Map[String, Array[Byte]] = new mutable.HashMap()
  private val reports: mutable.Map[String, Array[Byte]] = new mutable.HashMap()

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

  override def getDefinition(defId: String): InputStream = {
    if (!definitions.contains(defId))
      throw new IOException("Definition does not exist for defId " + defId)
    new ByteArrayInputStream(definitions.get(defId).get)
  }

  override def getReport(rptId: String): InputStream = {
    if (!reports.contains(rptId))
      throw new IOException("Report does not exist for rptId " + rptId)
    new ByteArrayInputStream(reports.get(rptId).get)
  }

  override def putDefinition(defId: String): OutputStream = {
    new LocalBOS(defId, { (id, content) =>
      putDefinition(id, content)
    })
  }

  override def putReport(rptId: String, rptType: ReportType): OutputStream = {
    new LocalBOS(rptId, { (id, content) =>
      putReport(id, content)
    })
  }

  override def deleteDefinition(defId: String) {
    definitions -= defId
  }

  override def deleteReport(rptId: String) {
    reports -= rptId
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
  def putDefinition(defId: String, content: Array[Byte]) {
    definitions += (defId -> content)
  }

  /**
   * Puts the given content in memory and attaches it to rptId.
   * If rptId already exists, its content is replaced with the new content
   */
  def putReport(rptId: String, content: Array[Byte]) {
    reports += (rptId -> content)
  }

}