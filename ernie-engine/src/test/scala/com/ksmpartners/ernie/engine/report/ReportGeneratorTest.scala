/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.engine.report

import org.testng.annotations.{ AfterClass, Test, BeforeClass }
import java.io.{ ByteArrayInputStream, ByteArrayOutputStream, File, FileInputStream }
import java.net.URL
import org.testng.Assert

class ReportGeneratorTest {

  var reportGenerator: ReportGenerator = null
  var reportManager: MemoryReportManager = null

  @BeforeClass
  def setup() {
    reportManager = new MemoryReportManager
    val url: URL = Thread.currentThread().getContextClassLoader().getResource("test_def.rptdesign")
    val file = new File(url.getPath)
    val fis = new FileInputStream(file)
    val byteArr = new Array[Byte](file.length().asInstanceOf[Int])
    fis.read(byteArr)
    reportManager.putDefinition("test_def", byteArr)
    reportGenerator = new ReportGenerator(reportManager)
    reportGenerator.startup()
  }

  @AfterClass
  def shutdown() {
    reportGenerator.shutdown()
  }

  @Test
  def canRunDefFromStream() {
    val bos = new ByteArrayOutputStream()
    reportGenerator.runPdfReport(reportManager.getDefinition("test_def"), bos)
    Assert.assertTrue(bos.toByteArray.length > 0)
  }

  @Test
  def canGetAvailableDefs() {
    Assert.assertEquals(reportGenerator.getAvailableRptDefs, List("test_def"))
  }

  @Test
  def canRunExistingDef() {
    reportGenerator.runPdfReport("test_def", "test_rpt")
    Assert.assertTrue(reportManager.hasReport("test_rpt"))
  }

  @Test(expectedExceptions = Array(classOf[IllegalStateException]))
  def cantRunExistingReportWithStoppedGenerator() {
    val rptGen = new ReportGenerator(new MemoryReportManager)
    rptGen.runPdfReport("test1", "test2")
  }

  @Test(expectedExceptions = Array(classOf[IllegalStateException]))
  def cantRunStreamReportWithStoppedGenerator() {
    val rptGen = new ReportGenerator(new MemoryReportManager)
    rptGen.runPdfReport(new ByteArrayInputStream(Array[Byte](1)), new ByteArrayOutputStream())
  }

}
