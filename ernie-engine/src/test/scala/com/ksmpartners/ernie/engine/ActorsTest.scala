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
import com.ksmpartners.ernie.model._
import org.joda.time.DateTime
import com.ksmpartners.common.annotations.tracematrix.{ TestSpecs, TestSpec }
import org.slf4j.{ LoggerFactory, Logger }
import scala.collection.{ JavaConversions, mutable }
import com.ksmpartners.ernie.util.Utility._
import com.ksmpartners.ernie.util.TestLogger
import scala.Some
import org.eclipse.birt.report.engine.api.UnsupportedFormatException
import com.ksmpartners.ernie.engine.JobsCatalogResponse

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
      reportManager.putDefinition("test_def", byteArr, new DefinitionEntity(DateTime.now(), "test_def", "default", null, "", JavaConversions.asJavaList(List(ReportType.CSV)), null))
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
    import com.ksmpartners.ernie.engine._
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
    import com.ksmpartners.ernie.engine._

    val statusRespOpt = (coordinator !? (timeout, StatusRequest(0))).asInstanceOf[Option[StatusResponse]]
    Assert.assertTrue(statusRespOpt isDefined)
    val statusResp = statusRespOpt.get
    Assert.assertEquals(statusResp.jobStatus, JobStatus.NO_SUCH_JOB)
  }

  @Test
  def unsupportedOutputFormatJobHasCorrectStatus() {
    import com.ksmpartners.ernie.engine._

    val respOpt = (coordinator !? (timeout, ReportRequest("test_def", ReportType.CSV, None, Map.empty[String, String], "testUser"))).asInstanceOf[Option[ReportResponse]]
    Assert.assertTrue(respOpt.isDefined)
    val resp = respOpt.get
    val statusRespOpt = (coordinator !? (timeout, StatusRequest(resp.jobId))).asInstanceOf[Option[StatusResponse]]
    Assert.assertTrue(statusRespOpt isDefined)
    Assert.assertEquals(statusRespOpt.get.jobStatus, JobStatus.FAILED_UNSUPPORTED_FORMAT)
  }

  @Test(dependsOnMethods = Array("reportEntitiesIncludeAllRequiredMetadata"))
  def canDeleteJobOutput() {
    import com.ksmpartners.ernie.engine._

    var respOpt = (coordinator !? (timeout, DeleteRequest(jobId))).asInstanceOf[Option[DeleteResponse]]
    Assert.assertTrue(respOpt.isDefined)
    Assert.assertEquals(respOpt.get.deleteStatus, DeleteStatus.SUCCESS)
  }

  @Test
  def canRequestJobCatalogs() {
    import com.ksmpartners.ernie.engine._

    var respOpt = (coordinator !? (timeout, JobsCatalogRequest(Some(JobCatalog.COMPLETE)))).asInstanceOf[Option[JobsCatalogResponse]]
    Assert.assertTrue(respOpt.isDefined)
    Assert.assertTrue(respOpt.get.catalog != null)
    respOpt = (coordinator !? (timeout, JobsCatalogRequest(Some(JobCatalog.DELETED)))).asInstanceOf[Option[JobsCatalogResponse]]
    Assert.assertTrue(respOpt.isDefined)
    Assert.assertTrue(respOpt.get.catalog != null)
    respOpt = (coordinator !? (timeout, JobsCatalogRequest(Some(JobCatalog.EXPIRED)))).asInstanceOf[Option[JobsCatalogResponse]]
    Assert.assertTrue(respOpt.isDefined)
    Assert.assertTrue(respOpt.get.catalog != null)
    respOpt = (coordinator !? (timeout, JobsCatalogRequest(Some(JobCatalog.FAILED)))).asInstanceOf[Option[JobsCatalogResponse]]
    Assert.assertTrue(respOpt.isDefined)
    Assert.assertTrue(respOpt.get.catalog != null)
    respOpt = (coordinator !? (timeout, JobsCatalogRequest(None))).asInstanceOf[Option[JobsCatalogResponse]]
    Assert.assertTrue(respOpt.isDefined)
    Assert.assertTrue(respOpt.get.catalog != null)
    respOpt = (coordinator !? (timeout, JobsCatalogRequest(Some(JobCatalog.IN_PROGRESS)))).asInstanceOf[Option[JobsCatalogResponse]]
    Assert.assertTrue(respOpt.isDefined)
    Assert.assertTrue(respOpt.get.catalog != null)

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

  var jobId = -1L

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
    jobId = rptResp.jobId
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

  @TestSpecs(Array(new TestSpec(key = "ERNIE-164")))
  @Test(dependsOnMethods = Array("canGetResult"))
  def reportEntitiesIncludeAllRequiredMetadata() {
    Assert.assertTrue(jobId > 0L)
    val rptRespOpt = (coordinator !? (timeout, ReportDetailRequest(jobId))).asInstanceOf[Option[ReportDetailResponse]]
    Assert.assertTrue(rptRespOpt.isDefined)
    Assert.assertTrue(rptRespOpt.get.rptEntity.isDefined)
    val rptEnt = rptRespOpt.get.rptEntity.get
    Assert.assertTrue(rptEnt.getCreatedDate != null)
    Assert.assertTrue(rptEnt.getCreatedUser != null)
    Assert.assertTrue(rptEnt.getFinishDate != null)
    Assert.assertTrue(rptEnt.getReportType != null)
    Assert.assertTrue(rptEnt.getRetentionDate != null)
    Assert.assertTrue(rptEnt.getRptId != null)
    Assert.assertTrue(rptEnt.getSourceDefId != null)
    Assert.assertTrue(rptEnt.getStartDate != null)
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
  private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.engine.TestReportGenerator")

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
    if (reportManager.getDefinition(defId).get.getUnsupportedReportTypes.contains(rptType))
      throw new UnsupportedFormatException("Unsupported format", Unit)
    var entity = new mutable.HashMap[String, Any]()
    entity += (ReportManager.rptId -> rptId)
    entity += (ReportManager.sourceDefId -> "test_def")
    entity += (ReportManager.reportType -> rptType)
    entity += (ReportManager.createdUser -> userName)
    entity += (ReportManager.startDate -> DateTime.now)
    try_(reportManager.putReport(entity)) { os =>
      os.write(rptId.getBytes)
    }
    entity += (ReportManager.finishDate -> DateTime.now)
    reportManager.updateReportEntity(entity)
  }

  override def shutdown() {
    if (!isStarted)
      throw new IllegalStateException("ReportGenerator is not started")
    isStarted = false
  }
}
