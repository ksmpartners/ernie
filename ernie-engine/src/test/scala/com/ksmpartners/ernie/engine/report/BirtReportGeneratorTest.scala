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

import org.testng.annotations.{ AfterClass, Test, BeforeClass }
import java.io._
import java.net.URL
import org.testng.Assert
import com.ksmpartners.ernie.model.{ ParameterEntity, DefinitionEntity, ReportType }
import com.ksmpartners.ernie.engine.report.BirtReportGenerator._
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import java.util
import java.sql.Date
import com.ksmpartners.ernie.util.TestLogger

class BirtReportGeneratorTest extends BirtReportGeneratorFactory with TestLogger {

  private var reportGenerator: BirtReportGenerator = null
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

    reportGenerator = getReportGenerator(reportManager).asInstanceOf[BirtReportGenerator]
    reportGenerator.startup()
  }

  @AfterClass
  def shutdown() {
    reportGenerator.shutdown()
  }

  @Test
  def startEngineIsIdempotent() {
    reportGenerator.startup()
    reportGenerator.startup()
    reportGenerator.startup()
    reportGenerator.startup()
    reportGenerator.startup()
  }

  @Test
  def canGetAvailableDefs() {
    Assert.assertTrue(reportGenerator.getAvailableRptDefs.contains("test_def"))
    Assert.assertTrue(reportGenerator.getAvailableRptDefs.contains("test_def_var"))
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
    Assert.assertEquals(rptIs.readLine(), "false")
    Assert.assertEquals(rptIs.readLine(), "30.1")
    Assert.assertEquals(rptIs.readLine(), "toast")
    Assert.assertEquals(rptIs.readLine(), "30.100000381469727")
    Assert.assertEquals(rptIs.readLine(), "12:10:00 PM")
    //  Assert.assertEquals(rptIs.readLine(), "Aug 4  2013 10:04 AM")
    rptIs.readLine
    Assert.assertEquals(rptIs.readLine(), "Aug 4  2013")
  }

  @Test
  def canRunExistingDefWithDefaultParam() {
    val paramMap = new scala.collection.immutable.HashMap[String, String]()
    reportGenerator.runReport("test_def_var", "test_rpt_var_csv", ReportType.CSV, None, paramMap, "testUser")
    Assert.assertTrue(reportManager.hasReport("test_rpt_var_csv"))
    val rptIs: BufferedReader = new BufferedReader(new InputStreamReader(reportManager.getReportContent("test_rpt_var_csv").get))
    Assert.assertEquals(rptIs.readLine(), "This is a test page.")
    Assert.assertEquals(rptIs.readLine(), "10")
    Assert.assertEquals(rptIs.readLine(), "true")
    Assert.assertEquals(rptIs.readLine(), "10.1")
    Assert.assertEquals(rptIs.readLine(), "test")
    Assert.assertEquals(rptIs.readLine(), "10.100000381469727")
    Assert.assertEquals(rptIs.readLine(), "10:04:00 AM")
    //  Assert.assertEquals(rptIs.readLine(), "Oct 4  2013 10:04 AM")
    rptIs.readLine
    Assert.assertEquals(rptIs.readLine(), "Oct 4  2013")
  }

  @Test(expectedExceptions = Array(classOf[ParameterNullException]))
  def nullParamsThrowsException() {
    var paramMap = new scala.collection.immutable.HashMap[String, String]()
    paramMap += ("var_integer" -> "30")
    paramMap += ("var_boolean" -> "false")
    paramMap += ("var_decimal" -> "30.1")
    paramMap += ("var_string" -> "toast")
    paramMap += ("var_float" -> "30.1")
    paramMap += ("var_time" -> "12:10:00")
    paramMap += ("var_datetime" -> "2013-08-04T10:04:00")
    paramMap += ("var_date" -> "")
    reportGenerator.runReport("test_def_var", "test_rpt_var_csv", ReportType.CSV, None, paramMap, "testUser")
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

  @Test(expectedExceptions = Array(classOf[IllegalStateException]), dependsOnMethods = Array("canGetAvailableDefs", "canRunExistingDef", "canValidateReportDefinition", "canRunExistingDefWithParam", "nullParamsThrowsException", "canRunExistingDefWithDefaultParam", "startEngineIsIdempotent"))
  def cantRunExistingReportWithStoppedGenerator() {
    val rptGen = new BirtReportGenerator(new MemoryReportManager)
    rptGen.startup
    rptGen.shutdown()
    rptGen.runReport("test1", "test2", ReportType.PDF, None, "testUser")
  }

  @Test
  def canGetBirtParamData() {
    val bool = stringToBirtParamData("false", new ParameterEntity("var", "boolean", false, "true"))
    Assert.assertEquals(bool.getClass, classOf[java.lang.Boolean])
    Assert.assertEquals(bool, false)
    val int = stringToBirtParamData("21", new ParameterEntity("var", "integer", false, "1"))
    Assert.assertEquals(int.getClass, classOf[java.lang.Integer])
    Assert.assertEquals(int, 21)
    val date = stringToBirtParamData("2013-08-04", new ParameterEntity("var", "date", false, "2013-08-04"))
    Assert.assertEquals(date.getClass, classOf[java.sql.Date])
    Assert.assertEquals(date, new Date(113, 7, 4))
    val datetime = stringToBirtParamData("2013-08-04T10:15:00", new ParameterEntity("var", "dateTime", false, "2013-08-04T10:15:00"))
    Assert.assertEquals(datetime.getClass, classOf[java.sql.Date])
    Assert.assertEquals(datetime, new java.sql.Date(DateTime.parse("2013-08-04T10:15:00").getMillis))
    val decimal = stringToBirtParamData("21.1", new ParameterEntity("var", "decimal", false, "1.1"))
    Assert.assertEquals(decimal.getClass, classOf[java.lang.Double])
    Assert.assertEquals(decimal, 21.1d)
    val float = stringToBirtParamData("21.1", new ParameterEntity("var", "float", false, "1.1"))
    Assert.assertEquals(float.getClass, classOf[java.lang.Float])
    Assert.assertEquals(float, 21.1f)
    val string = stringToBirtParamData("toast", new ParameterEntity("var", "string", false, "test"))
    Assert.assertEquals(string.getClass, classOf[java.lang.String])
    Assert.assertEquals(string, "toast")
    val time = stringToBirtParamData("10:15:00", new ParameterEntity("var", "time", false, "08:15:00"))
    Assert.assertEquals(time.getClass, classOf[java.sql.Time])
    Assert.assertEquals(time, java.sql.Time.valueOf("10:15:00"))
    val any = stringToBirtParamData("Anything", new ParameterEntity("var", "any", false, "Nothing"))
    Assert.assertEquals(any.getClass, classOf[java.lang.String])
    Assert.assertEquals(any, "Anything")
  }

  @Test(expectedExceptions = Array(classOf[UnsupportedDataTypeException]))
  def unsupportedDataTypeThrowsException() {
    stringToBirtParamData("false", new ParameterEntity("var", "bad_type", false, "true"))
  }

  @Test(expectedExceptions = Array(classOf[ClassCastException]))
  def badDataThrowsClassCastException() {
    stringToBirtParamData("false", new ParameterEntity("var", "decimal", false, "21.1"))
  }

}
