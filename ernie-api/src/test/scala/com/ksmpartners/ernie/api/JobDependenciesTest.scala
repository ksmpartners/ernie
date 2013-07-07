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
import scala.Array

import akka.pattern.ask
import scala.concurrent.duration._
import com.ksmpartners.ernie.model.DeleteStatus
import scala.Some
import akka.actor.{ ActorRef, ActorDSL, ActorSystem }

@Test(dependsOnGroups = Array("timeout"))
class JobDependenciesTest extends JobDependencies with RequiresCoordinator with RequiresReportManager { //TestLogger with

  private val tempInputDir = createTempDirectory
  private val tempOutputDir = createTempDirectory
  private val tempJobDir = createTempDirectory

  protected def workerCount: Int = 5

  def timeoutDuration = (5 minutes)

  protected val system: ActorSystem = ActorSystem("job-dependencies-test")

  @BeforeClass
  protected def jobsDir = tempJobDir.getAbsolutePath

  @BeforeClass
  protected def outputDir = tempOutputDir.getAbsolutePath

  @BeforeClass
  protected def defDir = tempInputDir.getAbsolutePath

  @BeforeClass
  private var testDef = ""

  private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.server.JobDependenciesTest")

  @BeforeClass
  protected def timeout: Long = 300 * 1000L

  @BeforeClass(dependsOnGroups = Array("timeout"))
  def startup() {
  }

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

  @BeforeClass
  protected val coordinator: ActorRef = {
    val coord = ActorDSL.actor(ActorSystem("job-dependencies-tests"))(new Coordinator(Some(tempJobDir.getAbsolutePath), reportManager, Some(30 minutes), workerCount) with TestReportGeneratorFactory)
    coord
  }

  @BeforeClass
  private var jobId = -1L

  @AfterTest
  def shutdown() {
    recDel(tempInputDir)
    recDel(tempOutputDir)
    recDel(tempJobDir)
  }

  @Test(groups = Array("jDCleanUp"), dependsOnGroups = Array("main"))
  def deleteJob() {
    val resultRes = new JobResultsResource
    Assert.assertEquals(resultRes.del(jobId), DeleteStatus.SUCCESS)
  }

  @Test(groups = Array("jDCleanUp"), dependsOnGroups = Array("main"))
  def purgeTest() {
    val purgeResp = (new JobCatalogResource).purge //(coordinator !? PurgeRequest()).asInstanceOf[PurgeResponse]
    Assert.assertTrue(purgeResp._2.contains("REPORT_2"))
    Assert.assertTrue(purgeResp._2.contains("REPORT_4"))
    Assert.assertFalse(purgeResp._2.contains("REPORT_1"))
    Assert.assertFalse(purgeResp._2.contains("REPORT_3"))
  }

  @Test(groups = Array("main"))
  def canGetJobsMap() {
    val jobsResource = new JobsResource
    val respBox = jobsResource.getList()

    Assert.assertTrue(!respBox.isEmpty)
  }

  @Test(groups = Array("main"), dependsOnGroups = Array("timeout"))
  def canPostNewJob() {
    val jobsResource = new JobsResource
    val (id, status) = jobsResource.createJob(testDef, model.ReportType.PDF, Some(5), Map.empty[String, String], "testUser")
    Assert.assertTrue(id > 0)
    jobId = id
    Assert.assertEquals(status, model.JobStatus.IN_PROGRESS)
  }

  @Test(dependsOnMethods = Array("canPostNewJob"), groups = Array("main"))
  def canGetJobEntity() {
    val jobEntityResource = new JobEntityResource
    val resp = jobEntityResource.getJobEntity(jobId)
    Assert.assertTrue(resp.isDefined)
  }

  @Test(dependsOnMethods = Array("canPostNewJob"), groups = Array("main"))
  def canGetJobStatus() {
    val jobStatusResource = new JobStatusResource
    val resp = jobStatusResource.get(jobId)
    Assert.assertTrue(resp != null)
  }

  @Test(dependsOnMethods = Array("canGetJobResults"), groups = Array("main"))
  def canGetRptEntity() {
    val jobStatusResource = new JobResultsResource
    val resp = jobStatusResource.getReportEntity(jobId)
    Assert.assertTrue(resp.isDefined)
  }

  @Test(dependsOnMethods = Array("canPostNewJob"), groups = Array("main"))
  def canGetJobCatalog() {
    val jobCatalogResource = new JobCatalogResource
    val resp = jobCatalogResource.getCatalog(None)

    Assert.assertTrue(resp.size > 0)
  }

  @Test(dependsOnMethods = Array("canPostNewJob"), groups = Array("main"))
  def canGetJobResults() {
    val jobResultsResource = new JobResultsResource
    val jobsResource = new JobsResource
    val statusResource = new JobStatusResource
    val end = System.currentTimeMillis() + 5000L
    var statusResp = statusResource.get(jobId)
    while ((statusResp != model.JobStatus.COMPLETE) && (System.currentTimeMillis() < end)) {
      statusResp = statusResource.get(jobId)
    }
    val resultRespBox = jobResultsResource.get(jobId)
    Assert.assertTrue(resultRespBox.isDefined)
  }

  @Test
  def canCompareOutputExceptions() {
    val one = ReportOutputException(Some(model.JobStatus.FAILED_INVALID_PARAMETER_VALUES), "")
    val two: Exception = ReportOutputException(Some(model.JobStatus.FAILED), "")
    Assert.assertFalse(one.compare(two))
    val three: Exception = ReportOutputException(None, "")
    Assert.assertTrue(one.compare(three))
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

  protected var running = false

  def startup() = if (!running) {
    running = true
  }

  def getAvailableRptDefs: List[String] = {
    if (!running)
      throw new IllegalStateException("ReportGenerator is not started")
    List("def_1")
  }

  def runReport(defId: String, rptId: String, rptType: model.ReportType, retentionDays: Option[Int], userName: String) = runReport(defId, rptId, rptType, retentionDays, Map.empty[String, String], userName)
  def runReport(defId: String, rptId: String, rptType: model.ReportType, retentionDays: Option[Int], reportParameters: scala.collection.Map[String, String], userName: String) {
    if (!running)
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
    if (!running)
      throw new IllegalStateException("ReportGenerator is not started")
  }

  def shutdown() {
    if (running)
      running = false
  }
}

