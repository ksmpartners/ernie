/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.server

import org.testng.annotations._
import net.liftweb.mockweb.{ MockWeb, WebSpec }
import bootstrap.liftweb.Boot
import net.liftweb.mocks.MockHttpServletRequest
import com.ksmpartners.ernie.server.PropertyNames._
import com.ksmpartners.ernie.server.filter.SAMLConstants._
import com.ksmpartners.ernie.model._
import org.testng.Assert
import net.liftweb.http._
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.Properties
import java.io.{ FileInputStream, File }
import com.ksmpartners.ernie.util.Utility._
import org.slf4j.{ Logger, LoggerFactory }
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonAST.{ JBool, JField, JObject }
import com.ksmpartners.common.annotations.tracematrix.{ TestSpec, TestSpecs }

class DispatchRestAPITest extends WebSpec(() => (new TestBoot).setUpAndBoot()) {

  val mapper = new ObjectMapper()
  var outputDir: File = null
  private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.server.DispatchRestAPITest")

  @AfterClass
  def shutdown() {
    DispatchRestAPI.shutdown()
    for (file <- outputDir.listFiles()) {
      recDel(file)
    }
  }

  @BeforeClass
  def setup() {
    outputDir = new File(properties.get("output.dir").toString)
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-87")))
  @Test
  def canGetJobs() {
    val mockReq = new MockReadAuthReq("/jobs")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI.apply(req).apply()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[PlainTextResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 200)
      val body = resp.open_!.asInstanceOf[PlainTextResponse].text
      val respObj = mapper.readValue(body, classOf[JobsMapResponse])
      Assert.assertNotNull(respObj.getJobStatusMap)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-85")))
  @Test
  def cantGetJobsWithoutReadAuth() {
    val mockReq = new MockNoAuthReq("/jobs")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI.apply(req).apply()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[ForbiddenResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 403)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-89")))
  @Test
  def cantGetJobsWithoutCorrectAcceptHeader() {
    val mockReq = new MockReadAuthReq("/jobs")

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI.apply(req).apply()
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

  @TestSpecs(Array(new TestSpec(key = "ERNIE-41"), new TestSpec(key = "ERNIE-42")))
  @Test
  def canGetDefs() {
    val mockReq = new MockReadAuthReq("/defs")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI.apply(req).apply()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[PlainTextResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 200)
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
      val resp = DispatchRestAPI.apply(req).apply()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[ForbiddenResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 403)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-43")))
  @Test
  def cantGetDefsWithoutCorrectAcceptHeader() {
    val mockReq = new MockReadAuthReq("/defs")

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI.apply(req).apply()
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
      val resp = DispatchRestAPI.apply(req).apply()
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
      val resp = DispatchRestAPI.apply(req).apply()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[ForbiddenResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 403)
    }
  }

  private var testJobID: Long = -1L
  private var testJobHTMLID: Long = -1L
  private var testJobCSVID: Long = -1L

  @TestSpecs(Array(new TestSpec(key = "ERNIE-53")))
  @Test
  def canPostJob() {
    val mockReq = new MockWriteAuthReq("/jobs")
    mockReq.method = "POST"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    val mockReportReq = new ReportRequest()
    mockReportReq.setDefId("test_def")
    mockReportReq.setRptType(ReportType.PDF)
    mockReq.body = DispatchRestAPI.serialize(mockReportReq)

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[PlainTextResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 201)

      val reportResponse: ReportResponse = DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[ReportResponse])
      testJobID = reportResponse.getJobId()
      Assert.assertTrue(testJobID > -1L)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-53"), new TestSpec(key = "ERNIE-66")))
  @Test
  def canPostJobHTML() {
    val mockReq = new MockWriteAuthReq("/jobs")
    mockReq.method = "POST"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    val mockReportReq = new ReportRequest()
    mockReportReq.setDefId("test_def")
    mockReportReq.setRptType(ReportType.HTML)
    mockReq.body = DispatchRestAPI.serialize(mockReportReq)

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
    mockReq.body = DispatchRestAPI.serialize(mockReportReq)

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
    mockReq.body = DispatchRestAPI.serialize(mockReportReq)

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI.apply(req).apply()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[ForbiddenResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 403)
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
    mockReq.body = DispatchRestAPI.serialize(mockReportReq)

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
      Assert.assertTrue(resp.open_!.isInstanceOf[BadResponse])
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
    mockReq.body = DispatchRestAPI.serialize(mockReportReq)

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.toResponse.headers.contains(("Content-Type", "application/vnd.ksmpartners.ernie+json")))
    }
  }

  @Test(dependsOnMethods = Array("canPostJob"))
  def canCompleteJob() {
    val mockReq = new MockReadAuthReq("/jobs/" + testJobID + "/status")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    var jobRunning = true
    while (jobRunning) {
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

  @Test(dependsOnMethods = Array("canPostJobHTML"))
  def canCompleteJobHTML() {
    val mockReq = new MockReadAuthReq("/jobs/" + testJobHTMLID + "/status")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    var jobRunning = true
    while (jobRunning) {
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
    while (jobRunning) {
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
      Assert.assertTrue(respBox.isDefined)
      Assert.assertTrue(respBox.open_!.isInstanceOf[StreamingResponse])

      val resultResp = respBox.open_!.asInstanceOf[StreamingResponse]
      Assert.assertEquals(resultResp.code, 200)
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
      val resp = DispatchRestAPI.apply(req).apply()
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
      val resp = DispatchRestAPI.apply(req).apply()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[NotAcceptableResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 406)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-60")))
  @Test(dependsOnMethods = Array("canPostJob"))
  def canGetJobStatus() {
    val mockReq = new MockReadAuthReq("/jobs/" + testJobID + "/status")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[PlainTextResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 200)
      val statusResponse: StatusResponse = DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[StatusResponse])
      Assert.assertTrue(JobStatus.values().contains(statusResponse.getJobStatus))

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

  @TestSpecs(Array(new TestSpec(key = "ERNIE-65"), new TestSpec(key = "ERNIE-66")))
  @Test(dependsOnMethods = Array("canGetCSVOutputDownload"))
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
      Assert.assertTrue(deleteResponse.getJobStatus == JobStatus.DELETED)
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
      val resp = DispatchRestAPI.apply(req).apply()
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

  class MockReadAuthReq(path: String) extends MockHttpServletRequest(path) {
    override def isUserInRole(role: String) = role match {
      case READ_ROLE => true
      case _ => false
    }
  }

  class MockWriteAuthReq(path: String) extends MockHttpServletRequest(path) {
    override def isUserInRole(role: String) = role match {
      case WRITE_ROLE => true
      case _ => false
    }
  }

  class MockNoAuthReq(path: String) extends MockHttpServletRequest(path) {
    override def isUserInRole(role: String) = false
  }

  protected val properties: Properties = {

    val propsPath = System.getProperty(PROPERTIES_FILE_NAME_PROP)

    if (null == propsPath) {
      throw new RuntimeException("System property " + PROPERTIES_FILE_NAME_PROP + " is undefined")
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
    val url = Thread.currentThread.getContextClassLoader.getResource("default.props")
    System.setProperty(PROPERTIES_FILE_NAME_PROP, url.getPath)
    boot()
  }
}
