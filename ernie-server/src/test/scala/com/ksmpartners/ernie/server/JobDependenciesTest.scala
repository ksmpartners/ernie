/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.server

import com.ksmpartners.ernie.engine.{ ShutDownRequest, Coordinator }
import com.ksmpartners.ernie.engine.report.{ MemoryReportManager, ReportGenerator, ReportManager, ReportGeneratorFactory }
import com.ksmpartners.ernie.model
import model.{ DefinitionEntity, ReportType }
import com.ksmpartners.ernie.util.FileUtils._
import java.io.{ Closeable, OutputStream, InputStream }
import org.testng.annotations.{ AfterTest, BeforeTest, Test }
import net.liftweb.common.Full
import net.liftweb.http.{ StreamingResponse, PlainTextResponse, BadResponse }
import org.testng.Assert
import java.util.Date
import collection.mutable

class JobDependenciesTest extends JobDependencies with JsonTranslator {

  val reportManager = new MemoryReportManager

  val coordinator: Coordinator = {
    val coord = new Coordinator(reportManager) with TestReportGeneratorFactory
    coord.start()
    coord
  }

  @BeforeTest
  def setup() {
    val byteArr = Array[Byte](1, 2, 3)
    reportManager.putDefinition("test_def", byteArr, new DefinitionEntity(new Date(), "test_def", "default", null, ""))
  }

  @AfterTest
  def shutdown() {
    coordinator !? ShutDownRequest()
  }

  @Test
  def canGetJobsMap() {
    val jobsResource = new JobsResource
    val respBox = jobsResource.get("/jobs")

    Assert.assertTrue(respBox.isDefined)

    val resp = respBox.open_!.asInstanceOf[PlainTextResponse]
    Assert.assertEquals(resp.code, 200)
    Assert.assertEquals(resp.headers, List(("Content-Type", "application/vnd.ksmpartners.ernie+json")))
  }

  @Test
  def canPostNewJob() {
    val jobsResource = new JobsResource
    val respBox = jobsResource.post(Full("""{"defId":"test_def","rptType":"PDF"}""".getBytes))

    Assert.assertTrue(respBox.isDefined)

    val resp = respBox.open_!.asInstanceOf[PlainTextResponse]
    Assert.assertEquals(resp.code, 201)
    Assert.assertEquals(resp.headers, List(("Content-Type", "application/vnd.ksmpartners.ernie+json")))
  }

  @Test
  def cantPostNewJobWithBadSyntax() {
    val jobsResource = new JobsResource
    val respBox = jobsResource.post(Full("""{"THIS_IS":"WRONG"}""".getBytes))
    Assert.assertTrue(respBox.open_!.isInstanceOf[BadResponse])
  }

  @Test
  def canGetJobStatus() {
    val jobStatusResource = new JobStatusResource
    val respBox = jobStatusResource.get("1234")

    Assert.assertTrue(respBox.isDefined)

    val resp = respBox.open_!.asInstanceOf[PlainTextResponse]
    Assert.assertEquals(resp.code, 200)
    Assert.assertEquals(resp.headers, List(("Content-Type", "application/vnd.ksmpartners.ernie+json")))
    Assert.assertEquals(resp.text, """{"jobStatus":"NO_SUCH_JOB"}""")
  }

  @Test
  def canGetJobResults() {
    val jobResultsResource = new JobResultsResource
    val jobsResource = new JobsResource
    val respBox = jobsResource.post(Full("""{"defId":"test_def","rptType":"PDF"}""".getBytes))

    Assert.assertTrue(respBox.isDefined)

    val resp = respBox.open_!.asInstanceOf[PlainTextResponse]
    Assert.assertEquals(resp.code, 201)
    Assert.assertEquals(resp.headers, List(("Content-Type", "application/vnd.ksmpartners.ernie+json")))

    val rptResp = deserialize(resp.text, classOf[model.ReportResponse])
    val resultRespBox = jobResultsResource.get(rptResp.getJobId.toString)

    val resultResp = resultRespBox.open_!.asInstanceOf[StreamingResponse]
    Assert.assertEquals(resultResp.code, 200)
    Assert.assertEquals(resultResp.headers, List(("Content-Type", "application/pdf"),
      ("Content-Length", "20"),
      ("Content-Disposition", "attachment; filename=\"REPORT_" + rptResp.getJobId + ".pdf\"")))
  }

  @Test
  def missingJobReturnsBadResult() {
    val jobResultsResource = new JobResultsResource
    val resultRespBox = jobResultsResource.get("000")

    Assert.assertTrue(resultRespBox.open_!.isInstanceOf[BadResponse])
  }

}

// Stubs used for testing:

trait TestReportGeneratorFactory extends ReportGeneratorFactory {

  def getReportGenerator(reportManager: ReportManager): ReportGenerator = {
    new TestReportGenerator(reportManager)
  }

}

class TestReportGenerator(reportManager: ReportManager) extends ReportGenerator {

  private var isStarted = false

  def startup() {
    if (isStarted)
      throw new IllegalStateException("ReportGenerator is already started")
    isStarted = true
  }

  def getAvailableRptDefs: List[String] = {
    if (!isStarted)
      throw new IllegalStateException("ReportGenerator is not started")
    List("def_1")
  }

  def runReport(defId: String, rptId: String, rptType: ReportType) {
    if (!isStarted)
      throw new IllegalStateException("ReportGenerator is not started")
    var entity = new mutable.HashMap[String, Any]()
    entity += (ReportManager.RPT_ID -> rptId)
    entity += (ReportManager.SOURCE_DEF_ID -> "def")
    entity += (ReportManager.REPORT_TYPE -> rptType)
    entity += (ReportManager.CREATED_USER -> "default")
    try_(reportManager.putReport(entity)) { os =>
      os.write(rptId.getBytes)
    }
  }

  def runReport(defInputStream: InputStream, rptOutputStream: OutputStream, rptType: ReportType) {
    if (!isStarted)
      throw new IllegalStateException("ReportGenerator is not started")
  }

  def shutdown() {
    if (!isStarted)
      throw new IllegalStateException("ReportGenerator is not started")
    isStarted = false
  }
}