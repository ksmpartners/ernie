/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.server

import filter.SAMLConstants
import org.testng.annotations._
import net.liftweb.mockweb.{ MockWeb, WebSpec }
import bootstrap.liftweb.Boot
import net.liftweb.mocks.MockHttpServletRequest
import com.ksmpartners.ernie.server.PropertyNames._
import com.ksmpartners.ernie.model._
import org.testng.Assert
import net.liftweb.http._
import java.util.Properties
import java.io.{ FileOutputStream, FileInputStream, File }
import com.ksmpartners.ernie.util.Utility._
import org.slf4j.{ Logger, LoggerFactory }
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonAST.{ JBool, JField, JObject }
import net.liftweb.http.auth.{ AuthRole, userRoles }

//import com.ksmpartners.common.annotations.tracematrix.{ TestSpecs, TestSpec }
import com.ksmpartners.ernie.util.MapperUtility._
import net.liftweb.http.StreamingResponse
import net.liftweb.json.JsonAST.JObject
import net.liftweb.http.ResponseWithReason
import net.liftweb.http.BadResponse
import net.liftweb.http.GoneResponse
import net.liftweb.json.JsonAST.JField
import net.liftweb.json.JsonAST.JBool
import scala.xml.NodeSeq
import scala.collection.JavaConversions.asJavaCollection
import com.ksmpartners.ernie.server.service.{ ServiceRegistry, ConflictResponse }

import com.ksmpartners.ernie.engine.PurgeResponse
import com.ksmpartners.ernie.engine.PurgeRequest
import scala.collection.JavaConversions
import org.joda.time.DateTime
import com.ksmpartners.ernie.server.service.ServiceRegistry._
import net.liftweb.http.ResponseWithReason
import com.ksmpartners.ernie.server.service.ConflictResponse
import net.liftweb.common.Box

