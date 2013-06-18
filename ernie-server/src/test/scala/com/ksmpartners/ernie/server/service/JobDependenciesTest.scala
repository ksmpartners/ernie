/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 *
 *
 * package com.ksmpartners.ernie.server.service
 *
 * import com.ksmpartners.ernie.engine._
 * import com.ksmpartners.ernie.engine.report._
 * import com.ksmpartners.ernie.{ engine, model }
 * import com.ksmpartners.ernie.util.MapperUtility._
 * import com.ksmpartners.ernie.model._
 * import com.ksmpartners.ernie.util.Utility._
 * import java.io._
 * import org.testng.annotations._
 * import net.liftweb.common.{ Full, Box }
 * import net.liftweb.http._
 * import org.testng.Assert
 * import collection.mutable
 * import org.joda.time.DateTime
 * import com.ksmpartners.ernie.server.{ DispatchRestAPI, JsonTranslator }
 * import org.slf4j.{ LoggerFactory, Logger }
 * import com.ksmpartners.common.annotations.tracematrix.{ TestSpec, TestSpecs }
 * import scala.Array
 *
 * import net.liftweb.http.StreamingResponse
 * import net.liftweb.http.ResponseWithReason
 * import com.ksmpartners.ernie.engine.ShutDownRequest
 * import net.liftweb.common.Full
 * import com.ksmpartners.ernie.engine.PurgeResponse
 * import com.ksmpartners.ernie.engine.PurgeRequest
 * import com.ksmpartners.ernie.util.TestLogger
 * import com.ksmpartners.ernie.api.ErnieAPI
 *
 * class JobDependenciesTest extends TestLogger with JobDependencies with JsonTranslator {
 *
 * val tempInputDir = createTempDirectory
 * val tempOutputDir = createTempDirectory
 * val tempJobDir = createTempDirectory
 * var testDef = ""
 * val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.server.JobDependenciesTest")
 * val timeout = 300 * 1000L
 *
 * protected val ernie = {
 * for (i <- 1 to 4) {
 * var report = new ReportEntity(DateTime.now, if (i % 2 == 0) DateTime.now.minusDays(10) else DateTime.now.plusDays(10), "REPORT_" + i, "test_def", "default", null, ReportType.PDF, null, null)
 * try {
 * val rptEntFile = new File(tempOutputDir, report.getRptId + ".entity")
 * try_(new FileOutputStream(rptEntFile)) { fos =>
 * mapper.writeValue(fos, report)
 * }
 * val ext = report.getReportType match {
 * case ReportType.CSV => ".csv"
 * case ReportType.HTML => ".html"
 * case ReportType.PDF => ".pdf"
 * }
 * val file = new File(tempOutputDir, report.getRptId + ext)
 * try_(new FileOutputStream(file)) { fos =>
 * fos.write("test".getBytes)
 * }
 *
 * val job = new File(tempJobDir, rptToJobId(report.getRptId) + ".entity")
 * val jobEnt: JobEntity = new JobEntity(rptToJobId(report.getRptId), if (i % 2 == 0) JobStatus.COMPLETE else JobStatus.IN_PROGRESS, DateTime.now, report.getRptId, if (i % 2 == 0) null else report)
 * try_(new FileOutputStream(job)) { fos =>
 * mapper.writeValue(fos, jobEnt)
 * }
 * } catch {
 * case e: Exception => log.info("Caught exception while generating test entities: {}", e.getMessage + "\n" + e.getStackTraceString)
 * }
 * }
 * val api = ErnieAPI(tempJobDir.getAbsolutePath, tempInputDir.getAbsolutePath, tempOutputDir.getAbsolutePath, timeout, 7, 14)
 * testDef = api.createDefinition(None, "", "").defEnt.get.getDefId
 * api
 * }
 *
 *
 * @AfterTest
 * def shutdown() {
 * recDel(tempInputDir)
 * recDel(tempOutputDir)
 * recDel(tempJobDir)
 * }
 *
 * @Test
 * def downloadServiceReturn410ForExpiredReports() {
 * val jobResultsResource = new JobResultsResource
 * val resp: Box[LiftResponse] = jobResultsResource.get(2L.toString)
 * Assert.assertTrue(resp.isDefined)
 * val re = resp.open_!
 * Assert.assertEquals(re.getClass, classOf[ResponseWithReason])
 * Assert.assertEquals(resp.open_!.asInstanceOf[ResponseWithReason].reason, "Report expired")
 * Assert.assertEquals(resp.open_!.toResponse.code, 410)
 * }
 *
 * @Test(dependsOnMethods = Array("downloadServiceReturn410ForExpiredReports"))
 * def purgeTest() {
 * val purgeResp = (coordinator !? PurgeRequest()).asInstanceOf[PurgeResponse]
 * Assert.assertTrue(purgeResp.purgedRptIds.contains("REPORT_2"))
 * Assert.assertTrue(purgeResp.purgedRptIds.contains("REPORT_4"))
 * Assert.assertFalse(purgeResp.purgedRptIds.contains("REPORT_1"))
 * Assert.assertFalse(purgeResp.purgedRptIds.contains("REPORT_3"))
 * }
 *
 * @Test
 * def canGetJobsMap() {
 * val jobsResource = new JobsResource
 * val respBox = jobsResource.getMap("/jobs")
 *
 * Assert.assertTrue(respBox.isDefined)
 *
 * val resp = respBox.open_!.asInstanceOf[PlainTextResponse]
 * Assert.assertEquals(resp.code, 200)
 * Assert.assertEquals(resp.headers, List(("Content-Type", "application/vnd.ksmpartners.ernie+json")))
 * }
 *
 * @Test
 * def canPostNewJob() {
 * val jobsResource = new JobsResource
 * val respBox = jobsResource.post(Full(("""{"defId":"""" + testDef + """","rptType":"PDF"}""").getBytes), "testUser")
 *
 * Assert.assertTrue(respBox.isDefined)
 *
 * val resp = respBox.open_!.asInstanceOf[PlainTextResponse]
 * Assert.assertEquals(resp.code, 201)
 * Assert.assertTrue(resp.headers.contains(("Content-Type", "application/vnd.ksmpartners.ernie+json")))
 * }
 *
 * @Test
 * def cantPostNewJobWithBadSyntax() {
 * val jobsResource = new JobsResource
 * val respBox = jobsResource.post(Full("""{"THIS_IS":"WRONG"}""".getBytes), "testUser")
 * Assert.assertTrue(respBox.open_!.isInstanceOf[ResponseWithReason])
 * }
 *
 * @Test
 * def canGetJobStatus() {
 * val jobStatusResource = new JobStatusResource
 * val respBox = jobStatusResource.get("1234")
 *
 * Assert.assertTrue(respBox.isDefined)
 *
 * val resp = respBox.open_!.asInstanceOf[PlainTextResponse]
 * Assert.assertEquals(resp.code, 200)
 * Assert.assertEquals(resp.headers, List(("Content-Type", "application/vnd.ksmpartners.ernie+json")))
 * Assert.assertEquals(resp.text, """{"jobStatus":"NO_SUCH_JOB"}""")
 * }
 *
 * @TestSpecs(Array(new TestSpec(key = "ERNIE-160")))
 * @Test
 * def jobsInProgressOnShutDownAreRestarted() {
 * var jobRunning = true
 * var end = System.currentTimeMillis + (1000 * 30)
 * var job1Complete = false
 * var job2Complete = false
 * while (jobRunning && (System.currentTimeMillis < end)) {
 * val respOpt = (coordinator !? (timeout, engine.StatusRequest(1L))).asInstanceOf[Option[engine.StatusResponse]]
 * respOpt.map(r =>
 * if (r.jobStatus == JobStatus.COMPLETE) {
 * job1Complete = true
 * jobRunning = false
 * })
 * }
 * jobRunning = true
 * end = System.currentTimeMillis + (1000 * 30)
 * while (jobRunning && (System.currentTimeMillis < end)) {
 * val respOpt = (coordinator !? (timeout, engine.StatusRequest(3L))).asInstanceOf[Option[engine.StatusResponse]]
 * respOpt.map(r =>
 * if (r.jobStatus == JobStatus.COMPLETE) {
 * job2Complete = true
 * jobRunning = false
 * })
 * }
 * Assert.assertTrue(job1Complete)
 * Assert.assertTrue(job2Complete)
 * }
 *
 * @Test
 * def canGetJobResults() {
 * val jobResultsResource = new JobResultsResource
 * val jobsResource = new JobsResource
 * val req = """{"defId":"""" + testDef + """","rptType":"PDF"}"""
 *
 * val respBox = jobsResource.post(Full(req.getBytes), "testUser")
 *
 * Assert.assertTrue(respBox.isDefined)
 *
 * val resp = respBox.open_!.asInstanceOf[PlainTextResponse]
 * Assert.assertEquals(resp.code, 201)
 * Assert.assertTrue(resp.headers.contains(("Content-Type", "application/vnd.ksmpartners.ernie+json")))
 * val rptResp = deserialize(resp.text, classOf[model.ReportResponse])
 *
 * val jobStatusResource = new JobStatusResource
 *
 * var statusRespBox = jobStatusResource.get(rptResp.getJobId.toString).open_!.asInstanceOf[PlainTextResponse]
 * var statusResp = deserialize(statusRespBox.text, classOf[model.StatusResponse])
 * val end = System.currentTimeMillis + (1000 * 5)
 * while ((statusResp.getJobStatus != JobStatus.COMPLETE) && (System.currentTimeMillis() < end)) {
 * statusRespBox = jobStatusResource.get(rptResp.getJobId.toString).open_!.asInstanceOf[PlainTextResponse]
 * statusResp = deserialize(statusRespBox.text, classOf[model.StatusResponse])
 * }
 * val resultRespBox = jobResultsResource.get(rptResp.getJobId.toString)
 * val resultResp = resultRespBox.open_!.asInstanceOf[StreamingResponse]
 * Assert.assertEquals(resultResp.code, 200)
 * Assert.assertEquals(resultResp.headers, List(("Content-Type", "application/pdf"),
 * ("Content-Length", "20"),
 * ("Content-Disposition", "attachment; filename=\"REPORT_" + rptResp.getJobId + ".pdf\"")))
 * }
 *
 * @Test
 * def missingJobReturnsNotFound() {
 * val jobResultsResource = new JobResultsResource
 * val resultRespBox = jobResultsResource.get("000")
 *
 * Assert.assertTrue(resultRespBox.open_!.isInstanceOf[NotFoundResponse])
 * }
 *
 * private def createTempDirectory(): File = {
 *
 * var temp: File = null
 *
 * temp = File.createTempFile("temp", System.nanoTime.toString)
 *
 * if (!(temp.delete())) {
 * throw new IOException("Could not delete temp file: " + temp.getAbsolutePath)
 * }
 *
 * if (!(temp.mkdir())) {
 * throw new IOException("Could not create temp directory: " + temp.getAbsolutePath)
 * }
 *
 * temp
 * }
 *
 * }
 *
 * // Stubs used for testing:
 *
 * trait TestReportGeneratorFactory extends ReportGeneratorFactory {
 *
 * def getReportGenerator(reportManager: ReportManager): ReportGenerator = {
 * new TestReportGenerator(reportManager)
 * }
 *
 * }
 *
 * class TestReportGenerator(reportManager: ReportManager) extends ReportGenerator {
 *
 * private var isStarted = false
 *
 * def startup() {
 * if (isStarted)
 * throw new IllegalStateException("ReportGenerator is already started")
 * isStarted = true
 * }
 *
 * def getAvailableRptDefs: List[String] = {
 * if (!isStarted)
 * throw new IllegalStateException("ReportGenerator is not started")
 * List("def_1")
 * }
 *
 * def runReport(defId: String, rptId: String, rptType: ReportType, retentionDays: Option[Int], userName: String) = runReport(defId, rptId, rptType, retentionDays, Map.empty[String, String], userName)
 * def runReport(defId: String, rptId: String, rptType: ReportType, retentionDays: Option[Int], reportParameters: scala.collection.Map[String, String], userName: String) {
 * if (!isStarted)
 * throw new IllegalStateException("ReportGenerator is not started")
 * var entity = new mutable.HashMap[String, Any]()
 * entity += (ReportManager.rptId -> rptId)
 * entity += (ReportManager.sourceDefId -> "def")
 * entity += (ReportManager.reportType -> rptType)
 * entity += (ReportManager.createdUser -> userName)
 * try_(reportManager.putReport(entity)) { os =>
 * os.write(rptId.getBytes)
 * }
 * }
 *
 * def runReport(defInputStream: InputStream, rptOutputStream: OutputStream, rptType: ReportType) {
 * if (!isStarted)
 * throw new IllegalStateException("ReportGenerator is not started")
 * }
 *
 * def shutdown() {
 * if (!isStarted)
 * throw new IllegalStateException("ReportGenerator is not started")
 * isStarted = false
 * }
 * }
 */
