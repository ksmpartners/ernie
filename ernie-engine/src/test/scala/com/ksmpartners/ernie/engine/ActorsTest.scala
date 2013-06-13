/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.engine

import org.testng.annotations.{ AfterClass, BeforeClass, Test }
import java.io._
import report._
import java.net.URL
import org.testng.Assert
import com.ksmpartners.ernie.model.{ ParameterEntity, DefinitionEntity, ReportType, JobStatus }
import org.joda.time.DateTime
import com.ksmpartners.common.annotations.tracematrix.{ TestSpecs, TestSpec }
import org.slf4j.{ LoggerFactory, Logger }
import collection.mutable
import com.ksmpartners.ernie.util.Utility._
import com.ksmpartners.ernie.util.TestLogger

class ActorsTest extends TestLogger {

  private var reportManager: MemoryReportManager = null
  private var coordinator: Coordinator = null

  private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.engine.ActorsTest")
  private val timeout = 1000L

  @BeforeClass
  def setup() {
    reportManager = new MemoryReportManager
    val url: URL = Thread.currentThread().getContextClassLoader.getResource("test_def.rptdesign")
    val file = new File(url.getPath)
    var fis: FileInputStream = null
    try {
      fis = new FileInputStream(file)
      val byteArr = new Array[Byte](file.length().asInstanceOf[Int])
      fis.read(byteArr)
      reportManager.putDefinition("test_def", byteArr, new DefinitionEntity(DateTime.now(), "test_def", "default", null, "", null, null))
      coordinator = new Coordinator(createTempDirectory.getAbsolutePath, reportManager) with TestReportGeneratorFactory
      coordinator.start()
    } finally {
      try { fis.close() } catch { case e => }
    }
  }

  @AfterClass
  def shutdown() {
    val sResp = (coordinator !? (timeout, ShutDownRequest())).asInstanceOf[Option[ShutDownResponse]]
  }

  @Test
  def canRequestReportAndRetrieveStatus() {
    val respOpt = (coordinator !? (timeout, ReportRequest("test_def", ReportType.PDF, None, Map.empty[String, String], "testUser"))).asInstanceOf[Option[ReportResponse]]
    Assert.assertTrue(respOpt.isDefined)
    val resp = respOpt.get
    val statusRespOpt = (coordinator !? (timeout, StatusRequest(resp.jobId))).asInstanceOf[Option[StatusResponse]]
    Assert.assertTrue(statusRespOpt isDefined)
    val statusResp = statusRespOpt.get
    Assert.assertNotSame(statusResp.jobStatus, JobStatus.NO_SUCH_JOB)
  }

  @Test
  def statusForMissingJobIsNoSuchJob() {
    val statusRespOpt = (coordinator !? (timeout, StatusRequest(0))).asInstanceOf[Option[StatusResponse]]
    Assert.assertTrue(statusRespOpt isDefined)
    val statusResp = statusRespOpt.get
    Assert.assertEquals(statusResp.jobStatus, JobStatus.NO_SUCH_JOB)
  }

  @Test
  def canRequestJobMap() {
    val respOpt = (coordinator !? (timeout, ReportRequest("test_def", ReportType.PDF, None, Map.empty[String, String], "testUser"))).asInstanceOf[Option[ReportResponse]]
    Assert.assertTrue(respOpt.isDefined)
    val resp = respOpt.get
    val jobMapRespOpt = (coordinator !? (timeout, JobsListRequest())).asInstanceOf[Option[JobsListResponse]]
    Assert.assertTrue(jobMapRespOpt isDefined)
    val jobMapResp: JobsListResponse = jobMapRespOpt.get
    Assert.assertTrue(jobMapResp.jobsList.contains(resp.jobId.toString))
  }

