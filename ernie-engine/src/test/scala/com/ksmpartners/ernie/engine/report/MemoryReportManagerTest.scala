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
import scala.collection._
import com.fasterxml.jackson.databind.ObjectMapper
import org.joda.time.DateTime

class MemoryReportManagerTest {

  private var reportManager: MemoryReportManager = new MemoryReportManager

  @BeforeMethod
  def setup() {
    reportManager = new MemoryReportManager
    reportManager.putDefinition("def_1", "DEF_1".getBytes, new DefinitionEntity(DateTime.now(), "def_1", "default", null, ""))
    reportManager.putDefinition("def_2", "DEF_2".getBytes, new DefinitionEntity(DateTime.now(), "def_2", "default", null, ""))
    reportManager.putDefinition("def_3", "DEF_3".getBytes, new DefinitionEntity(DateTime.now(), "def_3", "default", null, ""))
    reportManager.putDefinition("def_4", "DEF_4".getBytes, new DefinitionEntity(DateTime.now(), "def_4", "default", null, ""))
    reportManager.putDefinition("def_5", "DEF_5".getBytes, new DefinitionEntity(DateTime.now(), "def_5", "default", null, ""))
    reportManager.putReport("rpt_1", "RPT_1".getBytes, new ReportEntity(DateTime.now(), DateTime.now(), "rpt_1", "def_1", "default", null, ReportType.PDF))
    reportManager.putReport("rpt_2", "RPT_2".getBytes, new ReportEntity(DateTime.now(), DateTime.now(), "rpt_2", "def_2", "default", null, ReportType.PDF))
    reportManager.putReport("rpt_3", "RPT_3".getBytes, new ReportEntity(DateTime.now(), DateTime.now(), "rpt_3", "def_3", "default", null, ReportType.PDF))
    reportManager.putReport("rpt_4", "RPT_4".getBytes, new ReportEntity(DateTime.now(), DateTime.now(), "rpt_4", "def_4", "default", null, ReportType.PDF))
    reportManager.putReport("rpt_5", "RPT_5".getBytes, new ReportEntity(DateTime.now(), DateTime.now(), "rpt_5", "def_5", "default", null, ReportType.PDF))
  }

  @Test
  def testPutReport() {
    var entity = new mutable.HashMap[String, Any]()
    entity += (ReportManager.RPT_ID -> "rpt_6")
    entity += (ReportManager.SOURCE_DEF_ID -> "def_6")
    entity += (ReportManager.REPORT_TYPE -> ReportType.CSV)
    entity += (ReportManager.CREATED_USER -> "default")
    var params = new mutable.HashMap[String, String]()
    params += ("PARAM_1" -> "VAL_1")
    params += ("PARAM_2" -> "VAL_2")
    params += ("PARAM_3" -> "VAL_3")
    entity += (ReportManager.PARAM_MAP -> params)
    val bosR = reportManager.putReport(entity)
    Assert.assertFalse(reportManager.hasReport("rpt_6"))
    bosR.close()
    Assert.assertTrue(reportManager.hasReport("rpt_6"))

    val report = reportManager.getReport("rpt_6").get
    Assert.assertNotNull(report.getParams)
    Assert.assertNotNull(report.getCreatedDate)
    Assert.assertNotNull(report.getRetentionDate)
  }

  @Test
  def testPutDefinition() {
    var entity = new mutable.HashMap[String, Any]()
    entity = new mutable.HashMap[String, Any]()
    entity += (ReportManager.DEF_ID -> "def_6")
    entity += (ReportManager.CREATED_USER -> "default")
    val paramList = List("PARAM_1", "PARAM_2", "PARAM_3")
    entity += (ReportManager.PARAM_NAMES -> paramList)
    val bosD = reportManager.putDefinition(entity)
    Assert.assertFalse(reportManager.hasDefinition("def_6"))
    bosD.close()
    Assert.assertTrue(reportManager.hasDefinition("def_6"))

    val definition = reportManager.getDefinition("def_6").get
    Assert.assertNotNull(definition.getParamNames)
    Assert.assertNotNull(definition.getCreatedDate)
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

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException]))
  def missingDefIdThrowsException() {
    var entity = new mutable.HashMap[String, Any]()
    entity += (ReportManager.CREATED_USER -> "default")
    reportManager.putDefinition(entity)
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException]))
  def missingDefCreatedUserThrowsException() {
    var entity = new mutable.HashMap[String, Any]()
    entity += (ReportManager.DEF_ID -> "def_6")
    reportManager.putDefinition(entity)
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException]))
  def missingRptIdThrowsException() {
    var entity = new mutable.HashMap[String, Any]()
    entity += (ReportManager.SOURCE_DEF_ID -> "def_6")
    entity += (ReportManager.REPORT_TYPE -> ReportType.CSV)
    entity += (ReportManager.CREATED_USER -> "default")
    reportManager.putReport(entity)
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException]))
  def missingSourceDefIdThrowsException() {
    var entity = new mutable.HashMap[String, Any]()
    entity += (ReportManager.RPT_ID -> "rpt_6")
    entity += (ReportManager.REPORT_TYPE -> ReportType.CSV)
    entity += (ReportManager.CREATED_USER -> "default")
    reportManager.putReport(entity)
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException]))
  def missingReportTypeThrowsException() {
    var entity = new mutable.HashMap[String, Any]()
    entity += (ReportManager.RPT_ID -> "rpt_6")
    entity += (ReportManager.SOURCE_DEF_ID -> "def_6")
    entity += (ReportManager.CREATED_USER -> "default")
    reportManager.putReport(entity)
  }

  @Test(expectedExceptions = Array(classOf[IllegalArgumentException]))
  def missingRptCreatedUserThrowsException() {
    var entity = new mutable.HashMap[String, Any]()
    entity += (ReportManager.RPT_ID -> "rpt_6")
    entity += (ReportManager.SOURCE_DEF_ID -> "def_6")
    entity += (ReportManager.REPORT_TYPE -> ReportType.CSV)
    reportManager.putReport(entity)
  }

}
