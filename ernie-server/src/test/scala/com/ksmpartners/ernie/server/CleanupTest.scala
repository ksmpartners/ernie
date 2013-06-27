/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.server

import filter.SAMLConstants
import org.testng.annotations._
import net.liftweb.mockweb.{ MockWeb, WebSpec }
import bootstrap.liftweb.Boot
import net.liftweb.mocks.MockHttpServletRequest
import com.ksmpartners.ernie.server.PropertyNames._
import com.ksmpartners.ernie.model._
import org.testng.Assert
import net.liftweb.http._
import java.util.Properties
import java.io.{ FileOutputStream, FileInputStream, File }
import com.ksmpartners.ernie.util.Utility._
import org.slf4j.{ Logger, LoggerFactory }
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonAST.{ JBool, JField, JObject }
import com.ksmpartners.common.annotations.tracematrix.{ TestSpecs, TestSpec }
import com.ksmpartners.ernie.util.MapperUtility._
import net.liftweb.http.StreamingResponse
import net.liftweb.json.JsonAST.JObject
import net.liftweb.http.ResponseWithReason
import net.liftweb.http.BadResponse
import net.liftweb.http.GoneResponse
import net.liftweb.json.JsonAST.JField
import net.liftweb.json.JsonAST.JBool
import scala.xml.NodeSeq
import scala.collection.JavaConversions.asJavaCollection
import com.ksmpartners.ernie.server.service.{ ServiceRegistry, ConflictResponse }

import com.ksmpartners.ernie.engine.PurgeResponse
import com.ksmpartners.ernie.engine.PurgeRequest
import scala.collection.JavaConversions

class CleanupTest extends WebSpec(() => Unit) with TestSetupUtilities {

  private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.server.CleanupTest")

  @AfterClass(groups = Array("REST"))
  def finish() {

  }

  @AfterMethod
  def logMethodAfter(result: java.lang.reflect.Method) {
    log.debug("END test:" + result.getName)
  }

  @BeforeMethod
  def logMethodBefore(result: java.lang.reflect.Method) {
    log.debug("BEGIN test:" + result.getName)
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-69")))
  @Test(dependsOnMethods = Array("canDeleteReportResults"))
  def jobStatusReturns410ForDeletedReports() {
    val mockReq = new MockReadAuthReq("/jobs/" + testJobID + "/status")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertEquals(resp.open_!.toResponse.code, 410)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-70")))
  @Test(dependsOnMethods = Array("canDeleteReportResults"))
  def downloadServiceReturns410ForDeletedReports() {
    val mockReq = new MockReadAuthReq("/jobs/" + testJobID + "/result")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)

      Assert.assertEquals(resp.open_!.getClass, classOf[ResponseWithReason])
      Assert.assertEquals(resp.open_!.toResponse.code, 410)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-74")))
  @Test
  def canDeleteReportResults() {
    completeJob

    val mockReq = new MockWriteAuthReq("/jobs/" + testJobID + "/result")
    mockReq.method = "DELETE"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[PlainTextResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 200)
      val deleteResponse: DeleteResponse = DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[DeleteResponse])
      Assert.assertTrue(deleteResponse.getDeleteStatus == DeleteStatus.SUCCESS)
    }
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-156")))
  @Test(dependsOnMethods = Array("canDeleteReportResults"))
  def canGetDeletedJobsCatalog() {
    val mockReq = new MockReadAuthReq("/jobs/deleted")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[PlainTextResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 200)
      val jobCatalogResp: JobsCatalogResponse = DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[JobsCatalogResponse])
      import JavaConversions._
      Assert.assertTrue(jobCatalogResp.getJobsCatalog.toList.filter(p => p.getJobId == testJobID).length > 0)
    }
  }

  @Test
  def canGetApiJSON() {
    def saveApiJSON(d: String) = {
      val mockReq = new MockReadAuthReq("/" + d)
      MockWeb.testReq(mockReq) {
        req =>
          val resp = DispatchRestAPI(req)()
          Assert.assertTrue(resp.isDefined)
          Assert.assertEquals(resp.open_!.getClass, classOf[JsonResponse])
          val file = new File(new File(Thread.currentThread.getContextClassLoader.getResource("in").getPath).getParent, d)
          var fos = new FileOutputStream(file)
          fos.write(resp.open_!.asInstanceOf[JsonResponse].json.toJsCmd.getBytes)
          fos.close
      }
    }
    saveApiJSON("resources.json")
    saveApiJSON("jobs.json")
    saveApiJSON("defs.json")
  }

}
