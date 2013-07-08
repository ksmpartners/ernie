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

  /**
   * Get a mutable ReportEntity, a representation of the report that is serializable and used for persistence.
   */
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
