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

class ActorsTest {

  private var reportManager: MemoryReportManager = null
  private var coordinator: Coordinator = null

  private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.engine.ActorsTest")

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
      coordinator = new Coordinator(reportManager) with TestReportGeneratorFactory
      coordinator.start()
    } finally {
      try { fis.close() } catch { case e => }
    }
  }

  @AfterClass
  def shutdown() {
    val sResp = (coordinator !? ShutDownRequest()).asInstanceOf[ShutDownResponse]
  }

  @Test
  def canRequestReportAndRetrieveStatus() {
    val resp = (coordinator !? ReportRequest("test_def", ReportType.PDF, None, Map.empty[String, String])).asInstanceOf[ReportResponse]
    val statusResp = (coordinator !? StatusRequest(resp.jobId)).asInstanceOf[StatusResponse]
    Assert.assertNotSame(statusResp.jobStatus, JobStatus.NO_SUCH_JOB)
  }

  @Test
  def statusForMissingJobIsNoSuchJob() {
    val statusResp = (coordinator !? StatusRequest(0)).asInstanceOf[StatusResponse]
    Assert.assertEquals(statusResp.jobStatus, JobStatus.NO_SUCH_JOB)
  }

  @Test
  def canRequestJobMap() {
    val resp = (coordinator !? ReportRequest("test_def", ReportType.PDF, None, Map.empty[String, String])).asInstanceOf[ReportResponse]
    val jobMapResp = (coordinator !? JobsListRequest()).asInstanceOf[JobsListResponse]
    Assert.assertTrue(jobMapResp.jobsList.contains(resp.jobId.toString))
  }

  @Test
  def canGetResult() {
    val rptResp = (coordinator !? ReportRequest("test_def", ReportType.PDF, None, Map.empty[String, String])).asInstanceOf[ReportResponse]
    while ((coordinator !? StatusRequest(rptResp.jobId)).asInstanceOf[StatusResponse].jobStatus != JobStatus.COMPLETE) {
      // peg coordinator until job is complete
    }
    val resultResp = (coordinator !? ResultRequest(rptResp.jobId)).asInstanceOf[ResultResponse]
    Assert.assertTrue(resultResp.rptId.isDefined)
    Assert.assertTrue(reportManager.getReport(resultResp.rptId.get).isDefined)
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-8")))
  @Test
  def jobWithoutRetentionDateUsesDefault() {

    val rptResp = (coordinator !? ReportRequest("test_def", ReportType.PDF, None, Map.empty[String, String])).asInstanceOf[ReportResponse]
    val defaultRetentionDate = DateTime.now().plusDays(reportManager.getDefaultRetentionDays)

    while ((coordinator !? StatusRequest(rptResp.jobId)).asInstanceOf[StatusResponse].jobStatus != JobStatus.COMPLETE) {
      // peg coordinator until job is complete
    }
    val resultResp = (coordinator !? ResultRequest(rptResp.jobId)).asInstanceOf[ResultResponse]

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

  def runReport(defId: String, rptId: String, rptType: ReportType, retentionDays: Option[Int]) = runReport(defId, rptId, rptType, retentionDays, Map.empty[String, String])
  def runReport(defId: String, rptId: String, rptType: ReportType, retentionDays: Option[Int], reportParameters: scala.collection.Map[String, String]) {
    if (!isStarted)
      throw new IllegalStateException("ReportGenerator is not started")
    var entity = new mutable.HashMap[String, Any]()
    entity += (ReportManager.rptId -> rptId)
    entity += (ReportManager.sourceDefId -> "test_def")
    entity += (ReportManager.reportType -> rptType)
    entity += (ReportManager.createdUser -> "default")
    try_(reportManager.putReport(entity)) { os =>
      os.write(rptId.getBytes)
    }
  }

  override def runReport(defInputStream: InputStream, rptOutputStream: OutputStream, rptType: ReportType) {
    if (!isStarted)
      throw new IllegalStateException("ReportGenerator is not started")
  }

  override def shutdown() {
    if (!isStarted)
      throw new IllegalStateException("ReportGenerator is not started")
    isStarted = false
  }
}