  @Test
  def canGetResult() {
    val rptRespOpt = (coordinator !? (timeout, ReportRequest("test_def", ReportType.PDF, None, Map.empty[String, String], "testUser"))).asInstanceOf[Option[ReportResponse]]
    Assert.assertTrue(rptRespOpt.isDefined)
    val rptResp = rptRespOpt.get
    var statusRespOpt: Option[StatusResponse] = None
    do {
      statusRespOpt = (coordinator !? (timeout, StatusRequest(rptResp.jobId))).asInstanceOf[Option[StatusResponse]]
      Assert.assertTrue(statusRespOpt.isDefined)
    } while (statusRespOpt.get.jobStatus == JobStatus.IN_PROGRESS)
    val resultRespOpt = (coordinator !? (timeout, ResultRequest(rptResp.jobId))).asInstanceOf[Option[ResultResponse]]
    Assert.assertTrue(resultRespOpt.isDefined)
    val resultResp = resultRespOpt.get
    Assert.assertTrue(resultResp.rptId.isDefined)
    Assert.assertTrue(reportManager.getReport(resultResp.rptId.get).isDefined)
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-118")))
  @Test
  def canAddDefWithEmptyOrWhitespaceDescription() {
    val defEntEmptyDesc = new DefinitionEntity(DateTime.now, null, "default", null, "", null, null)
    Assert.assertEquals(reportManager.putDefinition(defEntEmptyDesc)._1.getCreatedUser, "default")
    val defEntWhiteSpaceDesc = new DefinitionEntity(DateTime.now, null, "default", null, "                  ", null, null)
    Assert.assertTrue((reportManager.putDefinition(defEntEmptyDesc)._1.getDefDescription == null) || (reportManager.putDefinition(defEntEmptyDesc)._1.getDefDescription == ""))
    Assert.assertEquals(reportManager.putDefinition(defEntEmptyDesc)._1.getCreatedUser, "default")

  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-119")))
  @Test
  def defDescriptionWhitespaceIsTrimmed() {
    val defEntEmptyDesc = new DefinitionEntity(DateTime.now, null, "default", null, "    test    ", null, null)
    Assert.assertEquals(reportManager.putDefinition(defEntEmptyDesc)._1.getDefDescription, "test")
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-117")))
  @Test
  def nonUniqueDefDescriptionsAreAllowed() {
    val defEntEmptyDesc = new DefinitionEntity(DateTime.now, null, "default", null, "", null, null)
    Assert.assertEquals(reportManager.putDefinition(defEntEmptyDesc)._1.getCreatedUser, "default")
    Assert.assertEquals(reportManager.putDefinition(defEntEmptyDesc)._1.getCreatedUser, "default")
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-8")))
  @Test
  def jobWithoutRetentionDateUsesDefault() {

    val rptRespOpt = (coordinator !? (timeout, ReportRequest("test_def", ReportType.PDF, None, Map.empty[String, String], "testUser"))).asInstanceOf[Option[ReportResponse]]
    Assert.assertTrue(rptRespOpt isDefined)
    val rptResp = rptRespOpt.get
    val defaultRetentionDate = DateTime.now().plusDays(reportManager.getDefaultRetentionDays)
    var statusRespOpt: Option[StatusResponse] = None
    do {
      statusRespOpt = (coordinator !? (timeout, StatusRequest(rptResp.jobId))).asInstanceOf[Option[StatusResponse]]
      Assert.assertTrue(statusRespOpt isDefined)
    } while (statusRespOpt.get.jobStatus != JobStatus.COMPLETE)
    val resultRespOpt = (coordinator !? (timeout, ResultRequest(rptResp.jobId))).asInstanceOf[Option[ResultResponse]]
    Assert.assertTrue(resultRespOpt isDefined)
    val resultResp: ResultResponse = resultRespOpt.get
    Assert.assertTrue(resultResp.rptId.isDefined)
    Assert.assertTrue(reportManager.getReport(resultResp.rptId.get).isDefined)
    Assert.assertTrue(reportManager.getReport(resultResp.rptId.get).get.getRetentionDate.dayOfYear == defaultRetentionDate.dayOfYear)

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

  override def startup() {
    if (isStarted)
      throw new IllegalStateException("ReportGenerator is already started")
    isStarted = true
  }

  override def getAvailableRptDefs: List[String] = {
    if (!isStarted)
      throw new IllegalStateException("ReportGenerator is not started")
    List("test_def")
  }

  def runReport(defId: String, rptId: String, rptType: ReportType, retentionDays: Option[Int], userName: String) = runReport(defId, rptId, rptType, retentionDays, Map.empty[String, String], userName)
  def runReport(defId: String, rptId: String, rptType: ReportType, retentionDays: Option[Int], reportParameters: scala.collection.Map[String, String], userName: String) {
    if (!isStarted)
      throw new IllegalStateException("ReportGenerator is not started")
    var entity = new mutable.HashMap[String, Any]()
    entity += (ReportManager.rptId -> rptId)
    entity += (ReportManager.sourceDefId -> "test_def")
    entity += (ReportManager.reportType -> rptType)
    entity += (ReportManager.createdUser -> userName)
    try_(reportManager.putReport(entity)) { os =>
      os.write(rptId.getBytes)
    }
  }

  override def shutdown() {
    if (!isStarted)
      throw new IllegalStateException("ReportGenerator is not started")
    isStarted = false
  }
}
