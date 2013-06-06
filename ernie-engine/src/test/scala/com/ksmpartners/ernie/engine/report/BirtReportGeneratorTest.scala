/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.engine.report

import org.testng.annotations.{ AfterClass, Test, BeforeClass }
import java.io._
import java.net.URL
import org.testng.Assert
import com.ksmpartners.ernie.model.{ DefinitionEntity, ReportType }
import com.ksmpartners.ernie.util.Utility.try_
import org.joda.time.DateTime
import org.slf4j.LoggerFactory

class BirtReportGeneratorTest {

  private var reportGenerator: ReportGenerator = null
  private var reportManager: MemoryReportManager = null

  private val log = LoggerFactory.getLogger("c.k.e.e.report.BirtReportGeneratorTest")

  @BeforeClass
  def setup() {
    reportManager = new MemoryReportManager
    val url: URL = Thread.currentThread.getContextClassLoader.getResource("test_def.rptdesign")
    val file = new File(url.getPath)
    val fis = new FileInputStream(file)
    val byteArr = new Array[Byte](file.length.asInstanceOf[Int])
    fis.read(byteArr)
    reportManager.putDefinition("test_def", byteArr, new DefinitionEntity(DateTime.now(), "test_def", "default", null, "", null, null))
    reportGenerator = new BirtReportGenerator(reportManager)
    reportGenerator.startup()
  }

  @AfterClass
  def shutdown() {
    reportGenerator.shutdown()
  }

  /*@Test
  def canRunDefFromStream() {
    val bos = new ByteArrayOutputStream()
    reportGenerator.runReport(reportManager.getDefinitionContent("test_def").get, bos, ReportType.PDF, Map.empty[String, Any])
    Assert.assertTrue(bos.toByteArray.length > 0)
  } */

  @Test
  def canGetAvailableDefs() {
    Assert.assertEquals(reportGenerator.getAvailableRptDefs, List("test_def"))
  }

  @Test
  def canRunExistingDef() {
    reportGenerator.runReport("test_def", "test_rpt_pdf", ReportType.PDF, None, "testUser")
    reportGenerator.runReport("test_def", "test_rpt_csv", ReportType.CSV, None, "testUser")
    reportGenerator.runReport("test_def", "test_rpt_html", ReportType.HTML, None, "testUser")
    Assert.assertTrue(reportManager.hasReport("test_rpt_pdf"))
    Assert.assertTrue(reportManager.hasReport("test_rpt_csv"))
    Assert.assertTrue(reportManager.hasReport("test_rpt_html"))
  }

  @Test
  def canValidateReportDefinition() {
    var result = false
    var file = new File(Thread.currentThread.getContextClassLoader.getResource("test_def.rptdesign").getPath)
    try {
      result = BirtReportGenerator.isValidDefinition(new FileInputStream(file))
    }
    Assert.assertTrue(result)
    try {
      file = File.createTempFile("fail_def", ".rptdesign")
      result = BirtReportGenerator.isValidDefinition(new FileInputStream(file))
    }
    Assert.assertFalse(result)
  }

  @Test(expectedExceptions = Array(classOf[IllegalStateException]), dependsOnMethods = Array("canGetAvailableDefs", "canRunExistingDef", "canValidateReportDefinition"))
  def cantRunExistingReportWithStoppedGenerator() {
    val rptGen = new BirtReportGenerator(new MemoryReportManager)
    rptGen.shutdown()
    rptGen.runReport("test1", "test2", ReportType.PDF, None, "testUser")
  }

  /* @Test(expectedExceptions = Array(classOf[IllegalStateException]), dependsOnMethods = Array("canRunDefFromStream", "canGetAvailableDefs", "canRunExistingDef", "canValidateReportDefinition"))
  def cantRunStreamReportWithStoppedGenerator() {
    val rptGen = new BirtReportGenerator(new MemoryReportManager)
    rptGen.shutdown()
    rptGen.runReport(new ByteArrayInputStream(Array[Byte](1)), new ByteArrayOutputStream(), ReportType.PDF, Map.empty[String, Any])
  } */

}
