/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.engine.report

import com.ksmpartners.ernie.model.{ ReportType, ReportEntity }
import scala.collection._
import org.joda.time.DateTime

/**
 * Immutable wrapper class for sharing ReportEntity data
 */
class Report protected[report] (rptEntity: ReportEntity) {

  private lazy val params: Map[String, String] = {
    var paramsMap = new immutable.HashMap[String, String]
    val jParams = rptEntity.getParams
    if (jParams != null) {
      for (key <- jParams.keySet.toArray) {
        paramsMap += (key.toString -> jParams.get(key))
      }
    }
    paramsMap
  }

  private lazy val fileName = getRptId + "." + getReportType.toString.toLowerCase

  def getCreatedDate: DateTime = rptEntity.getCreatedDate

  def getRetentionDate: DateTime = rptEntity.getRetentionDate

  def getRptId: String = rptEntity.getRptId

  def getSourceDefId: String = rptEntity.getSourceDefId

  def getCreatedUser: String = rptEntity.getCreatedUser

  def getReportType: ReportType = rptEntity.getReportType

  def getParams: Map[String, String] = params

  def getStartDate: DateTime = rptEntity.getStartDate

  def getFinishDate: DateTime = rptEntity.getFinishDate

  def getReportName: String = fileName

  def getEntity: ReportEntity = {
    val rptEnt = new ReportEntity()
    rptEnt.setCreatedDate(rptEntity.getCreatedDate)
    rptEnt.setCreatedUser(rptEntity.getCreatedUser)
    rptEnt.setParams(rptEntity.getParams)
    rptEnt.setReportType(rptEntity.getReportType)
    rptEnt.setRetentionDate(rptEntity.getRetentionDate)
    rptEnt.setRptId(rptEntity.getRptId)
    rptEnt.setSourceDefId(rptEntity.getSourceDefId)
    rptEnt.setStartDate(rptEntity.getStartDate)
    rptEnt.setFinishDate(rptEntity.getFinishDate)
    rptEnt
  }

}