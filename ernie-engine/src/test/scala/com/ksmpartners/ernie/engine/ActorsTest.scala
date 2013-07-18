/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package com.ksmpartners.ernie.engine

import org.testng.annotations._
import java.io._
import report._
import java.net.URL
import org.testng.Assert
import com.ksmpartners.ernie.model._
import org.joda.time.DateTime
//import com.ksmpartners.common.annotations.tracematrix.{ TestSpecs, TestSpec }
import org.slf4j.{ LoggerFactory, Logger }
import scala.collection.{ JavaConversions, mutable }
import com.ksmpartners.ernie.util.Utility._
import com.ksmpartners.ernie.util.TestLogger
import org.eclipse.birt.report.engine.api.UnsupportedFormatException
import akka.actor.{ ActorSystem, ActorRef, ActorDSL }
import akka.pattern.ask
import akka.util.Timeout
import concurrent.duration._
import DurationConversions._
import scala.concurrent.{ Await, ExecutionContext, Future }
import ExecutionContext.Implicits.global
import scala.Some

class ActorsTest extends TestLogger {
  val system = ActorSystem("actors-test-system")
  private var reportManager: MemoryReportManager = null
  private var coordinator: ActorRef = null

  private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.engine.ActorsTest")
  implicit val timeout: Timeout = Timeout(365 days)

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
      coordinator = ActorDSL.actor(system)(new Coordinator(Some(createTempDirectory.getAbsolutePath), reportManager, None, 5) with TestReportGeneratorFactory)
    } finally {
      try { fis.close() } catch { case e => }
    }
  }

  @AfterClass
  def shutdown() {
    coordinator ! (ShutDownRequest())
  }

  @Test
  def canRequestReportAndRetrieveStatus() {
    import com.ksmpartners.ernie.engine._
    val jobId = Await.result((coordinator ? (ReportRequest("test_def", ReportType.PDF, None, Map.empty[String, String], "testUser"))).mapTo[ReportResponse], timeout.duration).jobId
    Assert.assertNotSame(Await.result((coordinator ? (StatusRequest(jobId))).mapTo[StatusResponse], timeout.duration).jobStatus, JobStatus.NO_SUCH_JOB)
  }

  @Test
  def statusForMissingJobIsNoSuchJob() {
    import com.ksmpartners.ernie.engine._
    Assert.assertEquals(Await.result((coordinator ? (StatusRequest(0))).mapTo[StatusResponse], timeout.duration).jobStatus, JobStatus.NO_SUCH_JOB)
  }

  @Test
  def unsupportedOutputFormatJobHasCorrectStatus() {
    import com.ksmpartners.ernie.engine._
    val jobId = Await.result((coordinator ? (ReportRequest("test_def", ReportType.CSV, None, Map.empty[String, String], "testUser"))).mapTo[ReportResponse], timeout.duration).jobId
    Assert.assertEquals(Await.result((coordinator ? (StatusRequest(jobId))).mapTo[StatusResponse], timeout.duration).jobStatus, JobStatus.FAILED_UNSUPPORTED_FORMAT)
  }

  @Test(dependsOnMethods = Array("reportEntitiesIncludeAllRequiredMetadata"))
  def canDeleteJobOutput() {
    import com.ksmpartners.ernie.engine._
    Assert.assertEquals(Await.result((coordinator ? (DeleteRequest(jobId))).mapTo[DeleteResponse], timeout.duration).deleteStatus, DeleteStatus.SUCCESS)
  }

  @Test
  def canRequestJobCatalogs() {
    import com.ksmpartners.ernie.engine._

    val catalogs = List((coordinator ? (JobsCatalogRequest(Some(JobCatalog.COMPLETE)))).mapTo[JobsCatalogResponse],
      (coordinator ? (JobsCatalogRequest(Some(JobCatalog.DELETED)))).mapTo[JobsCatalogResponse],
      (coordinator ? (JobsCatalogRequest(Some(JobCatalog.EXPIRED)))).mapTo[JobsCatalogResponse],
      (coordinator ? (JobsCatalogRequest(Some(JobCatalog.FAILED)))).mapTo[JobsCatalogResponse],
      (coordinator ? (JobsCatalogRequest(None))).mapTo[JobsCatalogResponse],
      (coordinator ? (JobsCatalogRequest(Some(JobCatalog.IN_PROGRESS)))).mapTo[JobsCatalogResponse])
    Await.result(Future.sequence(catalogs), timeout.duration).foreach { c =>
      Assert.assertNotSame(c.catalog, null)
    }
  }

  @Test
  def canSpawnWorker() {
    coordinator ! NewWorkerRequest()
  }

  @Test
  def canRequestJobMap() {
    import com.ksmpartners.ernie.engine._
    val jobId = Await.result((coordinator ? (ReportRequest("test_def", ReportType.PDF, None, Map.empty[String, String], "testUser"))).mapTo[ReportResponse], timeout.duration).jobId
    Assert.assertTrue(Await.result((coordinator ? (JobsListRequest())).mapTo[JobsListResponse], timeout.duration).jobsList.contains(jobId.toString))
  }

  var jobId = -1L

  @Test
  def canGetResult() {
    import com.ksmpartners.ernie.engine._
    val rptResp: ReportResponse = Await.result((coordinator ? (ReportRequest("test_def", ReportType.PDF, None, Map.empty[String, String], "testUser"))).mapTo[ReportResponse], timeout.duration)
    var status = rptResp.jobStatus
    do {
      status = Await.result((coordinator ? (StatusRequest(rptResp.jobId))).mapTo[StatusResponse], timeout.duration).jobStatus
    } while (status == JobStatus.IN_PROGRESS)
    val r = Await.result((coordinator ? (ResultRequest(rptResp.jobId))).mapTo[ResultResponse], timeout.duration)
    Assert.assertTrue(r.rptId isDefined)
    Assert.assertTrue(reportManager.getReport(r.rptId.get) isDefined)
    jobId = rptResp.jobId
  }

  ////@TestSpecs(Array(new TestSpec(key = "ERNIE-118")))
  @Test
  def canAddDefWithEmptyOrWhitespaceDescription() {
    val defEntEmptyDesc = new DefinitionEntity(DateTime.now, null, "default", null, "", null, null)
    Assert.assertEquals(reportManager.putDefinition(defEntEmptyDesc)._1.getCreatedUser, "default")
    val defEntWhiteSpaceDesc = new DefinitionEntity(DateTime.now, null, "default", null, "                  ", null, null)
    Assert.assertTrue((reportManager.putDefinition(defEntEmptyDesc)._1.getDefDescription == null) || (reportManager.putDefinition(defEntEmptyDesc)._1.getDefDescription == ""))
    Assert.assertEquals(reportManager.putDefinition(defEntEmptyDesc)._1.getCreatedUser, "default")

  }

  ////@TestSpecs(Array(new TestSpec(key = "ERNIE-119")))
  @Test
  def defDescriptionWhitespaceIsTrimmed() {
    val defEntEmptyDesc = new DefinitionEntity(DateTime.now, null, "default", null, "    test    ", null, null)
    Assert.assertEquals(reportManager.putDefinition(defEntEmptyDesc)._1.getDefDescription, "test")
  }

  ////@TestSpecs(Array(new TestSpec(key = "ERNIE-117")))
  @Test
  def nonUniqueDefDescriptionsAreAllowed() {
    val defEntEmptyDesc = new DefinitionEntity(DateTime.now, null, "default", null, "", null, null)
    Assert.assertEquals(reportManager.putDefinition(defEntEmptyDesc)._1.getCreatedUser, "default")
    Assert.assertEquals(reportManager.putDefinition(defEntEmptyDesc)._1.getCreatedUser, "default")
  }

  ////@TestSpecs(Array(new TestSpec(key = "ERNIE-164")))
  @Test(dependsOnMethods = Array("canGetResult"))
  def reportEntitiesIncludeAllRequiredMetadata() {
    Assert.assertTrue(jobId > 0L)
    val rptRsp = Await.result((coordinator ? (ReportDetailRequest(jobId))).mapTo[ReportDetailResponse], timeout.duration)

    Assert.assertTrue(rptRsp.rptEntity.isDefined)
    val rptEnt = rptRsp.rptEntity.get
    Assert.assertTrue(rptEnt.getCreatedDate != null)
    Assert.assertTrue(rptEnt.getCreatedUser != null)
    Assert.assertTrue(rptEnt.getFinishDate != null)
    Assert.assertTrue(rptEnt.getReportType != null)
    Assert.assertTrue(rptEnt.getRetentionDate != null)
    Assert.assertTrue(rptEnt.getRptId != null)
    Assert.assertTrue(rptEnt.getSourceDefId != null)
    Assert.assertTrue(rptEnt.getStartDate != null)

  }

  ////@TestSpecs(Array(new TestSpec(key = "ERNIE-8")))
  @Test
  def jobWithoutRetentionDateUsesDefault() {
    import com.ksmpartners.ernie.engine._
    val rsp = Await.result((coordinator ? (ReportRequest("test_def", ReportType.PDF, None, Map.empty[String, String], "testUser"))).mapTo[ReportResponse], timeout.duration)
    val defaultRetentionDate = DateTime.now().plusDays(reportManager.getDefaultRetentionDays)
    var statusRespOp = rsp.jobStatus
    do {
      statusRespOp = Await.result((coordinator ? (StatusRequest(rsp.jobId))).mapTo[StatusResponse], timeout.duration).jobStatus
    } while (statusRespOp != JobStatus.COMPLETE)
    val r = Await.result((coordinator ? (ResultRequest(rsp.jobId))).mapTo[ResultResponse], timeout.duration)
    Assert.assertTrue(r.rptId.isDefined)
    Assert.assertTrue(reportManager.getReport(r.rptId.get).isDefined)
    Assert.assertTrue(reportManager.getReport(r.rptId.get).get.getRetentionDate.dayOfYear == defaultRetentionDate.dayOfYear)
  }
}

// Stubs used for testing:

trait TestReportGeneratorFactory extends ReportGeneratorFactory {
  private var rptGen: Option[TestReportGenerator] = None
  def getReportGenerator(reportManager: ReportManager): ReportGenerator = rptGen getOrElse {
    rptGen = Some(new TestReportGenerator(reportManager))
    rptGen.get
  }

}

class TestReportGenerator(reportManager: ReportManager) extends ReportGenerator {

  protected var running = false
  private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.engine.TestReportGenerator")

  override def startup = if (!running) running = true

  override def getAvailableRptDefs: List[String] = {
    if (!running)
      throw new IllegalStateException("ReportGenerator is not started")
    List("test_def")
  }

  def runReport(defId: String, rptId: String, rptType: ReportType, retentionDays: Option[Int], userName: String) = runReport(defId, rptId, rptType, retentionDays, Map.empty[String, String], userName)
  def runReport(defId: String, rptId: String, rptType: ReportType, retentionDays: Option[Int], reportParameters: scala.collection.Map[String, String], userName: String) {
    if (!running)
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
    if (running)
      running = false
  }
}
