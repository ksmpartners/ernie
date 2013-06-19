/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 *
 */

package com.ksmpartners.ernie.api.service

import com.ksmpartners.ernie.engine._
import com.ksmpartners.ernie.engine.report._
import com.ksmpartners.ernie.{ engine, model }
import com.ksmpartners.ernie.util.MapperUtility._
import com.ksmpartners.ernie.util.Utility._
import java.io._
import com.ksmpartners.ernie.api._
import org.testng.annotations._
import org.testng.Assert
import collection.mutable
import org.joda.time.DateTime
import org.slf4j.{ LoggerFactory, Logger }
import com.ksmpartners.common.annotations.tracematrix.{ TestSpec, TestSpecs }
import scala.Array

import com.ksmpartners.ernie.engine.PurgeResponse
import com.ksmpartners.ernie.engine.PurgeRequest
import com.ksmpartners.ernie.util.TestLogger
import com.ksmpartners.ernie.api.ErnieAPI

class JobDependenciesTest extends TestLogger with JobDependencies with RequiresCoordinator with RequiresReportManager {

  val tempInputDir = createTempDirectory
  val tempOutputDir = createTempDirectory
  val tempJobDir = createTempDirectory

  def jobsDir = tempJobDir.getAbsolutePath
  def outputDir = tempOutputDir.getAbsolutePath
  def defDir = tempInputDir.getAbsolutePath

  var testDef = ""
  val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.server.JobDependenciesTest")
  val timeout = 300 * 1000L

  protected val reportManager = {
    for (i <- 1 to 4) {
      var report = new model.ReportEntity(DateTime.now, if (i % 2 == 0) DateTime.now.minusDays(10) else DateTime.now.plusDays(10), "REPORT_" + i, "test_def", "default", null, model.ReportType.PDF, null, null)
      try {
        val rptEntFile = new File(tempOutputDir, report.getRptId + ".entity")
        try_(new FileOutputStream(rptEntFile)) { fos =>
          mapper.writeValue(fos, report)
        }
        val ext = report.getReportType match {
          case model.ReportType.CSV => ".csv"
          case model.ReportType.HTML => ".html"
          case model.ReportType.PDF => ".pdf"
        }
        val file = new File(tempOutputDir, report.getRptId + ext)
        try_(new FileOutputStream(file)) { fos =>
          fos.write("test".getBytes)
        }

        val job = new File(tempJobDir, rptToJobId(report.getRptId) + ".entity")
        val jobEnt: model.JobEntity = new model.JobEntity(rptToJobId(report.getRptId), if (i % 2 == 0) model.JobStatus.COMPLETE else model.JobStatus.IN_PROGRESS, DateTime.now, report.getRptId, if (i % 2 == 0) null else report)
        try_(new FileOutputStream(job)) { fos =>
          mapper.writeValue(fos, jobEnt)
        }
      } catch {
        case e: Exception => log.info("Caught exception while generating test entities: {}", e.getMessage + "\n" + e.getStackTraceString)
      }
    }
    //val api = ErnieAPI(tempJobDir.getAbsolutePath, tempInputDir.getAbsolutePath, tempOutputDir.getAbsolutePath, timeout, 7, 14)
    val rm = new FileReportManager(tempInputDir.getAbsolutePath, tempOutputDir.getAbsolutePath)
    testDef = rm.putDefinition(new model.DefinitionEntity(DateTime.now(), "test_def", "default", null, "", null, null))._1.getDefId
    rm
  }

  val coordinator: Coordinator = {
    val coord = new Coordinator(tempJobDir.getAbsolutePath, reportManager) with TestReportGeneratorFactory
    coord.setTimeout(timeout)
    coord.start()
    coord
  }

  var jobId = -1L
  @AfterTest
  def shutdown() {
    recDel(tempInputDir)
    recDel(tempOutputDir)
    recDel(tempJobDir)
  }

  @Test(dependsOnMethods = Array("canGetJobResults"))
  def purgeTest() {
    val purgeResp = (new JobCatalogResource).purge //(coordinator !? PurgeRequest()).asInstanceOf[PurgeResponse]
    Assert.assertTrue(purgeResp.purgedIds.contains("REPORT_2"))
    Assert.assertTrue(purgeResp.purgedIds.contains("REPORT_4"))
    Assert.assertFalse(purgeResp.purgedIds.contains("REPORT_1"))
    Assert.assertFalse(purgeResp.purgedIds.contains("REPORT_3"))
  }

  @Test
  def canGetJobsMap() {
    val jobsResource = new JobsResource
    val respBox = jobsResource.getList()

    Assert.assertTrue(!respBox.isEmpty)
  }

  @Test
  def canPostNewJob() {
    val jobsResource = new JobsResource
    val resp: JobStatus = jobsResource.createJob(testDef, model.ReportType.PDF, Some(5), Map.empty[String, String], "testUser")
    jobId = resp.jobId
    Assert.assertTrue(resp.jobId > 0)
    Assert.assertEquals(resp.jobStatus.get, model.JobStatus.IN_PROGRESS)
  }

  @Test(dependsOnMethods = Array("canPostNewJob"))
  def canGetJobStatus() {
    val jobStatusResource = new JobStatusResource
    val resp = jobStatusResource.get(jobId)

    Assert.assertTrue(resp.jobStatus.isDefined)
  }

  @Test(dependsOnMethods = Array("canPostNewJob"))
  def canGetJobResults() {
    val jobResultsResource = new JobResultsResource
    val jobsResource = new JobsResource
    val statusResource = new JobStatusResource
    val end = System.currentTimeMillis() + 5000L
    var statusResp = statusResource.get(jobId).jobStatus
    while ((statusResp.isDefined) && (statusResp.get != model.JobStatus.COMPLETE) && (System.currentTimeMillis() < end)) {
      statusResp = statusResource.get(jobId).jobStatus
    }
    val resultRespBox = jobResultsResource.get(jobId, false, true)
    Assert.assertTrue(resultRespBox.stream.isDefined)
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

  def runReport(defId: String, rptId: String, rptType: model.ReportType, retentionDays: Option[Int], userName: String) = runReport(defId, rptId, rptType, retentionDays, Map.empty[String, String], userName)
  def runReport(defId: String, rptId: String, rptType: model.ReportType, retentionDays: Option[Int], reportParameters: scala.collection.Map[String, String], userName: String) {
    if (!isStarted)
      throw new IllegalStateException("ReportGenerator is not started")
    var entity = new mutable.HashMap[String, Any]()
    entity += (ReportManager.rptId -> rptId)
    entity += (ReportManager.sourceDefId -> "def")
    entity += (ReportManager.reportType -> rptType)
    entity += (ReportManager.createdUser -> userName)
    try_(reportManager.putReport(entity)) { os =>
      os.write(rptId.getBytes)
    }
  }

  def runReport(defInputStream: InputStream, rptOutputStream: OutputStream, rptType: model.ReportType) {
    if (!isStarted)
      throw new IllegalStateException("ReportGenerator is not started")
  }

  def shutdown() {
    if (!isStarted)
      throw new IllegalStateException("ReportGenerator is not started")
    isStarted = false
  }
}

