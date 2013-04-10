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
import java.io._

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
      try_(reportManager.putDefinition("def_" + i)) { stream =>
        stream.write(("DEF_" + i).getBytes)
      }
      try_(reportManager.putReport("rpt_" + i, ReportType.CSV)) { stream =>
        stream.write(("RPT_" + i).getBytes)
      }

    }

  }

  @Test
  def testPut() {
    var bosR = reportManager.putReport("rpt_6", ReportType.PDF)
    Assert.assertTrue(reportManager.hasReport("rpt_6"))
    bosR.close()
    Assert.assertTrue(reportManager.hasReport("rpt_6"))
    bosR = reportManager.putReport("rpt_7", ReportType.HTML)
    Assert.assertTrue(reportManager.hasReport("rpt_7"))
    bosR.close()
    Assert.assertTrue(reportManager.hasReport("rpt_7"))

    val bosD = reportManager.putDefinition("def_6")
    Assert.assertTrue(reportManager.hasDefinition("def_6"))
    bosD.close()
    Assert.assertTrue(reportManager.hasDefinition("def_6"))
  }

  @Test()
  def testGet() {
    val buf: Array[Byte] = new Array(5)
    reportManager.getDefinition("def_1").get.read(buf)
    Assert.assertEquals(buf, "DEF_1".getBytes)
    reportManager.getReport("rpt_1").get.read(buf)
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

  def recDel(file: File) {
    if (file.isDirectory) {
      for (f <- file.listFiles()) {
        recDel(f)
      }
      if (!file.delete())
        throw new FileNotFoundException("Failed to delete file: " + file)
    } else {
      if (!file.delete())
        throw new FileNotFoundException("Failed to delete file: " + file)
    }
  }

  private def try_[A <: Closeable](ac: A)(f: A => Unit) {
    try {
      f(ac)
    } finally {
      try {
        ac.close()
      } catch {
        case e =>
      }
    }
  }
}
