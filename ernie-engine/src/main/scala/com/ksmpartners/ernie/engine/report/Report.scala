/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.engine.report

import com.ksmpartners.ernie.model.{ ReportType, ReportEntity }
import java.util.Date
import scala.collection._

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

  def getCreatedDate: Date = rptEntity.getCreatedDate

  def getRetentionDate: Date = rptEntity.getRetentionDate

  def getRptId: String = rptEntity.getRptId

  def getSourceDefId: String = rptEntity.getSourceDefId

  def getCreatedUser: String = rptEntity.getCreatedUser

  def getReportType: ReportType = rptEntity.getReportType

  def getParams: Map[String, String] = params

}
