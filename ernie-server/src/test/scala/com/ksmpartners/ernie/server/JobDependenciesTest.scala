/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.server

import com.ksmpartners.ernie.engine.Coordinator
import com.ksmpartners.ernie.engine.report.{ MemoryReportManager, ReportGenerator, ReportManager, ReportGeneratorFactory }
import com.ksmpartners.ernie.model.ReportType
import java.io.{ Closeable, OutputStream, InputStream }
import org.testng.annotations.Test
import net.liftweb.common.Full
import net.liftweb.http.{ PlainTextResponse, BadResponse }
import org.testng.Assert

class JobDependenciesTest extends JobDependencies {

  val coordinator: Coordinator = {
    val coord = new Coordinator(reportManager) with TestReportGeneratorFactory
    coord.start()
    coord
  }

  val reportManager = new MemoryReportManager

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
    val respBox = jobsResource.post(Full("""{"defId":"test_1","rptType":"PDF"}""".getBytes))

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
    try_(reportManager.putReport(rptId, rptType)) { os =>
      os.write(rptId.getBytes())
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
