/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.engine.report

import java.io._
import com.ksmpartners.ernie.model.ReportType

/**
 * Trait that contains methods for generating reports.
 */
trait ReportGenerator {

  /**
   * Method to be called before any reports can be generated
   */
  def startup()

  /**
   * Get the list of available definitions
   */
  def getAvailableRptDefs: List[String]

  /**
   * Run the given defId and store the output in rptId as rptType
   */
  def runReport(defId: String, rptId: String, rptType: ReportType, retentionDate: Option[Int], reportParameters: scala.collection.Map[String, String]): Unit
  def runReport(defId: String, rptId: String, rptType: ReportType, retentionDate: Option[Int]): Unit

  /**
   * Run the given defInputStream and store the output in rptOutputStream as rptType
   */
  //def runReport(defInputStream: InputStream, rptOutputStream: OutputStream, rptType: ReportType)

  /**
   * Method to be called after all the reports have been run.
   */
  def shutdown()

}
