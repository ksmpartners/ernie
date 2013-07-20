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
 *
 */

package com.ksmpartners.ernie.api

import com.ksmpartners.ernie.engine.report.{ ReportGenerator, ReportManager, ReportGeneratorFactory }
import com.ksmpartners.ernie.model
import scala.collection.mutable
import com.ksmpartners.ernie.util.Utility._
import java.io.{ OutputStream, InputStream }

trait TestReportGeneratorFactory extends ReportGeneratorFactory {

  def getReportGenerator(reportManager: com.ksmpartners.ernie.engine.report.ReportManager): ReportGenerator = {
    new TestReportGenerator(reportManager)
  }

}

class TestReportGenerator(reportManager: com.ksmpartners.ernie.engine.report.ReportManager) extends ReportGenerator {
  import com.ksmpartners.ernie.engine.report.ReportManager
  protected var running = false

  def startup() {
    if (!running)
      running = true
  }

  def getAvailableRptDefs: List[String] = {
    if (!running)
      throw new IllegalStateException("ReportGenerator is not started")
    List("def_1")
  }

  def runReport(defId: String, rptId: String, rptType: model.ReportType, retentionDays: Option[Int], userName: String) = runReport(defId, rptId, rptType, retentionDays, Map.empty[String, String], userName)
  def runReport(defId: String, rptId: String, rptType: model.ReportType, retentionDays: Option[Int], reportParameters: scala.collection.Map[String, String], userName: String) {
    if (!running)
      throw new IllegalStateException("ReportGenerator is not started")
    var entity = new mutable.HashMap[String, Any]()
    entity += (ReportManager.rptId -> rptId)
    entity += (ReportManager.sourceDefId -> "def")
    entity += (ReportManager.reportType -> rptType)
    entity += (ReportManager.createdUser -> userName)
    try_(reportManager.putReport(entity)) { os =>
      os.write(rptId.getBytes)
    }
  }

  def runReport(defInputStream: InputStream, rptOutputStream: OutputStream, rptType: model.ReportType) {
    if (!running)
      throw new IllegalStateException("ReportGenerator is not started")
  }

  def shutdown() {
    if (running)
      running = false
  }
}
