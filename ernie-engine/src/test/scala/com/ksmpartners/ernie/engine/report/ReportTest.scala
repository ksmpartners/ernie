/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
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
