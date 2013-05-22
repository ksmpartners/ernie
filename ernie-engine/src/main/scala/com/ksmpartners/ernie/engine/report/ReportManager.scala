/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.engine.report

import java.io.{ OutputStream, InputStream }
import scala.collection._
import com.ksmpartners.ernie.model.{ ReportType, ReportEntity, DefinitionEntity }
import com.ksmpartners.ernie.engine.report.ReportManager._
import java.util
import org.joda.time.DateTime

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
   * Get a Definition object whose ID is defId
   */
  def getDefinition(defId: String): Option[Definition]
  /**
   * Get an InputStream containing the content for defId
   */
  def getDefinitionContent(defId: String): Option[InputStream]
  /**
   * Get an InputStream containing the content for definition
   */
  def getDefinitionContent(definition: Definition): Option[InputStream]
  /**
   * Get a Report object whose ID is rptId
   */
  def getReport(rptId: String): Option[Report]
  /**
   * Get an InputStream containing the content for rptId
   */
  def getReportContent(rptId: String): Option[InputStream]
  /**
   * Get an InputStream containing the content for report
   */
  def getReportContent(report: Report): Option[InputStream]

  /**
   * Returns an OutputStream into which content can be put. The entity must contain information about the
   * definition being added. Required fields are: DEF_ID and CREATED_USER. Optional fields are: PARAM_NAMES
   * and DESCRIPTION.
   * If DEF_ID already exists, the definition content will be replaced with the new content
   */
  def putDefinition(entity: Map[String, Any]): OutputStream
  /**
   * Returns an OutputStream into which content can be put. The entity must contain information about the
   * definition being added. Required fields are: RPT_ID, SOURCE_DEF_ID, REPORT_TYPE, and CREATED_USER. Optional fields
   * are: PARAM_MAP and RETENTION_DATE.
   * If RPT_ID already exists, the definition content will be replaced with the new content
   */
  def putReport(entity: Map[String, Any]): OutputStream

  /**
   * Deletes the given definition
   */
  def deleteDefinition(defId: String)
  /**
   * Deletes the given report
   */
  def deleteReport(rptId: String)

  /**
   * Returns a DefinitionEntity object containing the contents of entity. The entity must contain information about the
   * definition being added.
   * Required fields are:
   * - DEF_ID (String)
   * - CREATED_USER (String)
   *
   * Optional fields are:
   * - PARAM_NAMES (List[String])
   * - DESCRIPTION (String)
   */
  protected def createDefinitionEntity(entity: Map[String, Any]): DefinitionEntity = {
    if (!entity.contains(DEF_ID))
      throw new IllegalArgumentException("Entity must contain DEF_ID")
    if (!entity.contains(CREATED_USER))
      throw new IllegalArgumentException("Entity must contain CREATED_USER")

    val defEnt = new DefinitionEntity()
    defEnt.setCreatedDate(DateTime.now())
    defEnt.setDefId(entity.get(DEF_ID).get.asInstanceOf[String])
    defEnt.setCreatedUser(entity.get(CREATED_USER).get.asInstanceOf[String])
    defEnt.setDefDescription(entity.getOrElse(DESCRIPTION, "").asInstanceOf[String])
    if (entity.contains(PARAM_NAMES)) {
      val params = entity.get(PARAM_NAMES).get.asInstanceOf[List[String]]
      val paramList = new util.ArrayList[String]()
      for (param <- params) {
        paramList.add(param)
      }
      defEnt.setParamNames(paramList)
    }
    defEnt
  }

  /**
   * Returns a ReportEntity containing the contents of entity. The entity must contain information about the
   * definition being added.
   * Required fields are:
   * - RPT_ID (String)
   * - SOURCE_DEF_ID (String)
   * - REPORT_TYPE (ReportType)
   * - CREATED_USER (String)
   *
   * Optional fields are:
   * - PARAM_MAP (Map[String, String])
   * - RETENTION_DATE (DateTime)
   */
  protected def createReportEntity(entity: Map[String, Any]): ReportEntity = {
    if (!entity.contains(RPT_ID))
      throw new IllegalArgumentException("Entity must contain DEF_ID")
    if (!entity.contains(SOURCE_DEF_ID))
      throw new IllegalArgumentException("Entity must contain SOURCE_DEF_ID")
    if (!entity.contains(REPORT_TYPE))
      throw new IllegalArgumentException("Entity must contain REPORT_TYPE")
    if (!entity.contains(CREATED_USER))
      throw new IllegalArgumentException("Entity must contain CREATED_USER")

    val rptEnt = new ReportEntity()
    rptEnt.setCreatedDate(DateTime.now())
    rptEnt.setRptId(entity.get(RPT_ID).get.asInstanceOf[String])
    rptEnt.setSourceDefId(entity.get(SOURCE_DEF_ID).get.asInstanceOf[String])
    rptEnt.setReportType(entity.get(REPORT_TYPE).get.asInstanceOf[ReportType])
    rptEnt.setCreatedUser(entity.get(CREATED_USER).get.asInstanceOf[String])
    // TODO: Set up default retention date.
    rptEnt.setRetentionDate(entity.getOrElse(RETENTION_DATE, DateTime.now().plusDays(  )).asInstanceOf[DateTime])
    if (entity.contains(PARAM_MAP)) {
      val paramMap = entity.get(PARAM_MAP).get.asInstanceOf[Map[String, String]]
      val params: util.Map[String, String] = new util.HashMap()
      for (entry <- paramMap) {
        params.put(entry._1, entry._2)
      }
      rptEnt.setParams(params)
    }
    rptEnt
  }
}

/**
 * Companion Object containing constants for ReportManager
 */
object ReportManager {
  val DEF_ID = "defId"
  val RPT_ID = "rptId"
  val CREATED_DATE = "createdDate"
  val CREATED_USER = "createdUser"
  val PARAM_NAMES = "paramNames"
  val PARAM_MAP = "paramMap"
  val DESCRIPTION = "description"
  val RETENTION_DATE = "retentionDate"
  val REPORT_TYPE = "fileType"
  val SOURCE_DEF_ID = "sourceDefId"
}