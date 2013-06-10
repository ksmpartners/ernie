/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.engine.report

import java.io.{ OutputStream, InputStream }
import scala.collection._
import com.ksmpartners.ernie.model.{ ParameterEntity, ReportType, ReportEntity, DefinitionEntity }
import java.util
import org.joda.time.DateTime

/**
 * Trait that contains methods for managing reports and definitions
 */
trait ReportManager {

  /**
   * Return a list of available definition ids
   */
  def getAllDefinitionIds: List[String]
  /**
   * Return a list of available report ids
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
   * Return an OutputStream into which content can be put. The entity must contain information about the
   * definition being added. Required fields are: DEF_ID and CREATED_USER. Optional fields are: PARAM_NAMES
   * and DESCRIPTION.
   * If DEF_ID already exists, the definition content will be replaced with the new content
   */
  def putDefinition(entity: Map[String, Any]): OutputStream
  def putDefinition(entity: DefinitionEntity): OutputStream
  def putDefinition(entityEither: Either[Map[String, Any], DefinitionEntity]): OutputStream
  /**
   * Return an OutputStream into which content can be put. The entity must contain information about the
   * definition being added. Required fields are: RPT_ID, SOURCE_DEF_ID, REPORT_TYPE, and CREATED_USER. Optional fields
   * are: PARAM_MAP and RETENTION_DATE.
   * If RPT_ID already exists, the definition content will be replaced with the new content
   */
  def putReport(entity: Map[String, Any]): OutputStream
  /**
   * Return an OutputStream into which content can be put. The entity must contain information about the
   * definition being added. Required fields are: DEF_ID and CREATED_USER. Optional fields are: PARAM_NAMES
   * and DESCRIPTION.
   * If DEF_ID already exists, the definition content will be replaced with the new content
   */
  def updateDefinition(defId: String, entity: Map[String, Any]): OutputStream
  def updateDefinition(defId: String, entity: DefinitionEntity): OutputStream
  def updateDefinitionEntity(defId: String, entity: Map[String, Any])
  def updateDefinitionEntity(defId: String, entity: DefinitionEntity)
  def updateDefinition(defId: String, entity: Either[Map[String, Any], DefinitionEntity], entityOnly: Boolean): OutputStream

  /**
   * Delete the given definition
   */
  def deleteDefinition(defId: String)
  /**
   * Delete the given report
   */
  def deleteReport(rptId: String)

  /* Return the default number of days for report output retention */
  def getDefaultRetentionDays: Int
  /* Set the default number of days for report output retention */
  def putDefaultRetentionDays(in: Int)
  /* Return the maximum number of days for report output retention */
  def getMaximumRetentionDays: Int
  /* Set the maximum number of days for report output retention */
  def putMaximumRetentionDays(in: Int)

}

/**
 * Companion Object containing constants for ReportManager
 */
object ReportManager {
  val defId = "defId"
  val rptId = "rptId"
  val createdDate = "createdDate"
  val createdUser = "createdUser"
  val paramNames = "paramNames"
  val paramMap = "paramMap"
  val params = "params"
  val description = "description"
  val retentionDate = "retentionDate"
  val reportType = "fileType"
  val startDate = "startDate"
  val submitDate = "submitDate"
  val finishDate = "finishDate"
  val rptEntity = "rptEntity"
  val sourceDefId = "sourceDefId"
  val unsupportedReportTypes = "unsupportedReportTypes"

  private var defaultRetentionDays = 7
  private var maximumRetentionDays = 14

  def setDefaultRetentionDays(in: Int) { defaultRetentionDays = in }
  def setMaximumRetentionDays(in: Int) { maximumRetentionDays = in }
  def getDefaultRetentionDays: Int = defaultRetentionDays
  def getMaximumRetentionDays: Int = maximumRetentionDays

