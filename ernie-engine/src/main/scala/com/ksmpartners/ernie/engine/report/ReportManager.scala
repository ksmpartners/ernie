/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.engine.report

import java.io.{ OutputStream, InputStream }
import com.ksmpartners.ernie.model.ReportType

/**
 * Trait that contains methods for managing reports and definitions
 */
trait ReportManager {

  /**
   * Returns a list of available definition ids
   */
  def getAllDefinitionIds: List[String]
  /**
   * Returns a list of available report ids
   */
  def getAllReportIds: List[String]

  /**
   * Return true if the given definition exists
   */
  def hasDefinition(defId: String): Boolean
  /**
   * Return true if the given report exists
   */
  def hasReport(rptId: String): Boolean

  /**
   * Get an InputStream containing the content for definition defId
   */
  def getDefinition(defId: String): Option[InputStream]
  /**
   * Get an InputStream containing the content for report rptId
   */
  def getReport(rptId: String): Option[InputStream]

  /**
   * Returns an OutputStream into which content can be put. This content will be attached to defId.
   * If defId already exists, its content is replaced with the new content
   */
  def putDefinition(defId: String): OutputStream
  /**
   * Returns an OutputStream into which content can be put. This content will be attached to rptId
   * with the type of rptType.
   * If rptId already exists, its content is replaced with the new content
   */
  def putReport(rptId: String, rptType: ReportType): OutputStream

  /**
   * Deletes the given definition
   */
  def deleteDefinition(defId: String)
  /**
   * Deletes the given report
   */
  def deleteReport(rptId: String)

}
