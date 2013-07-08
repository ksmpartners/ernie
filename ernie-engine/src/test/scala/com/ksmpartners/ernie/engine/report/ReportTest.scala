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

import org.testng.annotations.Test
import com.ksmpartners.ernie.model.{ ReportType, ParameterEntity, ReportEntity }
import org.joda.time.DateTime
import java.util
import org.testng.Assert

class ReportTest {

  var report: Report = null
  var reportEntity: ReportEntity = null

  @Test
  def canCreateReport() {
    reportEntity = new ReportEntity()
    reportEntity.setCreatedDate(DateTime.now)
    reportEntity.setCreatedUser("default")
    reportEntity.setFinishDate(DateTime.now)
    reportEntity.setRetentionDate(DateTime.now.plusDays(5))
    reportEntity.setRptId("test_rpt")
    reportEntity.setSourceDefId("test_def")
    reportEntity.setReportType(ReportType.CSV)

    val paramMap = new util.HashMap[String, String]()
    paramMap.put("val1", "100")
    reportEntity.setParams(paramMap)

    report = new Report(reportEntity)
  }

  @Test(dependsOnMethods = Array("canCreateReport"))
  def getReportEntityReturnsCopy() {
    Assert.assertEquals(report.getCreatedDate, reportEntity.getCreatedDate)
    Assert.assertEquals(report.getCreatedUser, reportEntity.getCreatedUser)
    Assert.assertEquals(report.getSourceDefId, reportEntity.getSourceDefId)
    Assert.assertEquals(report.getRptId, reportEntity.getRptId)
    Assert.assertEquals(report.getReportType, reportEntity.getReportType)
    Assert.assertNotSame(report.getEntity, reportEntity)
  }

  @Test(dependsOnMethods = Array("canCreateReport"))
  def canGetParams() {
    Assert.assertEquals(report.getParams.size, 1)
  }

  @Test
  def nullListsReturnEmptyListsInstead() {
    val rpt = new Report(new ReportEntity())
    Assert.assertNotNull(rpt.getParams)
  }

}