class DefinitionTest extends WebSpec(() => {
  val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.server.DefinitionTest")

  val properties: Properties = {
    val url = Thread.currentThread.getContextClassLoader.getResource("test.props")
    System.setProperty(propertiesFileNameProp, url.getPath)
    val propsPath = System.getProperty(propertiesFileNameProp)

    if (null == propsPath) {
      throw new RuntimeException("System property " + propertiesFileNameProp + " is undefined")
    }

    val propsFile = new File(propsPath)
    if (!propsFile.exists) {
      throw new RuntimeException("Properties file " + propsPath + " does not exist.")
    }

    if (!propsFile.canRead) {
      throw new RuntimeException("Properties file " + propsPath + " is not readable; check file privileges.")
    }
    val props = new Properties()
    try_(new FileInputStream(propsFile)) { propsFileStream =>
      props.load(propsFileStream)
    }
    props
  }

  val outputDir = new File(properties.get("output.dir").toString)
  val jobsDir = new File(properties.get("jobs.dir").toString)
  val defsDir = new File(properties.get("rpt.def.dir").toString)
  for (file <- outputDir.listFiles()) {
    recDel(file)
  }
  for (file <- jobsDir.listFiles()) {
    recDel(file)
  }
  for (file <- defsDir.listFiles()) {
    if (!file.getName.contains("test_def")) recDel(file)
  }

  for (i <- 1 to 4) {
    var report = new ReportEntity(DateTime.now, if (i % 2 == 0) DateTime.now.minusDays(10) else DateTime.now.plusDays(4), "REPORT_" + i, "test_def", "default", null, ReportType.PDF, null, null)
    try {
      val rptEntFile = new File(outputDir, report.getRptId + ".entity")
      try_(new FileOutputStream(rptEntFile)) { fos =>
        mapper.writeValue(fos, report)
      }
      val ext = report.getReportType match {
        case ReportType.CSV => ".csv"
        case ReportType.HTML => ".html"
        case ReportType.PDF => ".pdf"
      }
      val file = new File(outputDir, report.getRptId + ext)
      try_(new FileOutputStream(file)) { fos =>
        fos.write("test".getBytes)
      }

      val job = new File(jobsDir, rptToJobId(report.getRptId) + ".entity")
      val jobEnt: JobEntity = new JobEntity(rptToJobId(report.getRptId), if (i % 2 == 0) JobStatus.COMPLETE else JobStatus.IN_PROGRESS, DateTime.now, report.getRptId, if (i % 2 == 0) null else report)
      try_(new FileOutputStream(job)) { fos =>
        mapper.writeValue(fos, jobEnt)
      }
    } catch {
      case e: Exception => log.info("Caught exception while generating test entities: {}", e.getMessage + "\n" + e.getStackTraceString)
    }
  }

  (new TestBoot).setUpAndBoot()
}) {

  val properties: Properties = {
    val url = Thread.currentThread.getContextClassLoader.getResource("test.props")
    System.setProperty(propertiesFileNameProp, url.getPath)
    val propsPath = System.getProperty(propertiesFileNameProp)

    if (null == propsPath) {
      throw new RuntimeException("System property " + propertiesFileNameProp + " is undefined")
    }

    val propsFile = new File(propsPath)
    if (!propsFile.exists) {
      throw new RuntimeException("Properties file " + propsPath + " does not exist.")
    }

    if (!propsFile.canRead) {
      throw new RuntimeException("Properties file " + propsPath + " is not readable; check file privileges.")
    }
    val props = new Properties()
    try_(new FileInputStream(propsFile)) { propsFileStream =>
      props.load(propsFileStream)
    }
    props
  }

  @Test(enabled = false)
  private var outputDir: File = null

  @Test(enabled = false)
  private var jobsDir: File = null

  @Test(enabled = false)
  private var defsDir: File = null

  @Test(enabled = false)
  private[this] var testDef: String = ""

  private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.server.DefinitionTest")

  @BeforeSuite
  def setup() {
    outputDir = new File(properties.get("output.dir").toString)
    jobsDir = new File(properties.get("jobs.dir").toString)
    defsDir = new File(properties.get("rpt.def.dir").toString)
  }

  @AfterClass(groups = Array("REST"))
  def finish() {

  }

  @AfterSuite
  def shutdown() {

    DispatchRestAPI.shutdown()
    for (file <- outputDir.listFiles()) {
      recDel(file)
    }
    for (file <- jobsDir.listFiles()) {
      recDel(file)
    }
    for (file <- defsDir.listFiles()) {
      if (!file.getName.contains("test_def")) recDel(file)
    }

    var keep = new File(outputDir, ".keep")
    keep.createNewFile()
    keep = new File(jobsDir, ".keep")
    keep.createNewFile()
  }

  @AfterMethod
  def logMethodAfter(result: java.lang.reflect.Method) {
    log.debug("END test:" + result.getName)
  }

  @BeforeMethod
  def logMethodBefore(result: java.lang.reflect.Method) {
    log.debug("BEGIN test:" + result.getName)
  }

  @Test
  def downloadServiceReturn410ForExpiredReports() {
    val mockReq = new MockReadAuthReq("/jobs/2/result")

    mockReq.headers += ("Accept" -> List("application/pdf"))

    MockWeb.testReq(mockReq) { req =>
      val respBox = DispatchRestAPI(req)()
      Assert.assertTrue(respBox.isDefined)
      Assert.assertEquals(respBox.open_!.getClass, classOf[ResponseWithReason])
      Assert.assertEquals(respBox.open_!.toResponse.code, 410)
    }
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-129")))
  @Test(dependsOnMethods = Array("downloadServiceReturn410ForExpiredReports"))
  def canPurgeJobs() {
    val mockReq = new MockWriteAuthReq("/jobs/expired")
    mockReq.method = "DELETE"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[OkResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 200)

    }

    var mockReq2 = new MockReadAuthReq("/jobs/2/status")

    mockReq2.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq2) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertEquals(resp.open_!.getClass, classOf[ResponseWithReason])
      Assert.assertEquals(resp.open_!.toResponse.code, GoneResponse().toResponse.code)
    }

    mockReq2 = new MockReadAuthReq("/jobs/4/status")

    mockReq2.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq2) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertEquals(resp.open_!.getClass, classOf[ResponseWithReason])
      Assert.assertEquals(resp.open_!.toResponse.code, GoneResponse().toResponse.code)
    }

  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-160")))
  @Test
  def jobsInProgressOnShutDownAreRestarted() {
    var jobRunning = true
    var end = System.currentTimeMillis + (1000 * 30)
    var job1Complete = false
    var job2Complete = false
    while (jobRunning && (System.currentTimeMillis < end)) {
      val respOpt = DispatchRestAPI.deserialize(jobStatusResource.get("1").open_!.asInstanceOf[PlainTextResponse].text.getBytes, classOf[StatusResponse])
      if (respOpt.getJobStatus == JobStatus.COMPLETE) {
        job1Complete = true
        jobRunning = false
      }
    }
    jobRunning = true
    end = System.currentTimeMillis + (1000 * 30)
    while (jobRunning && (System.currentTimeMillis < end)) {
      val respOpt = DispatchRestAPI.deserialize(jobStatusResource.get("3").open_!.asInstanceOf[PlainTextResponse].text.getBytes, classOf[StatusResponse])
      if (respOpt.getJobStatus == JobStatus.COMPLETE) {
        job2Complete = true
        jobRunning = false
      }
    }
    Assert.assertTrue(job1Complete)
    Assert.assertTrue(job2Complete)
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-41")))
  @Test
  def canGetDefs() {
    val mockReq = new MockReadAuthReq("/defs")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[PlainTextResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 200)
    }
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-42")))
  @Test
  def getDefsReturnsJSON() {
    val mockReq = new MockReadAuthReq("/defs")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.toResponse.headers.contains(("Content-Type", "application/vnd.ksmpartners.ernie+json")))
      val body = resp.open_!.asInstanceOf[PlainTextResponse].text
      val respObj = mapper.readValue(body, classOf[ReportDefinitionMapResponse])
      Assert.assertNotNull(respObj.getReportDefMap)
    }
  }

  /*@Test
  def cantPostJobWithoutExistingReportDefinitionFile() {
    val mockReq = new MockWriteAuthReq("/jobs")
    mockReq.method = "POST"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    val mockReportReq = new ReportRequest()
    mockReportReq.setDefId("WRONG")
    mockReportReq.setRptType(ReportType.HTML)
    mockReq.body = DispatchRestAPI.serialize(mockReportReq)

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      log.info(resp.open_!.toResponse.code + "")
      Assert.assertTrue(resp.open_!.isInstanceOf[BadResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 400)
    }
  } */

  ////@TestSpecs(Array(new TestSpec(key = "ERNIE-120")))
  //@Test(dependsOnMethods = Array("canPostJob"))
  @Test(enabled = false)
  def cantDeleteInUseDef() {
    val mockReq = new MockWriteAuthReq("/defs/" + testDef)
    mockReq.method = "DELETE"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)

      Assert.assertTrue(resp.open_!.isInstanceOf[ConflictResponse])
    }
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-80")))
  @Test
  def canGetDefDetails() {
    val mockReq = new MockReadAuthReq("/defs/test_def")
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[PlainTextResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 200)
      val body = resp.open_!.asInstanceOf[PlainTextResponse].text
      val respObj = mapper.readValue(body, classOf[DefinitionEntity])
      Assert.assertNotNull(respObj.getDefId)
    }
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-81")))
  @Test
  def defDetailServiceReturnsJSON() {
    val mockReq = new MockReadAuthReq("/defs/test_def")
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.toResponse.headers.contains(("Content-Type", "application/vnd.ksmpartners.ernie+json")))
    }
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-96")))
  @Test
  def cantGetDefDetailForNonExistentReportDef() {
    val mockReq = new MockReadAuthReq("/defs/invalid_def")
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI.apply(req).apply()
      Assert.assertTrue(resp.isDefined)
      Assert.assertEquals(resp.open_!.toResponse.code, 404)
    }
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-47"), new TestSpec(key = "ERNIE-49"), new TestSpec(key = "ERNIE-162")))
  @Test //(dependsOnMethods = Array("canDeleteDefs"))
  def canPostDefs() {
    val mockReq = new MockWriteAuthReq("/defs")
    mockReq.method = "POST"

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    val defEnt = new DefinitionEntity()
    defEnt.setCreatedUser("default")
    defEnt.setDefId("test_def2")
    mockReq.body = DispatchRestAPI.serialize(defEnt).getBytes

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)

      Assert.assertEquals(resp.open_!.toResponse.code, 201)

      Assert.assertTrue(resp.open_!.isInstanceOf[PlainTextResponse])

      val defEntRsp: DefinitionEntity = DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[DefinitionEntity])
      Assert.assertEquals(defEntRsp.getCreatedUser, "mockWriteUser")
      testDef = defEntRsp.getDefId
      Assert.assertTrue(resp.open_!.toResponse.headers.contains(("Location", req.hostAndPath + "/defs/" + defEntRsp.getDefId)))

    }

  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-92"), new TestSpec(key = "ERNIE-95")))
  @Test(dependsOnMethods = Array("canPostDefs"))
  def canDeleteDefs() {
    val mockReq = new MockWriteAuthReq("/defs/" + testDef)
    mockReq.method = "DELETE"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[PlainTextResponse])
      val deleteDefinitionResponse = DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[DeleteDefinitionResponse])
      Assert.assertEquals(deleteDefinitionResponse.getDeleteStatus, DeleteStatus.SUCCESS)
    }

    canPostDefs
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-48"), new TestSpec(key = "ERNIE-100"), new TestSpec(key = "ERNIE-101")))
  @Test(dependsOnMethods = Array("canDeleteDefs"))
  def canPutDefs() {
    val mockReq = new MockWriteAuthReq("/defs/" + testDef + "/rptdesign")
    mockReq.method = "PUT"

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    mockReq.headers += ("Content-Type" -> List("application/rptdesign+xml"))

    val file = new File(Thread.currentThread.getContextClassLoader.getResource("in/test_def.rptdesign").getPath)

    mockReq.body = scala.xml.XML.loadFile(file)

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)

      Assert.assertEquals(resp.open_!.toResponse.code, 201)

      Assert.assertTrue(resp.open_!.isInstanceOf[PlainTextResponse])

      val defEntRsp: DefinitionEntity = DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[DefinitionEntity])

      Assert.assertEquals(defEntRsp.getDefId, testDef)
    }
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-135")))
  @Test(dependsOnMethods = Array("canPutDefs"))
  def canPutDefsWithParams() {
    val mockReq = new MockWriteAuthReq("/defs/" + testDef + "/rptdesign")
    mockReq.method = "PUT"

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    mockReq.headers += ("Content-Type" -> List("application/rptdesign+xml"))

    val file = new File(Thread.currentThread.getContextClassLoader.getResource("in/test_def_params.rptdesign").getPath)

    mockReq.body = scala.xml.XML.loadFile(file)

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)

      Assert.assertEquals(resp.open_!.toResponse.code, 201)

      val defEntRsp: DefinitionEntity = DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[DefinitionEntity])

      Assert.assertEquals(defEntRsp.getDefId, testDef)

      val params = defEntRsp.getParams

      Assert.assertTrue(params != null)

      Assert.assertTrue(params.size > 0)

      Assert.assertEquals(params.get(0).getParamName, "MinQuantityInStock")

    }
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-56"), new TestSpec(key = "ERNIE-103")))
  @Test(dependsOnMethods = Array("canPostDefs"))
  def invalidDefinitionPutReturns400() {
    val mockReq = new MockWriteAuthReq("/defs/" + testDef + "/rptdesign")
    mockReq.method = "PUT"

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    mockReq.headers += ("Content-Type" -> List("application/rptdesign+xml"))

    mockReq.body = <report><invalid>Definition</invalid></report>

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)

      Assert.assertEquals(resp.open_!.toResponse.code, 400)

      Assert.assertTrue(resp.open_!.isInstanceOf[ResponseWithReason])

    }
  }

}

