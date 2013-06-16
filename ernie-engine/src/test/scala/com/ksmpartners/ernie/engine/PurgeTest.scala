/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.engine

import org.testng.annotations.{ Test, AfterClass, BeforeClass }
import java.io._
import com.ksmpartners.ernie.engine.report._
import java.net.URL
import org.testng.Assert
import com.ksmpartners.ernie.model._
import org.joda.time.DateTime
import com.ksmpartners.common.annotations.tracematrix.{ TestSpecs, TestSpec }
import org.slf4j.{ LoggerFactory, Logger }
import scala.collection.{ JavaConversions, mutable }
import com.ksmpartners.ernie.util.Utility._
import com.ksmpartners.ernie.util.MapperUtility._
import com.ksmpartners.ernie.util.TestLogger
import com.ksmpartners.ernie.engine._

class PurgeTest extends TestLogger {

  private var reportManager: FileReportManager = null
  private var coordinator: Coordinator = null
  private val timeout = 1000L
  private var testDef = ""
  private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.engine.PurgeTest")

  @BeforeClass
  def setup() {
    val rptDefDir = createTempDirectory
    val outputDir = createTempDirectory
    val jobDir = createTempDirectory

    val url: URL = Thread.currentThread().getContextClassLoader.getResource("test_def.rptdesign")
    val file = new File(url.getPath)
    var fis: FileInputStream = null
    try {
      fis = new FileInputStream(file)
      val byteArr = new Array[Byte](file.length().asInstanceOf[Int])
      fis.read(byteArr)
      var fos = new FileOutputStream(new File(rptDefDir, "test_def.rptdesign"))
      fos.write(byteArr)
      fos.close
      fos = new FileOutputStream(new File(rptDefDir, "test_def.entity"))
      val defEnt = new DefinitionEntity(DateTime.now(), "test_def", "default", null, "", JavaConversions.asJavaList(List(ReportType.CSV)), null)
      mapper.writeValue(fos, defEnt)
      fos.close
    } catch {
      case e: Exception => {
        log.info("Caught exception while generating test definition: {}", e.getMessage + "\n" + e.getStackTraceString)
        throw e
      }
    }
    finally {
      try { fis.close() } catch { case e => }
    }

    for (i <- 1 to 4) {
      var report = new ReportEntity(DateTime.now, if (i % 2 == 0) DateTime.now.minusDays(10) else DateTime.now.plusDays(10), "REPORT_" + i, "test_def", "default", null, if (i == 3) ReportType.CSV else ReportType.PDF, null, null)
      try {
        val rptEntFile = new File(outputDir, report.getRptId + ".entity")
        try_(new FileOutputStream(rptEntFile)) { fos =>
          mapper.writeValue(fos, report)
        }
        val ext = report.getReportType match {
          case ReportType.CSV => ".csv"
          case ReportType.HTML => ".html"
          case ReportType.PDF => ".pdf"
        }
        val file = new File(outputDir, report.getRptId + ext)
        try_(new FileOutputStream(file)) { fos =>
          fos.write("test".getBytes)
        }
        val job = new File(jobDir, rptToJobId(report.getRptId) + ".entity")
        try_(new FileOutputStream(job)) { fos =>
          mapper.writeValue(fos,
            if (i % 2 == 0)
              new JobEntity(rptToJobId(report.getRptId), JobStatus.COMPLETE, DateTime.now, report.getRptId, null)
            else
              new JobEntity(rptToJobId(report.getRptId), JobStatus.IN_PROGRESS, DateTime.now, null, report))
        }

      } catch { case e: Exception => {} }
    }
    try {
      reportManager = new FileReportManager(rptDefDir.getAbsolutePath, outputDir.getAbsolutePath)
      coordinator = new Coordinator(jobDir.getAbsolutePath, reportManager) with TestReportGeneratorFactory
      coordinator.start()
    } finally {

    }

  }

  @AfterClass
  def shutdown() {
    val sResp = (coordinator !? ShutDownRequest()).asInstanceOf[ShutDownResponse]
  }

  @Test
  def hasReports() {
    Assert.assertTrue(reportManager.hasReport("REPORT_1"))
    Assert.assertTrue(reportManager.hasReport("REPORT_2"))
    Assert.assertTrue(reportManager.hasReport("REPORT_3"))
    Assert.assertTrue(reportManager.hasReport("REPORT_4"))
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-68")))
  @Test(dependsOnMethods = Array("hasReports"))
  def canPurgeExpiredJobs() {
    import com.ksmpartners.ernie.engine.StatusResponse
    val statusList = List((coordinator !? (timeout, StatusRequest(2L))).asInstanceOf[Option[StatusResponse]], (coordinator !? (timeout, StatusRequest(4L))).asInstanceOf[Option[StatusResponse]])
    statusList.foreach(f => {
      Assert.assertTrue(f.isDefined)
      Assert.assertEquals(f.get.jobStatus, JobStatus.EXPIRED)
    })
    val purgeResp = (coordinator !? PurgeRequest()).asInstanceOf[PurgeResponse]
    Assert.assertTrue(purgeResp.purgedRptIds.contains("REPORT_2"))
    Assert.assertTrue(purgeResp.purgedRptIds.contains("REPORT_4"))
  }

  @Test(dependsOnMethods = Array("canPurgeExpiredJobs"))
  def pendingJobsRestart() {
    import com.ksmpartners.ernie.engine.StatusResponse
    var statusRespOpt: List[StatusResponse] = Nil
    val end = DateTime.now.plus(timeout * 10L)
    var result = false
    do {
      statusRespOpt = Nil
      (coordinator !? (timeout, StatusRequest(1L))).asInstanceOf[Option[StatusResponse]].map(f => statusRespOpt ::= f)
      Assert.assertEquals(statusRespOpt.length, 1)
      (coordinator !? (timeout, StatusRequest(3L))).asInstanceOf[Option[StatusResponse]].map(f => statusRespOpt ::= f)
      Assert.assertEquals(statusRespOpt.length, 2)
      result = (statusRespOpt(1).jobStatus == JobStatus.COMPLETE) && (statusRespOpt(0).jobStatus == JobStatus.FAILED_UNSUPPORTED_FORMAT)
    } while (!result && (DateTime.now.isBefore(end)))
    Assert.assertTrue(result)
  }

  @Test(dependsOnMethods = Array("pendingJobsRestart"))
  def canDeleteDef() {
    import com.ksmpartners.ernie.engine.DeleteDefinitionResponse
    val respOpt = (coordinator !? (timeout, DeleteDefinitionRequest("test_def"))).asInstanceOf[Option[DeleteDefinitionResponse]]
    Assert.assertTrue(respOpt.isDefined)
    Assert.assertEquals(respOpt.get.deleteStatus, DeleteStatus.SUCCESS)
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
