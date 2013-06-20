/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.server

import net.liftweb.mockweb.{ MockWeb, WebSpec }
import org.testng.annotations.{ Test, BeforeClass }
import com.ksmpartners.ernie.model._
import org.testng.Assert
import net.liftweb.http.PlainTextResponse
import java.io.File

trait TestSetupUtilities extends WebSpec {

  var testJobID: Long = -1L

  var testDef: String = ""

  @BeforeClass
  def setupDefs() {
    var mockReq = new MockWriteAuthReq("/defs")
    mockReq.method = "POST"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    val defEnt = new DefinitionEntity()
    mockReq.body = DispatchRestAPI.serialize(defEnt).getBytes
    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertEquals(resp.open_!.toResponse.code, 201)
      Assert.assertTrue(resp.open_!.isInstanceOf[PlainTextResponse])
      val defEntRsp: DefinitionEntity = DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[DefinitionEntity])
      testDef = defEntRsp.getDefId
    }
    mockReq = new MockWriteAuthReq("/defs/" + testDef + "/rptdesign")
    mockReq.method = "PUT"

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    mockReq.headers += ("Content-Type" -> List("application/rptdesign+xml"))

    val file = new File(Thread.currentThread.getContextClassLoader.getResource("in/test_def_params.rptdesign").getPath)

    mockReq.body = scala.xml.XML.loadFile(file)

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)

      Assert.assertEquals(resp.open_!.toResponse.code, 201)

      val defEntRsp: DefinitionEntity = DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[DefinitionEntity])

      Assert.assertEquals(defEntRsp.getDefId, testDef)

      val params = defEntRsp.getParams

      Assert.assertTrue(params != null)

      Assert.assertTrue(params.size > 0)

      Assert.assertEquals(params.get(0).getParamName, "MinQuantityInStock")

    }
  }

  @Test(enabled = false)
  def completeJob() {
    val mockReq = new MockReadAuthReq("/jobs/" + testJobID + "/status")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    var jobRunning = true
    val end = System.currentTimeMillis + (1000 * 300)
    while (jobRunning && (System.currentTimeMillis < end)) {
      MockWeb.testReq(mockReq) { req =>
        val resp = DispatchRestAPI(req)()
        resp.map(r =>
          if (r.isInstanceOf[PlainTextResponse])
            if (DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[StatusResponse]).getJobStatus == JobStatus.COMPLETE) {
            jobRunning = false
            Assert.assertTrue(DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[StatusResponse]).getJobStatus == JobStatus.COMPLETE)
          })
      }
    }
  }

  @BeforeClass(dependsOnMethods = Array("setupDefs"))
  def setupJob() {
    val mockReq = new MockWriteAuthReq("/jobs")
    mockReq.method = "POST"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    val mockReportReq = new ReportRequest()
    mockReportReq.setDefId(testDef)
    mockReportReq.setRptType(ReportType.PDF)
    val rptParams: java.util.HashMap[String, String] = new java.util.HashMap[String, String]()
    rptParams.put("MinQuantityInStock", "500")
    mockReportReq.setReportParameters(rptParams)
    mockReq.body = DispatchRestAPI.serialize(mockReportReq).getBytes

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()

      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[PlainTextResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 201)
      val reportResponse: ReportResponse = DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[ReportResponse])
      testJobID = reportResponse.getJobId()
      Assert.assertTrue(testJobID > -1L)
      Assert.assertTrue(resp.open_!.toResponse.headers.contains(("Location", req.hostAndPath + "/jobs/" + testJobID)))
    }
  }

}
