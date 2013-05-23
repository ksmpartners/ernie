/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.engine

import org.testng.annotations.{ Test, AfterClass, BeforeClass }
import java.io._
import report._
import java.net.URL
import org.testng.Assert
import com.ksmpartners.ernie.model.{ ReportEntity, DefinitionEntity, ReportType, JobStatus }
import org.joda.time.DateTime
import com.ksmpartners.common.annotations.tracematrix.{ TestSpecs, TestSpec }
import org.slf4j.{ LoggerFactory, Logger }
import collection.mutable
import com.ksmpartners.ernie.util.Utility._
import com.ksmpartners.ernie.util.MapperUtility._
import com.ksmpartners.ernie.engine.ShutDownRequest
import com.ksmpartners.ernie.engine.ShutDownResponse

class PurgeTest {

  private var reportManager: FileReportManager = null
  private var coordinator: Coordinator = null

  private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.engine.PurgeTest")

  @BeforeClass
  def setup() {
    val rptDefDir = createTempDirectory
    val outputDir = createTempDirectory

    for (i <- 1 to 4) {
      var report = new ReportEntity(DateTime.now, if (i % 2 == 0) DateTime.now.minusDays(10) else DateTime.now.plusDays(10), "REPORT_" + i, "test_def", "default", null, ReportType.PDF)
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
      } catch { case e: Exception => {} }
    }
    try {
      reportManager = new FileReportManager(rptDefDir.getAbsolutePath, outputDir.getAbsolutePath)
      coordinator = new Coordinator(reportManager) with TestReportGeneratorFactory
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
    val purgeResp = (coordinator !? PurgeRequest()).asInstanceOf[PurgeResponse]
    purgeResp.purgedRptIds.foreach(f => log.info(f))
    Assert.assertTrue(purgeResp.purgedRptIds.contains("REPORT_2"))
    Assert.assertTrue(purgeResp.purgedRptIds.contains("REPORT_4"))
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