  /**
   * Return a DefinitionEntity object containing the contents of entity. The entity must contain information about the
   * definition being added.
   * Required fields are:
   * - DEF_ID (String)
   * - CREATED_USER (String)
   *
   * Optional fields are:
   * - PARAM_NAMES (List[String])
   * - DESCRIPTION (String)
   */
  def createDefinitionEntity(entity: Map[String, Any]): DefinitionEntity = {
    if (!entity.contains(defId))
      throw new IllegalArgumentException("Entity must contain defId")
    if (!entity.contains(createdUser))
      throw new IllegalArgumentException("Entity must contain createdUser")

    val defEnt = new DefinitionEntity()
    defEnt.setCreatedDate(DateTime.now())
    defEnt.setDefId(entity.get(defId).get.asInstanceOf[String])
    defEnt.setCreatedUser(entity.get(createdUser).get.asInstanceOf[String])
    defEnt.setDefDescription(entity.getOrElse(description, "").asInstanceOf[String])

    if (entity.contains(paramNames)) {
      val params = entity.get(paramNames).get.asInstanceOf[List[String]]
      val paramList = new util.ArrayList[String]()
      for (param <- params) {
        paramList.add(param)
      }
      defEnt.setParamNames(paramList)
    }

    if (entity.contains(params)) {
      val paramMap = entity.get(params).get.asInstanceOf[List[ParameterEntity]]
      val paramList = new util.ArrayList[ParameterEntity]()
      for (param <- paramMap) {
        paramList.add(param)
      }
      defEnt.setParams(paramList)
    }

    if (entity.contains(unsupportedReportTypes)) {
      val rptTypes = entity.get(unsupportedReportTypes).get.asInstanceOf[List[ReportType]]
      val rptTypeList = new util.ArrayList[ReportType]()
      for (rptType <- rptTypes) {
        rptTypeList.add(rptType)
      }
      defEnt.setUnsupportedReportTypes(rptTypeList)
    }

    defEnt
  }

  /**
   * Return a ReportEntity containing the contents of entity. The entity must contain information about the
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
  def createReportEntity(entity: Map[String, Any]): ReportEntity = {
    if (!entity.contains(rptId))
      throw new IllegalArgumentException("Entity must contain defId")
    if (!entity.contains(sourceDefId))
      throw new IllegalArgumentException("Entity must contain sourceDefId")
    if (!entity.contains(reportType))
      throw new IllegalArgumentException("Entity must contain reportType")
    if (!entity.contains(createdUser))
      throw new IllegalArgumentException("Entity must contain createdUser")

    val rptEnt = new ReportEntity()
    rptEnt.setCreatedDate(DateTime.now())
    rptEnt.setRptId(entity.get(rptId).get.asInstanceOf[String])
    rptEnt.setSourceDefId(entity.get(sourceDefId).get.asInstanceOf[String])
    rptEnt.setReportType(entity.get(reportType).get.asInstanceOf[ReportType])
    rptEnt.setCreatedUser(entity.get(createdUser).get.asInstanceOf[String])

    // Set up default retention date.
    val retentionDateOption = entity.get(retentionDate)
    if (retentionDateOption.isDefined) {
      val retentionDate: DateTime = DateTime.parse(retentionDateOption.get.toString)
      if (retentionDate isAfter DateTime.now().plusDays(maximumRetentionDays)) throw new RetentionDateAfterMaximumException("Retention date after maximum")
      else if (retentionDate.isBeforeNow() || retentionDate.equals(DateTime.now())) throw new RetentionDateInThePastException("Retention date is in the past")
      else rptEnt.setRetentionDate(retentionDate)
    } else rptEnt.setRetentionDate(DateTime.now().plusDays(defaultRetentionDays))

    if (entity.contains(paramMap)) {
      val paramMapObj = entity.get(paramMap).get.asInstanceOf[Map[String, String]]
      val params: util.Map[String, String] = new util.HashMap()
      for (entry <- paramMapObj) {
        params.put(entry._1, entry._2)
      }
      rptEnt.setParams(params)
    }
    rptEnt
  }

  case class RetentionDateAfterMaximumException(smth: String) extends RuntimeException
  case class RetentionDateInThePastException(smth: String) extends RuntimeException
}