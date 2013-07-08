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

import com.ksmpartners.ernie.model.ReportType

/**
 * Trait that contains methods for generating reports.
 */
trait ReportGenerator {

  protected var running: Boolean

  /**
   * Method to be called before any reports can be generated
   */
  def startup()

  /**
   * Get the list of available definitions
   * @return definition IDs available
   */
  def getAvailableRptDefs: List[String]

  /**
   * Run the given defId and store the output in rptId as rptType
   * @param defId the definition ID to use in report generation
   * @param rptId the report ID under which generated output should be stored
   * @param rptType the output format
   * @param retentionDate an integer indicating the number of days until the report output expires
   * @param reportParameters a set of BIRT Report Parameters corresponding to the parameters specified in the report definition.
   * @param userName an identifier for the user who initiated the job
   */
  def runReport(defId: String, rptId: String, rptType: ReportType, retentionDate: Option[Int], reportParameters: scala.collection.Map[String, String], userName: String): Unit
  def runReport(defId: String, rptId: String, rptType: ReportType, retentionDate: Option[Int], userName: String): Unit

  /**
   * Method to be called after all the reports have been run.
   */
  def shutdown()

}
