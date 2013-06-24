/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 *
 */
package com.ksmpartners.ernie.server.service

import com.ksmpartners.ernie.engine._
import com.ksmpartners.ernie.engine.report._
import com.ksmpartners.ernie.{ engine, model }
import com.ksmpartners.ernie.util.MapperUtility._
import com.ksmpartners.ernie.model._
import com.ksmpartners.ernie.util.Utility._
import java.io._
import org.testng.annotations._
import net.liftweb.common.{ Full, Box }
import net.liftweb.http._
import org.testng.Assert
import collection.mutable
import org.joda.time.DateTime
import com.ksmpartners.ernie.server.{ DispatchRestAPI, JsonTranslator, service }
import service.ServiceRegistry._
import org.slf4j.{ LoggerFactory, Logger }
//import com.ksmpartners.common.annotations.tracematrix.{ TestSpec, TestSpecs }
import scala.Array

import net.liftweb.http.StreamingResponse
import net.liftweb.http.ResponseWithReason
import com.ksmpartners.ernie.engine.ShutDownRequest
import net.liftweb.common.Full
import com.ksmpartners.ernie.engine.PurgeResponse
import com.ksmpartners.ernie.engine.PurgeRequest
import com.ksmpartners.ernie.util.TestLogger
import com.ksmpartners.ernie.api.ErnieAPI

class JobDependenciesTest extends TestLogger {

  val tempInputDir = createTempDirectory

  val tempOutputDir = createTempDirectory

  val tempJobDir = createTempDirectory

  var testDef = ""

  val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.server.JobDependenciesTest")

  val timeout = 300 * 1000L

  @Test
  def canGetJobsMap() {
    val respBox = jobsResource.getMap("/jobs")

    Assert.assertTrue(respBox.isDefined)

    val resp = respBox.open_!.asInstanceOf[PlainTextResponse]
    Assert.assertEquals(resp.code, 200)
    Assert.assertEquals(resp.headers, List(("Content-Type", "application/vnd.ksmpartners.ernie+json")))
  }

  @Test
  def canPostNewJob() {
    val respBox = jobsResource.post(Full(("""{"defId":"test_def","rptType":"PDF"}""").getBytes), "testUser")

    Assert.assertTrue(respBox.isDefined)

    val resp = respBox.open_!.asInstanceOf[PlainTextResponse]
    Assert.assertEquals(resp.code, 201)
    Assert.assertTrue(resp.headers.contains(("Content-Type", "application/vnd.ksmpartners.ernie+json")))
  }

  @Test
  def cantPostNewJobWithBadSyntax() {
    val respBox = jobsResource.post(Full("""{"THIS_IS":"WRONG"}""".getBytes), "testUser")
    Assert.assertTrue(respBox.open_!.isInstanceOf[ResponseWithReason])
  }

  @Test
  def canGetJobStatus() {
    val respBox = jobStatusResource.get("1234")

    Assert.assertTrue(respBox.isDefined)

    val resp = respBox.open_!.asInstanceOf[PlainTextResponse]
    Assert.assertEquals(resp.code, 200)
    Assert.assertEquals(resp.headers, List(("Content-Type", "application/vnd.ksmpartners.ernie+json")))
    Assert.assertEquals(resp.text, """{"jobStatus":"NO_SUCH_JOB"}""")
  }

  @Test
  def canGetJobResults() {
    val jobResultsResource = new JobResultsResource
    val jobsResource = new JobsResource
    val req = """{"defId":"test_def","rptType":"PDF"}"""

    val respBox = jobsResource.post(Full(req.getBytes), "testUser")

    Assert.assertTrue(respBox.isDefined)

    val resp = respBox.open_!.asInstanceOf[PlainTextResponse]
    Assert.assertEquals(resp.code, 201)
    Assert.assertTrue(resp.headers.contains(("Content-Type", "application/vnd.ksmpartners.ernie+json")))
    val rptResp = DispatchRestAPI.deserialize(resp.text, classOf[model.ReportResponse])

    val jobStatusResource = new JobStatusResource

    var statusRespBox = jobStatusResource.get(rptResp.getJobId.toString).open_!.asInstanceOf[PlainTextResponse]
    var statusResp = DispatchRestAPI.deserialize(statusRespBox.text, classOf[model.StatusResponse])
    val end = System.currentTimeMillis + (1000 * 5)
    while ((statusResp.getJobStatus != JobStatus.COMPLETE) && (System.currentTimeMillis() < end)) {
      statusRespBox = jobStatusResource.get(rptResp.getJobId.toString).open_!.asInstanceOf[PlainTextResponse]
      statusResp = DispatchRestAPI.deserialize(statusRespBox.text, classOf[model.StatusResponse])
    }
    val resultRespBox = jobResultsResource.get(rptResp.getJobId.toString)
    val resultResp = resultRespBox.open_!.asInstanceOf[StreamingResponse]
    Assert.assertEquals(resultResp.code, 200)
    Assert.assertTrue(resultResp.headers.contains(("Content-Type", "application/pdf")))
    Assert.assertTrue(resultResp.headers.contains(("Content-Disposition", "attachment; filename=\"REPORT_" + rptResp.getJobId + ".pdf\"")))
  }

  @Test
  def missingJobReturnsNotFound() {
    val jobResultsResource = new JobResultsResource
    val resultRespBox = jobResultsResource.get("7777")

    Assert.assertEquals(resultRespBox.open_!.toResponse.code, 404)
  }

  @Test(enabled = false)
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

