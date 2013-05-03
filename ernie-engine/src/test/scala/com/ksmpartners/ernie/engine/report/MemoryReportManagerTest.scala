/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.engine.report

import org.testng.annotations.{ BeforeMethod, Test }
import org.testng.Assert
import com.ksmpartners.ernie.model.{ ReportEntity, DefinitionEntity, ReportType }
import java.util.Date
import scala.collection._

class MemoryReportManagerTest {

  private var reportManager: MemoryReportManager = new MemoryReportManager

  @BeforeMethod
  def setup() {
    reportManager = new MemoryReportManager
    reportManager.putDefinition("def_1", "DEF_1".getBytes, new DefinitionEntity(new Date(), "def_1", "default", null, ""))
    reportManager.putDefinition("def_2", "DEF_2".getBytes, new DefinitionEntity(new Date(), "def_2", "default", null, ""))
    reportManager.putDefinition("def_3", "DEF_3".getBytes, new DefinitionEntity(new Date(), "def_3", "default", null, ""))
    reportManager.putDefinition("def_4", "DEF_4".getBytes, new DefinitionEntity(new Date(), "def_4", "default", null, ""))
    reportManager.putDefinition("def_5", "DEF_5".getBytes, new DefinitionEntity(new Date(), "def_5", "default", null, ""))
    reportManager.putReport("rpt_1", "RPT_1".getBytes, new ReportEntity(new Date(), new Date(), "rpt_1", "def_1", "default", null, ReportType.PDF))
    reportManager.putReport("rpt_2", "RPT_2".getBytes, new ReportEntity(new Date(), new Date(), "rpt_2", "def_2", "default", null, ReportType.PDF))
    reportManager.putReport("rpt_3", "RPT_3".getBytes, new ReportEntity(new Date(), new Date(), "rpt_3", "def_3", "default", null, ReportType.PDF))
    reportManager.putReport("rpt_4", "RPT_4".getBytes, new ReportEntity(new Date(), new Date(), "rpt_4", "def_4", "default", null, ReportType.PDF))
    reportManager.putReport("rpt_5", "RPT_5".getBytes, new ReportEntity(new Date(), new Date(), "rpt_5", "def_5", "default", null, ReportType.PDF))
  }

  @Test
  def testPut() {
    var entity = new mutable.HashMap[String, Any]()
    entity += (ReportManager.RPT_ID -> "rpt_6")
    entity += (ReportManager.SOURCE_DEF_ID -> "def_6")
    entity += (ReportManager.REPORT_TYPE -> ReportType.CSV)
    entity += (ReportManager.CREATED_USER -> "default")
    val bosR = reportManager.putReport(entity)
    Assert.assertFalse(reportManager.hasReport("rpt_6"))
    bosR.close()
    Assert.assertTrue(reportManager.hasReport("rpt_6"))

    entity = new mutable.HashMap[String, Any]()
    entity += (ReportManager.DEF_ID -> "def_6")
    entity += (ReportManager.CREATED_USER -> "default")
    val bosD = reportManager.putDefinition(entity)
    Assert.assertFalse(reportManager.hasDefinition("def_6"))
    bosD.close()
    Assert.assertTrue(reportManager.hasDefinition("def_6"))
  }

  @Test()
  def testGet() {
    val buf: Array[Byte] = new Array(5)
    reportManager.getDefinitionContent("def_1").get.read(buf)
    Assert.assertEquals(buf, "DEF_1".getBytes)
    reportManager.getReportContent("rpt_1").get.read(buf)
    Assert.assertEquals(buf, "RPT_1".getBytes)
  }

  @Test
  def testHas() {
    Assert.assertTrue(reportManager.hasDefinition("def_1"))
    Assert.assertTrue(reportManager.hasDefinition("def_2"))
    Assert.assertTrue(reportManager.hasDefinition("def_3"))
    Assert.assertTrue(reportManager.hasDefinition("def_4"))
    Assert.assertTrue(reportManager.hasDefinition("def_5"))
    Assert.assertTrue(reportManager.hasReport("rpt_1"))
    Assert.assertTrue(reportManager.hasReport("rpt_2"))
    Assert.assertTrue(reportManager.hasReport("rpt_3"))
    Assert.assertTrue(reportManager.hasReport("rpt_4"))
    Assert.assertTrue(reportManager.hasReport("rpt_5"))
    Assert.assertFalse(reportManager.hasReport("def_1"))
    Assert.assertFalse(reportManager.hasReport("def_2"))
    Assert.assertFalse(reportManager.hasReport("def_3"))
    Assert.assertFalse(reportManager.hasReport("def_4"))
    Assert.assertFalse(reportManager.hasReport("def_5"))
    Assert.assertFalse(reportManager.hasDefinition("rpt_1"))
    Assert.assertFalse(reportManager.hasDefinition("rpt_2"))
    Assert.assertFalse(reportManager.hasDefinition("rpt_3"))
    Assert.assertFalse(reportManager.hasDefinition("rpt_4"))
    Assert.assertFalse(reportManager.hasDefinition("rpt_5"))
  }

  @Test
  def testDelete() {
    reportManager.deleteDefinition("def_1")
    reportManager.deleteDefinition("def_2")
    reportManager.deleteDefinition("def_3")
    reportManager.deleteDefinition("def_4")
    reportManager.deleteDefinition("def_5")
    reportManager.deleteReport("rpt_1")
    reportManager.deleteReport("rpt_2")
    reportManager.deleteReport("rpt_3")
    reportManager.deleteReport("rpt_4")
    reportManager.deleteReport("rpt_5")
    Assert.assertFalse(reportManager.hasReport("def_1"))
    Assert.assertFalse(reportManager.hasReport("def_2"))
    Assert.assertFalse(reportManager.hasReport("def_3"))
    Assert.assertFalse(reportManager.hasReport("def_4"))
    Assert.assertFalse(reportManager.hasReport("def_5"))
    Assert.assertFalse(reportManager.hasDefinition("rpt_1"))
    Assert.assertFalse(reportManager.hasDefinition("rpt_2"))
    Assert.assertFalse(reportManager.hasDefinition("rpt_3"))
    Assert.assertFalse(reportManager.hasDefinition("rpt_4"))
    Assert.assertFalse(reportManager.hasDefinition("rpt_5"))
  }

  @Test
  def testGetAll() {
    Assert.assertEquals(reportManager.getAllDefinitionIds.sortWith({ (x, y) => x < y }),
      List("def_1", "def_2", "def_3", "def_4", "def_5"))
    Assert.assertEquals(reportManager.getAllReportIds.sortWith({ (x, y) => x < y }),
      List("rpt_1", "rpt_2", "rpt_3", "rpt_4", "rpt_5"))
  }

  @Test
  def missingReportOrDefinitionReturnsNone() {
    Assert.assertEquals(reportManager.getReport("FAIL"), None)
    Assert.assertEquals(reportManager.getDefinition("FAIL"), None)
  }

}