class MockReadAuthReq(path: String) extends MockHttpServletRequest(path) {
  override def isUserInRole(role: String) = role match {
    case SAMLConstants.readRole => true
    case _ => false
  }
  override def getRemoteUser: String = "mockReadUser"
  addBasicAuth(getRemoteUser, "pass")
}

class MockWriteAuthReq(path: String) extends MockHttpServletRequest(path) {
  override def isUserInRole(role: String) = role match {
    case SAMLConstants.writeRole => true
    case _ => false
  }
  override def getRemoteUser: String = "mockWriteUser"
  addBasicAuth(getRemoteUser, "pass")

}

class MockRunAuthReq(path: String) extends MockHttpServletRequest(path) {
  override def isUserInRole(role: String) = role match {
    case SAMLConstants.runRole => true
    case _ => false
  }
  override def getRemoteUser: String = "mockRunUser"
  addBasicAuth(getRemoteUser, "pass")

}

class MockNoAuthReq(path: String) extends MockHttpServletRequest(path) {
  override def isUserInRole(role: String) = false
  override def getRemoteUser: String = "mockNoAuthUser"
  addBasicAuth(getRemoteUser, "pass")
}

class TestBoot extends Boot {
  private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.server.DispatchRestAPITest")

  def setUpAndBoot() {

    boot()
  }
}
