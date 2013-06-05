/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.engine.report

import com.ksmpartners.ernie.model.{ ReportType, DefinitionEntity }
import org.joda.time.DateTime
import collection.JavaConversions._

/**
 * Immutable wrapper class for sharing DefinitionEntity data
 */
class Definition protected[report] (defEntity: DefinitionEntity) {

  private lazy val paramNames: Array[String] = {
    val jParamNames = defEntity.getParamNames
    if (jParamNames == null) new Array(0) else jParamNames.toArray.map({ _.toString })
  }

  private lazy val unsupportedReportTypes: Array[ReportType] = {
    val jUnsupportedReportTypes: java.util.List[ReportType] = defEntity.getUnsupportedReportTypes
    if (jUnsupportedReportTypes == null) new Array(0) else jUnsupportedReportTypes.toArray.map(f => f.asInstanceOf[ReportType])
  }

  def getCreatedDate: DateTime = defEntity.getCreatedDate

  def getDefId: String = defEntity.getDefId

  def getCreatedUser: String = defEntity.getCreatedUser

  def getParamNames: Array[String] = paramNames

  def getDefDescription: String = defEntity.getDefDescription

  def getUnsupportedReportTypes: Array[ReportType] = unsupportedReportTypes

  def getEntity: DefinitionEntity = {
    val defEnt = new DefinitionEntity()
    defEnt.setCreatedDate(defEntity.getCreatedDate)
    defEnt.setCreatedUser(defEntity.getCreatedUser)
    defEnt.setDefDescription(defEntity.getDefDescription)
    defEnt.setDefId(defEntity.getDefId)
    defEnt.setParamNames(defEntity.getParamNames)
    defEnt.setParams(defEntity.getParams)
    defEnt.setUnsupportedReportTypes(defEntity.getUnsupportedReportTypes)
    defEnt
  }

}
