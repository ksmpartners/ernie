/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.engine.report

import com.ksmpartners.ernie.model.DefinitionEntity
import java.util.Date

/**
 * Immutable wrapper class for sharing DefinitionEntity data
 */
class Definition protected[report] (defEntity: DefinitionEntity) {

  private lazy val paramNames: Array[String] = {
    val jParamNames = defEntity.getParamNames
    if (jParamNames == null) new Array(0) else jParamNames.toArray.map({ _.toString })
  }

  def getCreatedDate: Date = defEntity.getCreatedDate

  def getDefId: String = defEntity.getDefId

  def getCreatedUser: String = defEntity.getCreatedUser

  def getParamNames: Array[String] = paramNames

  def getDefDescription: String = defEntity.getDefDescription

}
