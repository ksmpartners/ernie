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
import java.io.{ FileInputStream, File }
import com.ksmpartners.ernie.util.Utility._
import org.slf4j.{ Logger, LoggerFactory }
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonAST.{ JBool, JField, JObject }
import com.ksmpartners.common.annotations.tracematrix.{ TestSpecs, TestSpec }
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

class DispatchRestAPITest extends WebSpec(() => (new TestBoot).setUpAndBoot()) {

  var outputDir: File = null
  var jobsDir: File = null
  private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.server.DispatchRestAPITest")

  @AfterClass
  def shutdown() {
    DispatchRestAPI.shutdown()
    for (file <- outputDir.listFiles()) {
      recDel(file)
    }
    for (file <- jobsDir.listFiles()) {
      recDel(file)
    }

    var keep = new File(outputDir, ".keep")
    keep.createNewFile()
    keep = new File(jobsDir, ".keep")
    keep.createNewFile()
  }

  @BeforeClass
  def setup() {
    outputDir = new File(properties.get("output.dir").toString)
    jobsDir = new File(properties.get("jobs.dir").toString)
    if (outputDir.exists())
      recDel(outputDir)
    if (jobsDir.exists())
      recDel(jobsDir)
    outputDir.mkdir()
    jobsDir.mkdir()
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-107")))
  @Test
  def canGetJobsSupportsHead() {
    val mockReq = new MockReadAuthReq("/jobs")
    mockReq.method = "HEAD"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[InMemoryResponse])
      val resultResp = resp.open_!.asInstanceOf[InMemoryResponse]
      Assert.assertEquals(resultResp.size, 0)
      Assert.assertEquals(resultResp.code, 200)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-108")))
  @Test(dependsOnMethods = Array("canGetJobStatus"))
  def canGetJobStatusSupportsHead() {
    val mockReq = new MockReadAuthReq("/jobs/" + testJobID + "/status")
    mockReq.method = "HEAD"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[InMemoryResponse])
      val resultResp = resp.open_!.asInstanceOf[InMemoryResponse]
      Assert.assertEquals(resultResp.size, 0)
      Assert.assertEquals(resultResp.code, 200)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-109")))
  @Test(dependsOnMethods = Array("canCompleteJob"))
  def canGetOutputDownloadSupportsHead() {
    val mockReq = new MockReadAuthReq("/jobs/" + testJobID + "/result")
    mockReq.method = "HEAD"
    mockReq.headers += ("Accept" -> List("application/pdf"))

    MockWeb.testReq(mockReq) { req =>
      val respBox = DispatchRestAPI(req)()
      Assert.assertTrue(respBox.isDefined)
      Assert.assertTrue(respBox.open_!.isInstanceOf[InMemoryResponse])

      val resultResp = respBox.open_!.asInstanceOf[InMemoryResponse]
      Assert.assertEquals(resultResp.size, 0)
      Assert.assertEquals(resultResp.code, 200)
      Assert.assertTrue(resultResp.headers.contains(("Content-Type", "application/pdf")) && resultResp.headers.contains(("Content-Disposition", "attachment; filename=\"REPORT_" + testJobID + ".pdf\"")))
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-110")))
  @Test(dependsOnMethods = Array("canCompleteJob"))
  def canGetReportDetailSupportsHead() {
    val mockReq = new MockReadAuthReq("/jobs/" + testJobID + "/result/detail")
    mockReq.method = "HEAD"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[InMemoryResponse])
      val resultResp = resp.open_!.asInstanceOf[InMemoryResponse]
      Assert.assertEquals(resultResp.size, 0)
      Assert.assertEquals(resultResp.code, 200)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-111")))
  @Test
  def canGetDefDetailsSupportsHead() {
    val mockReq = new MockReadAuthReq("/defs/test_def")
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    mockReq.method = "HEAD"
    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[InMemoryResponse])
      val resultResp = resp.open_!.asInstanceOf[InMemoryResponse]
      Assert.assertEquals(resultResp.size, 0)
      Assert.assertEquals(resultResp.code, 200)
    }
  }

  @AfterMethod
  def logMethodAfter(result: java.lang.reflect.Method) {
    log.debug("END test:" + result.getName)
  }

  @BeforeMethod
  def logMethodBefore(result: java.lang.reflect.Method) {
    log.debug("BEGIN test:" + result.getName)
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-91")))
  @Test
  def jobStatusReportsFailureForUnsupportedOutput() {
    var mockReq: MockHttpServletRequest = new MockWriteAuthReq("/jobs")
    mockReq.method = "POST"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    val mockReportReq = new ReportRequest()
    mockReportReq.setDefId("test_def_nocsv")
    mockReportReq.setRptType(ReportType.CSV)
    mockReq.body = DispatchRestAPI.serialize(mockReportReq).getBytes
    var noCSVJob = -1L
    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[PlainTextResponse])
      val reportResponse: ReportResponse = DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[ReportResponse])
      noCSVJob = reportResponse.getJobId()
      Assert.assertEquals(resp.open_!.toResponse.code, 201)
    }
    mockReq = new MockReadAuthReq("/jobs/" + noCSVJob + "/status")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[PlainTextResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 200)
      val statusResponse: StatusResponse = DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[StatusResponse])
      Assert.assertEquals(statusResponse.getJobStatus, JobStatus.FAILED_UNSUPPORTED_FORMAT)

    }

  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-112")))
  @Test
  def unsupportedJobsServiceRequestsReturn405() {
    var mockReq = new MockWriteAuthReq("/jobs")
    mockReq.method = "DELETE"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    check405(mockReq)
    mockReq = new MockWriteAuthReq("/jobs")
    mockReq.method = "PUT"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    check405(mockReq)
  }
  @TestSpecs(Array(new TestSpec(key = "ERNIE-113")))
  @Test
  def unsupportedJobsStatusServiceRequestsReturn405() {
    var mockReq = new MockWriteAuthReq("/jobs/1/status")
    mockReq.method = "DELETE"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    check405(mockReq)
    mockReq = new MockWriteAuthReq("/jobs/1/status")
    mockReq.method = "PUT"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    check405(mockReq)
    mockReq = new MockWriteAuthReq("/jobs/1/status")
    mockReq.method = "POST"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    check405(mockReq)
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-114")))
  @Test
  def unsupportedJobsResultsServiceRequestsReturn405() {
    var mockReq = new MockWriteAuthReq("/jobs/1/result")
    mockReq.method = "PUT"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    check405(mockReq)
    mockReq = new MockWriteAuthReq("/jobs/1/result")
    mockReq.method = "POST"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    check405(mockReq)
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-115")))
  @Test
  def unsupportedDefsServiceRequestsReturn405() {
    var mockReq = new MockWriteAuthReq("/defs")
    mockReq.method = "PUT"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    check405(mockReq)
    mockReq = new MockWriteAuthReq("/defs")
    mockReq.method = "DELETE"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    check405(mockReq)
    mockReq = new MockWriteAuthReq("/defs/test")
    mockReq.method = "POST"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    check405(mockReq)
  }

  def check405(m: MockHttpServletRequest) {
    MockWeb.testReq(m) { req =>
      {
        val resp = DispatchRestAPI(req)()
        Assert.assertTrue(resp.isDefined)
        Assert.assertTrue(resp.open_!.isInstanceOf[MethodNotAllowedResponse])
        Assert.assertEquals(resp.open_!.toResponse.code, 405)
      }
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-127")))
  @Test
  def cantPurgeReportResultsWithoutWriteAuth() {
    val mockReq = new MockNoAuthReq("/jobs/expired")
    mockReq.method = "DELETE"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[ForbiddenResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 403)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-129")))
  @Test
  def canPurgeReportResults() {
    val mockReq = new MockWriteAuthReq("/jobs/expired")
    mockReq.method = "DELETE"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[OkResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 200)

    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-131")))
  @Test
  def cantPurgeReportResultsWithoutJSONRequest() {
    val mockReq = new MockWriteAuthReq("/jobs/expired")
    mockReq.method = "DELETE"
    mockReq.headers += ("Accept" -> List("application/vnd.ksmpartners.ernie+xml"))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[NotAcceptableResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 406)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-87")))
  @Test
  def canGetJobs() {
    val mockReq = new MockReadAuthReq("/jobs")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[PlainTextResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 200)
      val body = resp.open_!.asInstanceOf[PlainTextResponse].text
      val respObj = mapper.readValue(body, classOf[JobsMapResponse])
      Assert.assertNotNull(respObj.getJobStatusMap)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-103")))
  @Test(dependsOnMethods = Array("canPostDefs"))
  def cantReplaceReportDefsForInvalidDefFile() {
    val mockReq = new MockWriteAuthReq("/defs/" + testDef + "/rptdesign")
    mockReq.method = "PUT"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    mockReq.headers += ("Content-Type" -> List("application/rptdesign+xml"))

    mockReq.body = <invalidrequest.html/>

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[ResponseWithReason])
      Assert.assertEquals(resp.open_!.toResponse.code, 400)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-85")))
  @Test
  def cantGetJobsWithoutReadAuth() {
    val mockReq = new MockNoAuthReq("/jobs")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[ForbiddenResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 403)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-89")))
  @Test
  def cantGetJobsWithoutCorrectAcceptHeader() {
    val mockReq = new MockReadAuthReq("/jobs")
    mockReq.headers += ("Accept" -> List("application/vnd.ksmpartners.ernie+xml"))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[NotAcceptableResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 406)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-88")))
  @Test
  def jobsListServiceReturnsJSON() {
    val mockReq = new MockReadAuthReq("/jobs")
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.toResponse.headers.contains(("Content-Type", "application/vnd.ksmpartners.ernie+json")))
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-41")))
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

  @TestSpecs(Array(new TestSpec(key = "ERNIE-42")))
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

  @TestSpecs(Array(new TestSpec(key = "ERNIE-39")))
  @Test
  def cantGetDefsWithoutReadAuth() {
    val mockReq = new MockNoAuthReq("/defs")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[ForbiddenResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 403)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-43")))
  @Test
  def cantGetDefsWithoutCorrectAcceptHeader() {
    val mockReq = new MockReadAuthReq("/defs")
    mockReq.headers += ("Accept" -> List("application/vnd.ksmpartners.ernie+xml"))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[NotAcceptableResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 406)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-62")))
  @Test
  def cantGetJobStatusWithoutJSONRequest() {
    val mockReq = new MockReadAuthReq("/jobs/1/status")
    mockReq.headers += ("Accept" -> List("application/vnd.ksmpartners.ernie+xml"))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[NotAcceptableResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 406)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-58")))
  @Test
  def cantGetJobStatusWithoutReadAuth() {
    val mockReq = new MockNoAuthReq("/jobs/1/status")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[ForbiddenResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 403)
    }
  }

  private var testJobID: Long = -1L
  private var testJobHTMLID: Long = -1L
  private var testJobCSVID: Long = -1L

  @TestSpecs(Array(new TestSpec(key = "ERNIE-53"), new TestSpec(key = "ERNIE-104")))
  @Test(dependsOnMethods = Array("canPutDefs"))
  def canPostJob() {
    val mockReq = new MockWriteAuthReq("/jobs")
    mockReq.method = "POST"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    val mockReportReq = new ReportRequest()
    mockReportReq.setDefId(testDef)
    mockReportReq.setRptType(ReportType.PDF)
    mockReq.body = DispatchRestAPI.serialize(mockReportReq).getBytes

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[PlainTextResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 201)
      val reportResponse: ReportResponse = DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[ReportResponse])
      testJobID = reportResponse.getJobId()
      Assert.assertTrue(testJobID > -1L)
      Assert.assertTrue(resp.open_!.toResponse.headers.contains(("Location", req.hostAndPath + "/jobs/" + testJobID)))
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-148")))
  @Test
  def canPostJobAsRunUser() {
    val mockReq = new MockRunAuthReq("/jobs")
    mockReq.method = "POST"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    val mockReportReq = new ReportRequest()
    mockReportReq.setDefId("test_def")
    mockReportReq.setRptType(ReportType.PDF)
    mockReq.body = DispatchRestAPI.serialize(mockReportReq).getBytes

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[PlainTextResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 201)
      val reportResponse: ReportResponse = DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[ReportResponse])
      testJobID = reportResponse.getJobId()
      Assert.assertTrue(testJobID > -1L)
      Assert.assertTrue(resp.open_!.toResponse.headers.contains(("Location", req.hostAndPath + "/jobs/" + testJobID)))
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-53")))
  @Test
  def canPostJobHTML() {
    val mockReq = new MockWriteAuthReq("/jobs")
    mockReq.method = "POST"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    val mockReportReq = new ReportRequest()
    mockReportReq.setDefId("test_def")
    mockReportReq.setRptType(ReportType.HTML)
    mockReq.body = DispatchRestAPI.serialize(mockReportReq).getBytes

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[PlainTextResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 201)

      val reportResponse: ReportResponse = DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[ReportResponse])
      testJobHTMLID = reportResponse.getJobId()
      Assert.assertTrue(testJobHTMLID > -1L)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-53"), new TestSpec(key = "ERNIE-66")))
  @Test
  def canPostJobCSV() {
    val mockReq = new MockWriteAuthReq("/jobs")
    mockReq.method = "POST"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    val mockReportReq = new ReportRequest()
    mockReportReq.setDefId("test_def")
    mockReportReq.setRptType(ReportType.CSV)
    mockReq.body = DispatchRestAPI.serialize(mockReportReq).getBytes

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[PlainTextResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 201)

      val reportResponse: ReportResponse = DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[ReportResponse])
      testJobCSVID = reportResponse.getJobId()
      Assert.assertTrue(testJobCSVID > -1L)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-51")))
  @Test
  def cantPostJobWithoutWriteAuth() {
    val mockReq = new MockNoAuthReq("/jobs")
    mockReq.method = "POST"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    val mockReportReq = new ReportRequest()
    mockReportReq.setDefId("test_def")
    mockReportReq.setRptType(ReportType.HTML)
    mockReq.body = DispatchRestAPI.serialize(mockReportReq).getBytes

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[ForbiddenResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 403)
    }
  }

  @Test
  def cantPostJobWithInvalidDefID() {
    val mockReq = new MockWriteAuthReq("/jobs")
    mockReq.method = "POST"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    val mockReportReq = new ReportRequest()
    mockReportReq.setDefId("INVALID_DEF")
    mockReportReq.setRptType(ReportType.HTML)
    mockReq.body = DispatchRestAPI.serialize(mockReportReq).getBytes

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[ResponseWithReason])
      Assert.assertEquals(resp.open_!.asInstanceOf[ResponseWithReason].reason, "No such definition ID")
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-55")))
  @Test
  def cantPostJobWithoutJSONRequest() {
    val mockReq = new MockWriteAuthReq("/jobs")
    mockReq.method = "POST"
    mockReq.headers += ("Accept" -> List("application/vnd.ksmpartners.ernie+xml"))

    val mockReportReq = new ReportRequest()
    mockReportReq.setDefId("test_def")
    mockReportReq.setRptType(ReportType.HTML)
    mockReq.body = DispatchRestAPI.serialize(mockReportReq).getBytes

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[NotAcceptableResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 406)
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

  @TestSpecs(Array(new TestSpec(key = "ERNIE-57")))
  @Test
  def cantPostJobWithoutValidRequestJSON() {
    val mockReq = new MockWriteAuthReq("/jobs")
    mockReq.method = "POST"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    mockReq.body = JObject(List(JField("Invalid JSON?", JBool(true))))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[ResponseWithReason])
      Assert.assertEquals(resp.open_!.toResponse.code, 400)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-54")))
  @Test
  def reportsServiceReturnsJSON() {
    val mockReq = new MockWriteAuthReq("/jobs")
    mockReq.method = "POST"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    val mockReportReq = new ReportRequest()
    mockReportReq.setDefId("test_def")
    mockReportReq.setRptType(ReportType.HTML)
    mockReq.body = DispatchRestAPI.serialize(mockReportReq).getBytes

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.toResponse.headers.contains(("Content-Type", "application/vnd.ksmpartners.ernie+json")))
    }
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-120")))
  //@Test(dependsOnMethods = Array("canPostJob"))
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
  @TestSpecs(Array(new TestSpec(key = "ERNIE-120")))
  @Test(dependsOnMethods = Array("canPostJob"))
  def canCompleteJob() {
    val mockReq = new MockReadAuthReq("/jobs/" + testJobID + "/status")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    var triedDelInUseDef = false
    var jobRunning = true
    val end = System.currentTimeMillis + (1000 * 300)
    while (jobRunning && (System.currentTimeMillis < end)) {
      MockWeb.testReq(mockReq) { req =>
        val resp = DispatchRestAPI(req)()
        resp.map(r =>
          if (r.isInstanceOf[PlainTextResponse])
            if (DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[StatusResponse]).getJobStatus == JobStatus.COMPLETE) {
            jobRunning = false
            Assert.assertTrue(DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[StatusResponse]).getJobStatus == JobStatus.COMPLETE)
          } else {
            if (!triedDelInUseDef) cantDeleteInUseDef()
            triedDelInUseDef = true
          })
      }
    }
  }

  @Test(dependsOnMethods = Array("canPostJobHTML"))
  def canCompleteJobHTML() {
    val mockReq = new MockReadAuthReq("/jobs/" + testJobHTMLID + "/status")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    var jobRunning = true
    val end = System.currentTimeMillis + (1000 * 300)
    while (jobRunning && (System.currentTimeMillis < end)) {
      MockWeb.testReq(mockReq) { req =>
        val resp = DispatchRestAPI(req)()
        resp.map(r =>
          if (r.isInstanceOf[PlainTextResponse])
            if (DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[StatusResponse]).getJobStatus == JobStatus.COMPLETE) {
            jobRunning = false
            Assert.assertTrue(DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[StatusResponse]).getJobStatus == JobStatus.COMPLETE)
          })
      }
    }

  }

  @Test(dependsOnMethods = Array("canPostJobCSV"))
  def canCompleteJobCSV() {
    val mockReq = new MockReadAuthReq("/jobs/" + testJobCSVID + "/status")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    var jobRunning = true
    val end = System.currentTimeMillis + (1000 * 300)
    while (jobRunning && (System.currentTimeMillis < end)) {
      MockWeb.testReq(mockReq) { req =>
        val resp = DispatchRestAPI(req)()
        resp.map(r =>
          if (r.isInstanceOf[PlainTextResponse])
            if (DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[StatusResponse]).getJobStatus == JobStatus.COMPLETE) {
            jobRunning = false
            Assert.assertTrue(DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[StatusResponse]).getJobStatus == JobStatus.COMPLETE)
          })
      }
    }

  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-65"), new TestSpec(key = "ERNIE-66")))
  @Test(dependsOnMethods = Array("canCompleteJob"))
  def canGetOutputDownload() {
    val mockReq = new MockReadAuthReq("/jobs/" + testJobID + "/result")

    mockReq.headers += ("Accept" -> List("application/pdf"))

    MockWeb.testReq(mockReq) { req =>
      val respBox = DispatchRestAPI(req)()
      Assert.assertTrue(respBox.isDefined)
      Assert.assertTrue(respBox.open_!.isInstanceOf[StreamingResponse])

      val resultResp = respBox.open_!.asInstanceOf[StreamingResponse]
      Assert.assertEquals(resultResp.code, 200)
      Assert.assertTrue(resultResp.headers.contains(("Content-Type", "application/pdf")) && resultResp.headers.contains(("Content-Disposition", "attachment; filename=\"REPORT_" + testJobID + ".pdf\"")))
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-65"), new TestSpec(key = "ERNIE-66")))
  @Test(dependsOnMethods = Array("canCompleteJobHTML"))
  def canGetHTMLOutputDownload() {
    val mockReq = new MockReadAuthReq("/jobs/" + testJobHTMLID + "/result")

    mockReq.headers += ("Accept" -> List("application/html"))

    MockWeb.testReq(mockReq) { req =>
      val respBox = DispatchRestAPI(req)()
      Assert.assertTrue(respBox.isDefined, "Response is not defined")
      Assert.assertTrue(respBox.open_!.isInstanceOf[StreamingResponse], "Response is not of type StreamingResponse")

      val resultResp = respBox.open_!.asInstanceOf[StreamingResponse]
      Assert.assertEquals(resultResp.code, 200, "Status code is not 200")
      Assert.assertTrue(resultResp.headers.contains(("Content-Type", "application/html")) && resultResp.headers.contains(("Content-Disposition", "attachment; filename=\"REPORT_" + testJobHTMLID + ".html\"")))
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-65"), new TestSpec(key = "ERNIE-66")))
  @Test(dependsOnMethods = Array("canCompleteJobCSV"))
  def canGetCSVOutputDownload() {
    val mockReq = new MockReadAuthReq("/jobs/" + testJobCSVID + "/result")

    mockReq.headers += ("Accept" -> List("application/csv"))

    MockWeb.testReq(mockReq) { req =>
      val respBox = DispatchRestAPI(req)()
      Assert.assertTrue(respBox.isDefined)
      Assert.assertTrue(respBox.open_!.isInstanceOf[StreamingResponse])

      val resultResp = respBox.open_!.asInstanceOf[StreamingResponse]
      Assert.assertEquals(resultResp.code, 200)
      Assert.assertTrue(resultResp.headers.contains(("Content-Type", "application/csv")) && resultResp.headers.contains(("Content-Disposition", "attachment; filename=\"REPORT_" + testJobCSVID + ".csv\"")))
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-63")))
  @Test(dependsOnMethods = Array("canCompleteJob"))
  def cantGetOutputDownloadWithoutReadAuth() {
    val mockReq = new MockNoAuthReq("/jobs/" + testJobID + "/result")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[ForbiddenResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 403)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-67")))
  @Test(dependsOnMethods = Array("canCompleteJob"))
  def cantGetOutputDownloadWithoutCorrectAcceptHeader() {
    val mockReq = new MockReadAuthReq("/jobs/" + testJobID + "/result")

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[NotAcceptableResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 406)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-67")))
  @Test(dependsOnMethods = Array("canCompleteJob"))
  def cantGetPDFOutputDownloadWithCSVAcceptHeader() {
    val mockReq = new MockReadAuthReq("/jobs/" + testJobID + "/result")
    mockReq.headers += ("Accept" -> List("application/csv"))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertEquals(resp.open_!.getClass, classOf[NotAcceptableResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 406)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-67")))
  @Test(dependsOnMethods = Array("canCompleteJob"))
  def cantGetPDFOutputDownloadWithHTMLAcceptHeader() {
    val mockReq = new MockReadAuthReq("/jobs/" + testJobID + "/result")
    mockReq.headers += ("Accept" -> List("application/html"))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[NotAcceptableResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 406)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-60")))
  @Test(dependsOnMethods = Array("canCompleteJob"))
  def canGetJobStatus() {
    val mockReq = new MockReadAuthReq("/jobs/" + testJobID + "/status")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[PlainTextResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 200)
      val statusResponse: StatusResponse = DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[StatusResponse])
      Assert.assertEquals(statusResponse.getJobStatus, JobStatus.COMPLETE)

    }
  }

  @Test
  def cantGetJobStatusWithoutLongJobID() {
    val mockReq = new MockReadAuthReq("/jobs/test/status")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[ResponseWithReason])
      Assert.assertEquals(resp.open_!.asInstanceOf[ResponseWithReason].reason, "Job ID provided is not a number: test")
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-61")))
  @Test(dependsOnMethods = Array("canPostJob"))
  def jobStatusServiceReturnsJSON() {
    val mockReq = new MockReadAuthReq("/jobs/" + testJobID + "/status")
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.toResponse.headers.contains(("Content-Type", "application/vnd.ksmpartners.ernie+json")))
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-74")))
  @Test(dependsOnMethods = Array("canGetCompleteJobsCatalog"))
  def canDeleteReportResults() {
    val mockReq = new MockWriteAuthReq("/jobs/" + testJobCSVID + "/result")
    mockReq.method = "DELETE"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[PlainTextResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 200)
      val deleteResponse: DeleteResponse = DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[DeleteResponse])
      Assert.assertTrue(deleteResponse.getDeleteStatus == DeleteStatus.SUCCESS)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-76")))
  @Test
  def cantDeleteReportResultsWithoutJSONRequest() {
    val mockReq = new MockWriteAuthReq("/jobs/1/result")
    mockReq.method = "DELETE"
    mockReq.headers += ("Accept" -> List("application/vnd.ksmpartners.ernie+xml"))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[NotAcceptableResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 406)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-75")))
  @Test(dependsOnMethods = Array("canGetHTMLOutputDownload"))
  def deleteReportResultsReturnsJSON() {
    val mockReq = new MockWriteAuthReq("/jobs/" + testJobHTMLID + "/result")
    mockReq.method = "DELETE"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.toResponse.headers.contains(("Content-Type", "application/vnd.ksmpartners.ernie+json")))
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-72")))
  @Test
  def cantDeleteReportResultsWithoutWriteAuth() {
    val mockReq = new MockNoAuthReq("/jobs/1/result")
    mockReq.method = "DELETE"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[ForbiddenResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 403)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-69")))
  @Test(dependsOnMethods = Array("canDeleteReportResults"))
  def jobStatusReturns410ForDeletedReports() {
    val mockReq = new MockReadAuthReq("/jobs/" + testJobCSVID + "/status")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[GoneResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 410)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-70")))
  @Test(dependsOnMethods = Array("canDeleteReportResults"))
  def downloadServiceReturns410ForDeletedReports() {
    val mockReq = new MockReadAuthReq("/jobs/" + testJobCSVID + "/result")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[GoneResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 410)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-80")))
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

  @TestSpecs(Array(new TestSpec(key = "ERNIE-82")))
  @Test
  def cantGetDefDetailsWithoutJSONRequest() {
    val mockReq = new MockReadAuthReq("/defs/test_def")
    mockReq.headers += ("Accept" -> List("application/vnd.ksmpartners.ernie+xml"))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI.apply(req).apply()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[NotAcceptableResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 406)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-78")))
  @Test
  def cantGetDefDetailsWithoutReadAuth() {
    val mockReq = new MockNoAuthReq("/defs/test_def")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI.apply(req).apply()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[ForbiddenResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 403)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-81")))
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

  @TestSpecs(Array(new TestSpec(key = "ERNIE-96")))
  @Test
  def cantGetDefDetailForNonExistentReportDef() {
    val mockReq = new MockReadAuthReq("/defs/invalid_def")
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI.apply(req).apply()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[NotFoundResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 404)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-80")))
  @Test
  def canGetDefDetail() {
    val mockReq = new MockReadAuthReq("/defs/test_def")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[PlainTextResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 200)
      val defDetailResponse: DefinitionEntity = DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[DefinitionEntity])
      // Assert.assertTrue(JobStatus.values().contains(statusResponse.getJobStatus))

    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-71")))
  @Test
  def cantPostJobIfRetentionDateInPast() {
    val mockReq = new MockWriteAuthReq("/jobs")
    mockReq.method = "POST"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    val mockReportReq = new ReportRequest()
    mockReportReq.setDefId("test_def")
    mockReportReq.setRptType(ReportType.PDF)
    mockReportReq.setRetentionDays(-1)
    mockReq.body = DispatchRestAPI.serialize(mockReportReq)

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[ResponseWithReason])
      Assert.assertEquals(resp.open_!.toResponse.code, 400)
      Assert.assertEquals(resp.open_!.asInstanceOf[ResponseWithReason].reason, "Retention date before request time")
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-12")))
  @Test
  def cantPostJobIfRetentionDateBeyondMaximum() {
    val mockReq = new MockWriteAuthReq("/jobs")
    mockReq.method = "POST"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    val mockReportReq = new ReportRequest()
    mockReportReq.setDefId("test_def")
    mockReportReq.setRptType(ReportType.PDF)
    mockReportReq.setRetentionDays(9999)
    mockReq.body = DispatchRestAPI.serialize(mockReportReq)

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[ResponseWithReason])
      Assert.assertEquals(resp.open_!.toResponse.code, 400)
      Assert.assertEquals(resp.open_!.asInstanceOf[ResponseWithReason].reason, "Retention date exceeds maximum")
    }
  }

  var testDef = ""

  @TestSpecs(Array(new TestSpec(key = "ERNIE-47"), new TestSpec(key = "ERNIE-49"), new TestSpec(key = "ERNIE-162")))
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
      Assert.assertEquals(defEntRsp.getCreatedUser, "default")
      testDef = defEntRsp.getDefId
      Assert.assertTrue(resp.open_!.toResponse.headers.contains(("Location", req.hostAndPath + "/defs/" + defEntRsp.getDefId)))

    }

  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-48"), new TestSpec(key = "ERNIE-100"), new TestSpec(key = "ERNIE-101")))
  @Test(dependsOnMethods = Array("canPostDefs"))
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

  @TestSpecs(Array(new TestSpec(key = "ERNIE-135")))
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

  @TestSpecs(Array(new TestSpec(key = "ERNIE-94")))
  @Test
  def cantDeleteDefsWithoutWriteAuth() {
    val mockReq = new MockNoAuthReq("/defs/test_def2")
    mockReq.method = "DELETE"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertEquals(resp.open_!.toResponse.code, 403)
      Assert.assertTrue(resp.open_!.isInstanceOf[ForbiddenResponse])
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-92"), new TestSpec(key = "ERNIE-95")))
  @Test
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
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-97")))
  @Test
  def cantDeleteDefsWithoutCorrectAcceptHeader() {

    val mockReq = new MockWriteAuthReq("/defs/test_def2")
    mockReq.method = "DELETE"

    mockReq.headers += ("Accept" -> List("application/vnd.ksmpartners.ernie+xml"))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[NotAcceptableResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 406)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-56")))
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

  @TestSpecs(Array(new TestSpec(key = "ERNIE-45")))
  @Test
  def cantPostDefsWithoutWriteAuth() {
    val mockReq = new MockNoAuthReq("/defs")
    mockReq.method = "POST"

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    val defEnt = new DefinitionEntity()
    defEnt.setCreatedUser("default")
    defEnt.setDefId("test_def2")
    mockReq.body = DispatchRestAPI.serialize(defEnt).getBytes

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)

      Assert.assertEquals(resp.open_!.toResponse.code, 403)

      Assert.assertTrue(resp.open_!.isInstanceOf[ForbiddenResponse])

    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-50")))
  @Test
  def cantPostDefsWithoutJSONRequest() {
    val mockReq = new MockWriteAuthReq("/defs")
    mockReq.method = "POST"

    mockReq.headers += ("Accept" -> List("application/vnd.ksmpartners.ernie+xml"))

    val defEnt = new DefinitionEntity()
    defEnt.setCreatedUser("default")
    defEnt.setDefId("test_def2")
    mockReq.body = DispatchRestAPI.serialize(defEnt).getBytes

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)

      Assert.assertEquals(resp.open_!.toResponse.code, 406)

      Assert.assertTrue(resp.open_!.isInstanceOf[NotAcceptableResponse])

    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-98")))
  @Test
  def cantPutDefsWithoutWriteAuth() {
    val mockReq = new MockNoAuthReq("/defs/test_def2/rptdesign")
    mockReq.method = "PUT"

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    mockReq.headers += ("Content-Type" -> List("application/rptdesign+xml"))

    val file = new File(Thread.currentThread.getContextClassLoader.getResource("in/test_def.rptdesign").getPath)

    mockReq.body = scala.xml.XML.loadFile(file)

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)

      Assert.assertEquals(resp.open_!.toResponse.code, 403)

      Assert.assertTrue(resp.open_!.isInstanceOf[ForbiddenResponse])

    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-102")))
  @Test
  def cantPutDefsWithoutJSONRequest() {
    val mockReq = new MockWriteAuthReq("/defs/test_def2/rptdesign")
    mockReq.method = "PUT"

    mockReq.headers += ("Accept" -> List("application/vnd.ksmpartners.ernie+xml"))

    mockReq.headers += ("Content-Type" -> List("application/rptdesign+xml"))

    val file = new File(Thread.currentThread.getContextClassLoader.getResource("in/test_def.rptdesign").getPath)

    mockReq.body = scala.xml.XML.loadFile(file)

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)

      Assert.assertEquals(resp.open_!.toResponse.code, 406)

      Assert.assertTrue(resp.open_!.isInstanceOf[NotAcceptableResponse])

    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-133")))
  @Test(dependsOnMethods = Array("canPutDefsWithParams"))
  def cantPostJobWithInvalidParamDataTypes() {
    var mockReq: MockHttpServletRequest = new MockWriteAuthReq("/jobs")
    mockReq.method = "POST"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    val mockReportReq = new ReportRequest()
    mockReportReq.setDefId(testDef)
    mockReportReq.setRptType(ReportType.PDF)
    val rptParams: java.util.HashMap[String, String] = new java.util.HashMap[String, String]()
    rptParams.put("MinQuantityInStock", "string data")
    mockReportReq.setReportParameters(rptParams)
    mockReq.body = DispatchRestAPI.serialize(mockReportReq).getBytes
    var testParamsJobID = -1L
    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()

      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[PlainTextResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 201)
      val reportResponse: ReportResponse = DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[ReportResponse])
      testParamsJobID = reportResponse.getJobId()
      Assert.assertTrue(testParamsJobID > -1L)
    }

    mockReq = new MockReadAuthReq("/jobs/" + testParamsJobID + "/status")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    var jobRunning = true
    val end = System.currentTimeMillis + (1000 * 300)
    while (jobRunning && (System.currentTimeMillis < end)) {
      MockWeb.testReq(mockReq) { req =>
        val resp = DispatchRestAPI(req)()
        resp.map(r =>
          if (r.isInstanceOf[PlainTextResponse])
            if (DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[StatusResponse]).getJobStatus != JobStatus.IN_PROGRESS) {
            jobRunning = false
            Assert.assertTrue(DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[StatusResponse]).getJobStatus == JobStatus.FAILED_INVALID_PARAMETER_VALUES)
          })
      }
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-133")))
  @Test(dependsOnMethods = Array("canPutDefsWithParams"))
  def cantPostJobWithoutNonNullParam() {
    var mockReq: MockHttpServletRequest = new MockWriteAuthReq("/jobs")
    mockReq.method = "POST"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    val mockReportReq = new ReportRequest()
    mockReportReq.setDefId(testDef)
    mockReportReq.setRptType(ReportType.PDF)

    mockReq.body = DispatchRestAPI.serialize(mockReportReq).getBytes
    var testParamsJobID = -1L
    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()

      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[PlainTextResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 201)
      val reportResponse: ReportResponse = DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[ReportResponse])
      testParamsJobID = reportResponse.getJobId()
      Assert.assertTrue(testParamsJobID > -1L)
    }

    mockReq = new MockReadAuthReq("/jobs/" + testParamsJobID + "/status")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    var jobRunning = true
    val end = System.currentTimeMillis + (1000 * 300)
    while (jobRunning && (System.currentTimeMillis < end)) {
      MockWeb.testReq(mockReq) { req =>
        val resp = DispatchRestAPI(req)()
        resp.map(r =>
          if (r.isInstanceOf[PlainTextResponse])
            if (DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[StatusResponse]).getJobStatus != JobStatus.IN_PROGRESS) {
            jobRunning = false
            Assert.assertTrue(DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[StatusResponse]).getJobStatus == JobStatus.FAILED_PARAMETER_NULL)
          })
      }
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-134")))
  @Test(dependsOnMethods = Array("canPutDefsWithParams"))
  def canPostJobWithParams() {
    val mockReq = new MockWriteAuthReq("/jobs")
    mockReq.method = "POST"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    val mockReportReq = new ReportRequest()
    mockReportReq.setDefId(testDef)
    mockReportReq.setRptType(ReportType.PDF)
    val rptParams: java.util.HashMap[String, String] = new java.util.HashMap[String, String]()
    rptParams.put("MinQuantityInStock", "500")
    mockReportReq.setReportParameters(rptParams)
    mockReq.body = DispatchRestAPI.serialize(mockReportReq).getBytes

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()

      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[PlainTextResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 201)
      val reportResponse: ReportResponse = DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[ReportResponse])
      val testParamsJobID = reportResponse.getJobId()
      Assert.assertTrue(testParamsJobID > -1L)
      Assert.assertTrue(resp.open_!.toResponse.headers.contains(("Location", req.hostAndPath + "/jobs/" + testParamsJobID)))
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-145"), new TestSpec(key = "ERNIE-146"), new TestSpec(key = "ERNIE-159")))
  @Test(dependsOnMethods = Array("canCompleteJob"))
  def canGetReportDetail() {
    val mockReq = new MockReadAuthReq("/jobs/" + testJobID + "/result/detail")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[PlainTextResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 200)
      val rptDetailResponse: ReportEntity = DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[ReportEntity])
      Assert.assertEquals(rptDetailResponse.getRptId, jobToRptId(testJobID))
      Assert.assertEquals(rptDetailResponse.getCreatedUser, "mockWriteUser")
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-147")))
  @Test(dependsOnMethods = Array("canCompleteJob"))
  def cantGetReportDetailWithoutJSONRequest() {

    val mockReq = new MockReadAuthReq("/jobs/1/result/detail")
    mockReq.headers += ("Accept" -> List("application/vnd.ksmpartners.ernie+xml"))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[NotAcceptableResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 406)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-143")))
  @Test(dependsOnMethods = Array("canCompleteJob"))
  def cantGetReportDetailWithoutReadAuth() {
    val mockReq = new MockNoAuthReq("/jobs/1/result/detail")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[ForbiddenResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 403)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-139"), new TestSpec(key = "ERNIE-140")))
  @Test(dependsOnMethods = Array("canCompleteJob"))
  def canGetJobDetail() {
    val mockReq = new MockReadAuthReq("/jobs/" + testJobID)

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[PlainTextResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 200)
      val jobDetailResponse: JobEntity = DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[JobEntity])
      Assert.assertEquals(jobDetailResponse.getJobStatus, JobStatus.COMPLETE)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-141")))
  @Test(dependsOnMethods = Array("canCompleteJob"))
  def cantGetJobDetailWithoutJSONRequest() {

    val mockReq = new MockReadAuthReq("/jobs/1")
    mockReq.headers += ("Accept" -> List("application/vnd.ksmpartners.ernie+xml"))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[NotAcceptableResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 406)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-137")))
  @Test(dependsOnMethods = Array("canCompleteJob"))
  def cantGetJobDetailWithoutReadAuth() {
    val mockReq = new MockNoAuthReq("/jobs/1")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[ForbiddenResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 403)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-152"), new TestSpec(key = "ERNIE-153")))
  @Test(dependsOnMethods = Array("canGetCSVOutputDownload"))
  def canGetJobsCatalog() {
    val mockReq = new MockReadAuthReq("/jobs/catalog")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[PlainTextResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 200)
      val jobCatalogResp: JobsCatalogResponse = DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[JobsCatalogResponse])
      Assert.assertTrue(jobCatalogResp.getJobsCatalog.size > 0)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-154")))
  @Test(dependsOnMethods = Array("canGetCSVOutputDownload"))
  def cantGetJobsCatalogWithoutJSONAcceptHeader() {
    val mockReq = new MockReadAuthReq("/jobs/catalog")
    mockReq.headers += ("Accept" -> List("application/vnd.ksmpartners.ernie+xml"))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[NotAcceptableResponse])

    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-150")))
  @Test(dependsOnMethods = Array("canGetCSVOutputDownload"))
  def cantGetJobsCatalogWithoutReadAuth() {
    val mockReq = new MockNoAuthReq("/jobs/catalog")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[ForbiddenResponse])

    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-155")))
  @Test(dependsOnMethods = Array("canGetCSVOutputDownload"))
  def canGetCompleteJobsCatalog() {
    val mockReq = new MockReadAuthReq("/jobs/complete")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[PlainTextResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 200)
      val jobCatalogResp: JobsCatalogResponse = DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[JobsCatalogResponse])
      Assert.assertTrue(jobCatalogResp.getJobsCatalog.size > 0)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-157")))
  @Test(dependsOnMethods = Array("canGetCSVOutputDownload"))
  def canGetExpiredJobsCatalog() {
    val mockReq = new MockReadAuthReq("/jobs/expired")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[PlainTextResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 200)
      val jobCatalogResp: JobsCatalogResponse = DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[JobsCatalogResponse])
      Assert.assertTrue(jobCatalogResp.getJobsCatalog.size == 0)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-158")))
  @Test(dependsOnMethods = Array("canGetCSVOutputDownload"))
  def canGetFailedJobsCatalog() {
    val mockReq = new MockReadAuthReq("/jobs/failed")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[PlainTextResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 200)
      val jobCatalogResp: JobsCatalogResponse = DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[JobsCatalogResponse])
      // Assert.assertEquals(jobCatalogResp.getJobsCatalog.size, 2)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-156")))
  @Test(dependsOnMethods = Array("canDeleteReportResults"))
  def canGetDeletedJobsCatalog() {
    val mockReq = new MockReadAuthReq("/jobs/deleted")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[PlainTextResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 200)
      val jobCatalogResp: JobsCatalogResponse = DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[JobsCatalogResponse])
      import JavaConversions._
      Assert.assertTrue(jobCatalogResp.getJobsCatalog.toList.filter(p => p.getJobId == testJobCSVID).length > 0)
    }
  }

  class MockReadAuthReq(path: String) extends MockHttpServletRequest(path) {
    override def isUserInRole(role: String) = role match {
      case SAMLConstants.readRole => true
      case _ => false
    }
    override def getRemoteUser: String = "mockReadUser"
  }

  class MockWriteAuthReq(path: String) extends MockHttpServletRequest(path) {
    override def isUserInRole(role: String) = role match {
      case SAMLConstants.writeRole => true
      case _ => false
    }
    override def getRemoteUser: String = "mockWriteUser"

  }

  class MockRunAuthReq(path: String) extends MockHttpServletRequest(path) {
    override def isUserInRole(role: String) = role match {
      case SAMLConstants.runRole => true
      case _ => false
    }
    override def getRemoteUser: String = "mockRunUser"

  }

  class MockNoAuthReq(path: String) extends MockHttpServletRequest(path) {
    override def isUserInRole(role: String) = false
    override def getRemoteUser: String = "mockNoAuthUser"

  }

  protected val properties: Properties = {

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

}

class TestBoot extends Boot {
  def setUpAndBoot() {
    val url = Thread.currentThread.getContextClassLoader.getResource("test.props")
    System.setProperty(propertiesFileNameProp, url.getPath)

    boot()
  }
}
