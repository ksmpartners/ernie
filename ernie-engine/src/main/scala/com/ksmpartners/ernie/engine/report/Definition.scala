/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package com.ksmpartners.ernie.engine.report

import com.ksmpartners.ernie.model.{ ReportType, DefinitionEntity }
import org.joda.time.DateTime

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

  /**
   * Get a mutable DefinitionEntity, a representation of the definition that is serializable and used for persistence.
   */
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
