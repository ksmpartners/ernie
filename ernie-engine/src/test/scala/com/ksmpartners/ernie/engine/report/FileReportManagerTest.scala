/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.engine.report

import org.testng.annotations.{ AfterClass, BeforeClass, BeforeMethod, Test }
import org.testng.Assert
import com.ksmpartners.ernie.model.ReportType
import com.ksmpartners.ernie.util.Utility._
import java.io._
import collection.mutable

class FileReportManagerTest {

  private var reportManager: FileReportManager = null
  private var tempInputDir: File = null
  private var tempOutputDir: File = null

  @BeforeClass
  def setup() {
    tempInputDir = createTempDirectory()
    tempOutputDir = createTempDirectory()
    reportManager = new FileReportManager(tempInputDir.getAbsolutePath, tempOutputDir.getAbsolutePath)
  }

  @AfterClass
  def teardown() {
    recDel(tempInputDir)
    recDel(tempOutputDir)
  }

  @BeforeMethod
  def setupMethod() {

    for (i <- 1 to 5) {
      var entity = new mutable.HashMap[String, Any]()
      entity += (ReportManager.defId -> ("def_" + i))
      entity += (ReportManager.createdUser -> "default")
      try_(reportManager.putDefinition(entity)) { stream =>
        stream.write(("DEF_" + i).getBytes)
      }
      entity = new mutable.HashMap[String, Any]()
      entity += (ReportManager.rptId -> ("rpt_" + i))
      entity += (ReportManager.sourceDefId -> ("def_" + i))
      entity += (ReportManager.reportType -> ReportType.CSV)
      entity += (ReportManager.createdUser -> "default")
      try_(reportManager.putReport(entity)) { stream =>
        stream.write(("RPT_" + i).getBytes)
      }

    }

  }

  @Test
  def testDefinition() {
    val definition = reportManager.getDefinition("def_1").get
    Assert.assertEquals(definition.getCreatedUser, "default")
    Assert.assertEquals(definition.getDefDescription, "")
    Assert.assertEquals(definition.getParamNames.size, 0)
    Assert.assertEquals(definition.getDefId, "def_1")
    val defEnt = definition.getEntity
    Assert.assertEquals(defEnt.getCreatedUser, "default")
    Assert.assertEquals(defEnt.getDefDescription, "")
    Assert.assertNull(defEnt.getParamNames)
    Assert.assertEquals(defEnt.getDefId, "def_1")
  }

  @Test
  def testReport() {
    val report = reportManager.getReport("rpt_1").get
    Assert.assertEquals(report.getCreatedUser, "default")
    Assert.assertEquals(report.getRptId, "rpt_1")
    Assert.assertEquals(report.getSourceDefId, "def_1")
    Assert.assertEquals(report.getReportType, ReportType.CSV)
    Assert.assertEquals(report.getParams.size, 0)
    val rptEnt = report.getEntity
    Assert.assertEquals(rptEnt.getCreatedUser, "default")
    Assert.assertEquals(rptEnt.getRptId, "rpt_1")
    Assert.assertEquals(rptEnt.getSourceDefId, "def_1")
    Assert.assertEquals(rptEnt.getReportType, ReportType.CSV)
    Assert.assertNull(rptEnt.getParams)
  }

  @Test
  def testPut() {
    var entity = new mutable.HashMap[String, Any]()
    entity += (ReportManager.rptId -> "rpt_6")
    entity += (ReportManager.sourceDefId -> "def_6")
    entity += (ReportManager.reportType -> ReportType.PDF)
    entity += (ReportManager.createdUser -> "default")
    var bosR = reportManager.putReport(entity)
    Assert.assertTrue(reportManager.hasReport("rpt_6"))
    bosR.close()
    Assert.assertTrue(reportManager.hasReport("rpt_6"))

    entity += (ReportManager.rptId -> "rpt_7")
    entity += (ReportManager.sourceDefId -> "def_7")
    entity += (ReportManager.reportType -> ReportType.HTML)
    entity += (ReportManager.createdUser -> "default")
    bosR = reportManager.putReport(entity)
    Assert.assertTrue(reportManager.hasReport("rpt_7"))
    bosR.close()
    Assert.assertTrue(reportManager.hasReport("rpt_7"))

    entity = new mutable.HashMap[String, Any]()
    entity += (ReportManager.defId -> "def_6")
    entity += (ReportManager.createdUser -> "default")
    val bosD = reportManager.putDefinition(entity)
    Assert.assertTrue(reportManager.hasDefinition("def_6"))
    bosD.close()
    Assert.assertTrue(reportManager.hasDefinition("def_6"))
  }

  @Test()
  def testGet() {
    val buf: Array[Byte] = new Array(5)
    val definition = reportManager.getDefinition("def_1").get
    reportManager.getDefinitionContent(definition).get.read(buf)
    Assert.assertEquals(buf, "DEF_1".getBytes)
    val report = reportManager.getReport("rpt_1").get
    reportManager.getReportContent(report).get.read(buf)
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

  @Test
  def canLoadExistingDirectories() {
    val rptManager = new FileReportManager(tempInputDir.getAbsolutePath, tempOutputDir.getAbsolutePath)
    Assert.assertTrue(rptManager.hasDefinition("def_1"))
    Assert.assertTrue(rptManager.hasDefinition("def_2"))
    Assert.assertTrue(rptManager.hasDefinition("def_3"))
    Assert.assertTrue(rptManager.hasDefinition("def_4"))
    Assert.assertTrue(rptManager.hasDefinition("def_5"))
    Assert.assertTrue(rptManager.hasReport("rpt_1"))
    Assert.assertTrue(rptManager.hasReport("rpt_2"))
    Assert.assertTrue(rptManager.hasReport("rpt_3"))
    Assert.assertTrue(rptManager.hasReport("rpt_4"))
    Assert.assertTrue(rptManager.hasReport("rpt_5"))
  }

  private def createTempDirectory(): File = {

    var temp: File = null

    temp = File.createTempFile("temp", System.nanoTime.toString)

    if (!(temp.delete())) {
      throw new IOException("Could not delete temp file: " + temp.getAbsolutePath)
    }

    if (!(temp.mkdir())) {
      throw new IOException("Could not create temp directory: " + temp.getAbsolutePath)
    }

    temp
  }
}
