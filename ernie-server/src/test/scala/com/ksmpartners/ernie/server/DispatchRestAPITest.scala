/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.server

import org.testng.annotations._
import net.liftweb.mockweb.{ MockWeb, WebSpec }
import bootstrap.liftweb.Boot
import net.liftweb.mocks.MockHttpServletRequest
import com.ksmpartners.ernie.server.PropertyNames._
import com.ksmpartners.ernie.server.filter.SAMLConstants._
import com.ksmpartners.ernie.model._
import org.testng.Assert
import net.liftweb.http.{ NotAcceptableResponse, ForbiddenResponse, PlainTextResponse }
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.Properties
import java.io.{ FileInputStream, File }
import com.ksmpartners.ernie.util.Utility._

class DispatchRestAPITest extends WebSpec(() => (new TestBoot).setUpAndBoot()) {

  val mapper = new ObjectMapper()
  var outputDir: File = null

  @AfterClass
  def shutdown() {
    DispatchRestAPI.shutdown()
    for (file <- outputDir.listFiles()) {
      recDel(file)
    }
  }

  @BeforeClass
  def setup() {
    outputDir = new File(properties.get("output.dir").toString)
  }

  @Test
  def canGetJobs() {
    val mockReq = new MockReadAuthReq("/jobs")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI.apply(req).apply()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[PlainTextResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 200)
      val body = resp.open_!.asInstanceOf[PlainTextResponse].text
      val respObj = mapper.readValue(body, classOf[JobsMapResponse])
      Assert.assertNotNull(respObj.getJobStatusMap)
    }
  }

  @Test
  def cantGetJobsWithoutReadAuth() {
    val mockReq = new MockNoAuthReq("/jobs")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI.apply(req).apply()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[ForbiddenResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 403)
    }
  }

  @Test
  def cantGetJobsWithoutCorrectAcceptHeader() {
    val mockReq = new MockReadAuthReq("/jobs")

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI.apply(req).apply()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[NotAcceptableResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 406)
    }
  }

  @Test
  def canGetDefs() {
    val mockReq = new MockReadAuthReq("/defs")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI.apply(req).apply()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[PlainTextResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 200)
      val body = resp.open_!.asInstanceOf[PlainTextResponse].text
      val respObj = mapper.readValue(body, classOf[ReportDefinitionMapResponse])
      Assert.assertNotNull(respObj.getReportDefMap)
    }
  }

  @Test
  def cantGetDefsWithoutReadAuth() {
    val mockReq = new MockNoAuthReq("/defs")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI.apply(req).apply()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[ForbiddenResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 403)
    }
  }

  @Test
  def cantGetDefsWithoutCorrectAcceptHeader() {
    val mockReq = new MockReadAuthReq("/defs")

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI.apply(req).apply()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[NotAcceptableResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 406)
    }
  }

  class MockReadAuthReq(path: String) extends MockHttpServletRequest(path) {
    override def isUserInRole(role: String) = role match {
      case READ_ROLE => true
      case _ => false
    }
  }

  class MockWriteAuthReq(path: String) extends MockHttpServletRequest(path) {
    override def isUserInRole(role: String) = role match {
      case WRITE_ROLE => true
      case _ => false
    }
  }

  class MockNoAuthReq(path: String) extends MockHttpServletRequest(path) {
    override def isUserInRole(role: String) = false
  }

  protected val properties: Properties = {

    val propsPath = System.getProperty(PROPERTIES_FILE_NAME_PROP)

    if (null == propsPath) {
      throw new RuntimeException("System property " + PROPERTIES_FILE_NAME_PROP + " is undefined")
    }

    val propsFile = new File(propsPath)
    if (!propsFile.exists) {
      throw new RuntimeException("Properties file " + propsPath + " does not exist.")
    }

    if (!propsFile.canRead) {
      throw new RuntimeException("Properties file " + propsPath + " is not readable; check file privileges.")
    }
    val props = new Properties()
    try_(new FileInputStream(propsFile)) { propsFileStream =>
      props.load(propsFileStream)
    }
    props
  }

}

class TestBoot extends Boot {
  def setUpAndBoot() {
    val url = Thread.currentThread.getContextClassLoader.getResource("default.props")
    System.setProperty(PROPERTIES_FILE_NAME_PROP, url.getPath)
    boot()
  }
}
