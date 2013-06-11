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
import com.ksmpartners.ernie.model.{ ParameterEntity, DefinitionEntity, ReportType }
import com.ksmpartners.ernie.util.Utility.try_
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import java.util

class BirtReportGeneratorTest {

  private var reportGenerator: ReportGenerator = null
  private var reportManager: MemoryReportManager = null

  private val log = LoggerFactory.getLogger("c.k.e.e.report.BirtReportGeneratorTest")

  @BeforeClass
  def setup() {
    reportManager = new MemoryReportManager

    var url: URL = Thread.currentThread.getContextClassLoader.getResource("test_def.rptdesign")
    var file = new File(url.getPath)
    var fis = new FileInputStream(file)
    var byteArr = new Array[Byte](file.length.asInstanceOf[Int])
    fis.read(byteArr)
    reportManager.putDefinition("test_def", byteArr, new DefinitionEntity(DateTime.now(), "test_def", "default", null, "", null, null))

    url = Thread.currentThread.getContextClassLoader.getResource("test_def_var.rptdesign")
    file = new File(url.getPath)
    fis = new FileInputStream(file)
    byteArr = new Array[Byte](file.length.asInstanceOf[Int])
    fis.read(byteArr)
    val param1 = new ParameterEntity("var_integer", "integer", false, "10")
    val param2 = new ParameterEntity("var_boolean", "boolean", false, "true")
    val param3 = new ParameterEntity("var_decimal", "decimal", false, "10.1")
    val param4 = new ParameterEntity("var_string", "string", false, "test")
    val param5 = new ParameterEntity("var_float", "float", false, "10.1")
    val param6 = new ParameterEntity("var_time", "time", false, "10:04:00")
    val param7 = new ParameterEntity("var_datetime", "dateTime", false, "2013-10-04T10:04:00")
    val param8 = new ParameterEntity("var_date", "date", false, "2013-10-04")
    val paramNameList = new util.ArrayList[String]()
    paramNameList.add("var_integer")
    paramNameList.add("var_boolean")
    paramNameList.add("var_decimal")
    paramNameList.add("var_string")
    paramNameList.add("var_float")
    paramNameList.add("var_time")
    paramNameList.add("var_datetime")
    paramNameList.add("var_date")
    val paramList = new util.ArrayList[ParameterEntity]()
    paramList.add(param1)
    paramList.add(param2)
    paramList.add(param3)
    paramList.add(param4)
    paramList.add(param5)
    paramList.add(param6)
    paramList.add(param7)
    paramList.add(param8)
    reportManager.putDefinition("test_def_var", byteArr, new DefinitionEntity(DateTime.now(), "test_def_var", "default", paramNameList, "", null, paramList))

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
    Assert.assertEquals(reportGenerator.getAvailableRptDefs, List("test_def", "test_def_var"))
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
  def canRunExistingDefWithParam() {
    var paramMap = new scala.collection.immutable.HashMap[String, String]()
    paramMap += ("var_integer" -> "30")
    paramMap += ("var_boolean" -> "false")
    paramMap += ("var_decimal" -> "30.1")
    paramMap += ("var_string" -> "toast")
    paramMap += ("var_float" -> "30.1")
    paramMap += ("var_time" -> "12:10:00")
    paramMap += ("var_datetime" -> "2013-08-04T10:04:00")
    paramMap += ("var_date" -> "2013-08-04")
    reportGenerator.runReport("test_def_var", "test_rpt_var_csv", ReportType.CSV, None, paramMap, "testUser")
    Assert.assertTrue(reportManager.hasReport("test_rpt_var_csv"))
    val rptIs: BufferedReader = new BufferedReader(new InputStreamReader(reportManager.getReportContent("test_rpt_var_csv").get))
    Assert.assertEquals(rptIs.readLine(), "This is a test page.")
    Assert.assertEquals(rptIs.readLine(), "30")

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

  @Test(expectedExceptions = Array(classOf[IllegalStateException]), dependsOnMethods = Array("canGetAvailableDefs", "canRunExistingDef", "canValidateReportDefinition", "canRunExistingDefWithParam"))
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
