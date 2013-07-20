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
import java.io.{ FileInputStream, File }
import com.ksmpartners.ernie.util.Utility._
import org.slf4j.{ Logger, LoggerFactory }
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonAST.{ JBool, JField, JObject }

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

class HeadTest extends WebSpec(() => Unit) with TestSetupUtilities {

  private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.server.HeadTest")

  @Test
  def canGetJobsSupportsHead() {
    val mockReq = new MockReadAuthReq("/jobs")
    mockReq.method = "HEAD"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[InMemoryResponse])
      val resultResp = resp.open_!.asInstanceOf[InMemoryResponse]
      Assert.assertEquals(resultResp.size, 0)
      Assert.assertEquals(resultResp.code, 200)
    }
  }

  @AfterClass(groups = Array("REST"))
  def finish() {

  }

  @Test
  def canGetJobStatusSupportsHead() {
    val mockReq = new MockReadAuthReq("/jobs/" + testJobID + "/status")
    mockReq.method = "HEAD"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[InMemoryResponse])
      val resultResp = resp.open_!.asInstanceOf[InMemoryResponse]
      Assert.assertEquals(resultResp.size, 0)
      Assert.assertEquals(resultResp.code, 200)
    }
  }

  @Test
  def canGetOutputDownloadSupportsHead() {
    completeJob
    val mockReq = new MockReadAuthReq("/jobs/" + testJobID + "/result")
    mockReq.method = "HEAD"
    mockReq.headers += ("Accept" -> List("application/pdf"))

    MockWeb.testReq(mockReq) { req =>
      val respBox = DispatchRestAPI(req)()
      Assert.assertTrue(respBox.isDefined)
      Assert.assertTrue(respBox.open_!.isInstanceOf[InMemoryResponse])

      val resultResp = respBox.open_!.asInstanceOf[InMemoryResponse]
      Assert.assertEquals(resultResp.size, 0)
      Assert.assertEquals(resultResp.code, 200)
      Assert.assertTrue(resultResp.headers.contains(("Content-Type", "application/pdf")) && resultResp.headers.contains(("Content-Disposition", "attachment; filename=\"REPORT_" + testJobID + ".pdf\"")))
    }
  }

  @Test(dependsOnMethods = Array("canGetOutputDownloadSupportsHead"))
  def canGetReportDetailSupportsHead() {
    val mockReq = new MockReadAuthReq("/jobs/" + testJobID + "/result/detail")
    mockReq.method = "HEAD"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[InMemoryResponse])
      val resultResp = resp.open_!.asInstanceOf[InMemoryResponse]
      Assert.assertEquals(resultResp.size, 0)
      Assert.assertEquals(resultResp.code, 200)
    }
  }

  @Test
  def canGetDefDetailsSupportsHead() {
    val mockReq = new MockReadAuthReq("/defs/test_def")
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    mockReq.method = "HEAD"
    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[InMemoryResponse])
      val resultResp = resp.open_!.asInstanceOf[InMemoryResponse]
      Assert.assertEquals(resultResp.size, 0)
      Assert.assertEquals(resultResp.code, 200)
    }
  }

  @AfterMethod
  def logMethodAfter(result: java.lang.reflect.Method) {
    log.debug("END test:" + result.getName)
  }

  @BeforeMethod
  def logMethodBefore(result: java.lang.reflect.Method) {
    log.debug("BEGIN test:" + result.getName)
  }

}
