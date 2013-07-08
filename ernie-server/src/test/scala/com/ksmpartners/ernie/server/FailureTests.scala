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
//import com.ksmpartners.common.annotations.tracematrix.{ TestSpecs, TestSpec }
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

class FailureTest extends WebSpec(() => Unit) with TestSetupUtilities {

  private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.server.FailureTest")

  @BeforeClass
  def setupDef() {

  }

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

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-112")))
  @Test
  def unsupportedJobsServiceRequestsReturn405() {
    var mockReq = new MockWriteAuthReq("/jobs")
    mockReq.method = "DELETE"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    check405(mockReq)
    mockReq = new MockWriteAuthReq("/jobs")
    mockReq.method = "PUT"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    check405(mockReq)
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-113")))
  @Test
  def unsupportedJobsStatusServiceRequestsReturn405() {
    var mockReq = new MockWriteAuthReq("/jobs/1/status")
    mockReq.method = "DELETE"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    check405(mockReq)
    mockReq = new MockWriteAuthReq("/jobs/1/status")
    mockReq.method = "PUT"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    check405(mockReq)
    mockReq = new MockWriteAuthReq("/jobs/1/status")
    mockReq.method = "POST"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    check405(mockReq)
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-114")))
  @Test
  def unsupportedJobsResultsServiceRequestsReturn405() {
    var mockReq = new MockWriteAuthReq("/jobs/1/result")
    mockReq.method = "PUT"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    check405(mockReq)
    mockReq = new MockWriteAuthReq("/jobs/1/result")
    mockReq.method = "POST"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    check405(mockReq)
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-115")))
  @Test
  def unsupportedDefsServiceRequestsReturn405() {
    var mockReq = new MockWriteAuthReq("/defs")
    mockReq.method = "PUT"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    check405(mockReq)
    mockReq = new MockWriteAuthReq("/defs")
    mockReq.method = "DELETE"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    check405(mockReq)
    mockReq = new MockWriteAuthReq("/defs/test")
    mockReq.method = "POST"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    check405(mockReq)
  }

  def check405(m: MockHttpServletRequest) {
    MockWeb.testReq(m) { req =>
      {
        val resp = DispatchRestAPI(req)()
        Assert.assertTrue(resp.isDefined)
        Assert.assertTrue(resp.open_!.isInstanceOf[MethodNotAllowedResponse])
        Assert.assertEquals(resp.open_!.toResponse.code, 405)
      }
    }
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-127")))
  @Test
  def cantPurgeReportResultsWithoutWriteAuth() {
    val mockReq = new MockNoAuthReq("/jobs/expired")
    mockReq.method = "DELETE"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[ForbiddenResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 403)
    }
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-131")))
  @Test
  def cantPurgeReportResultsWithoutJSONRequest() {
    val mockReq = new MockWriteAuthReq("/jobs/expired")
    mockReq.method = "DELETE"
    mockReq.headers += ("Accept" -> List("application/vnd.ksmpartners.ernie+xml"))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[NotAcceptableResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 406)
    }
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-85")))
  @Test
  def cantGetJobsWithoutReadAuth() {
    val mockReq = new MockNoAuthReq("/jobs")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[ForbiddenResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 403)
    }
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-89")))
  @Test
  def cantGetJobsWithoutCorrectAcceptHeader() {
    val mockReq = new MockReadAuthReq("/jobs")
    mockReq.headers += ("Accept" -> List("application/vnd.ksmpartners.ernie+xml"))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertEquals(resp.open_!.toResponse.code, 406)
    }
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-39")))
  @Test
  def cantGetDefsWithoutReadAuth() {
    val mockReq = new MockNoAuthReq("/defs")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[ForbiddenResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 403)
    }
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-43")))
  @Test
  def cantGetDefsWithoutCorrectAcceptHeader() {
    val mockReq = new MockReadAuthReq("/defs")
    mockReq.headers += ("Accept" -> List("application/vnd.ksmpartners.ernie+xml"))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[NotAcceptableResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 406)
    }
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-62")))
  @Test
  def cantGetJobStatusWithoutJSONRequest() {
    val mockReq = new MockReadAuthReq("/jobs/1/status")
    mockReq.headers += ("Accept" -> List("application/vnd.ksmpartners.ernie+xml"))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[NotAcceptableResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 406)
    }
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-58")))
  @Test
  def cantGetJobStatusWithoutReadAuth() {
    val mockReq = new MockNoAuthReq("/jobs/1/status")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[ForbiddenResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 403)
    }
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-51")))
  @Test
  def cantPostJobWithoutWriteAuth() {
    val mockReq = new MockNoAuthReq("/jobs")
    mockReq.method = "POST"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    val mockReportReq = new ReportRequest()
    mockReportReq.setDefId("test_def")
    mockReportReq.setRptType(ReportType.HTML)
    mockReq.body = DispatchRestAPI.serialize(mockReportReq).getBytes

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[ForbiddenResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 403)
    }
  }

  @Test
  def cantPostJobWithInvalidDefID() {
    val mockReq = new MockWriteAuthReq("/jobs")
    mockReq.method = "POST"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    val mockReportReq = new ReportRequest()
    mockReportReq.setDefId("INVALID_DEF")
    mockReportReq.setRptType(ReportType.HTML)
    mockReq.body = DispatchRestAPI.serialize(mockReportReq).getBytes

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[ResponseWithReason])
      Assert.assertEquals(resp.open_!.asInstanceOf[ResponseWithReason].reason, "No such definition ID")
    }
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-55")))
  @Test
  def cantPostJobWithoutJSONRequest() {
    val mockReq = new MockWriteAuthReq("/jobs")
    mockReq.method = "POST"
    mockReq.headers += ("Accept" -> List("application/vnd.ksmpartners.ernie+xml"))

    val mockReportReq = new ReportRequest()
    mockReportReq.setDefId("test_def")
    mockReportReq.setRptType(ReportType.HTML)
    mockReq.body = DispatchRestAPI.serialize(mockReportReq).getBytes

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[NotAcceptableResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 406)
    }
  }

  /*@Test  def cantPostJobWithoutExistingReportDefinitionFile() {
    val mockReq = new MockWriteAuthReq("/jobs")
    mockReq.method = "POST"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    val mockReportReq = new ReportRequest()
    mockReportReq.setDefId("WRONG")
    mockReportReq.setRptType(ReportType.HTML)
    mockReq.body = DispatchRestAPI.serialize(mockReportReq)

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      log.info(resp.open_!.toResponse.code + "")
      Assert.assertTrue(resp.open_!.isInstanceOf[BadResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 400)
    }
  } */

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-57")))
  @Test
  def cantPostJobWithoutValidRequestJSON() {
    val mockReq = new MockWriteAuthReq("/jobs")
    mockReq.method = "POST"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    mockReq.body = JObject(List(JField("Invalid JSON?", JBool(true))))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[ResponseWithReason])
      Assert.assertEquals(resp.open_!.toResponse.code, 400)
    }
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-63")))
  @Test
  def cantGetOutputDownloadWithoutReadAuth() {
    val mockReq = new MockNoAuthReq("/jobs/234/result")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertEquals(resp.open_!.toResponse.code, 403)
    }
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-67")))
  @Test
  def cantGetOutputDownloadWithoutCorrectAcceptHeader() {
    completeJob
    val mockReq = new MockReadAuthReq("/jobs/" + testJobID + "/result")

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertEquals(resp.open_!.toResponse.code, 406)
    }
  }

  @Test
  def cantGetJobStatusWithoutLongJobID() {
    val mockReq = new MockReadAuthReq("/jobs/test/status")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[ResponseWithReason])
      Assert.assertEquals(resp.open_!.asInstanceOf[ResponseWithReason].reason, "Job ID provided is not a number: test")
    }
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-76")))
  @Test
  def cantDeleteReportResultsWithoutJSONRequest() {
    val mockReq = new MockWriteAuthReq("/jobs/1/result")
    mockReq.method = "DELETE"
    mockReq.headers += ("Accept" -> List("application/vnd.ksmpartners.ernie+xml"))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[NotAcceptableResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 406)
    }
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-72")))
  @Test
  def cantDeleteReportResultsWithoutWriteAuth() {
    val mockReq = new MockNoAuthReq("/jobs/1/result")
    mockReq.method = "DELETE"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[ForbiddenResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 403)
    }
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-82")))
  @Test
  def cantGetDefDetailsWithoutJSONRequest() {
    val mockReq = new MockReadAuthReq("/defs/test_def")
    mockReq.headers += ("Accept" -> List("application/vnd.ksmpartners.ernie+xml"))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI.apply(req).apply()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[NotAcceptableResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 406)
    }
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-78")))
  @Test
  def cantGetDefDetailsWithoutReadAuth() {
    val mockReq = new MockNoAuthReq("/defs/test_def")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI.apply(req).apply()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[ForbiddenResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 403)
    }
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-96")))
  @Test
  def cantGetDefDetailForNonExistentReportDef() {
    val mockReq = new MockReadAuthReq("/defs/invalid_def")
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI.apply(req).apply()
      Assert.assertTrue(resp.isDefined)
      Assert.assertEquals(resp.open_!.toResponse.code, 404)
    }
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-71")))
  @Test
  def cantPostJobIfRetentionDateInPast() {
    val mockReq = new MockWriteAuthReq("/jobs")
    mockReq.method = "POST"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    val mockReportReq = new ReportRequest()
    mockReportReq.setDefId("test_def")
    mockReportReq.setRptType(ReportType.PDF)
    mockReportReq.setRetentionDays(-1)
    mockReq.body = DispatchRestAPI.serialize(mockReportReq)

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[ResponseWithReason])
      Assert.assertEquals(resp.open_!.toResponse.code, 400)
      Assert.assertEquals(resp.open_!.asInstanceOf[ResponseWithReason].reason, "Retention date before request time")
    }
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-12")))
  @Test
  def cantPostJobIfRetentionDateBeyondMaximum() {
    val mockReq = new MockWriteAuthReq("/jobs")
    mockReq.method = "POST"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    val mockReportReq = new ReportRequest()
    mockReportReq.setDefId("test_def")
    mockReportReq.setRptType(ReportType.PDF)
    mockReportReq.setRetentionDays(9999)
    mockReq.body = DispatchRestAPI.serialize(mockReportReq)

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[ResponseWithReason])
      Assert.assertEquals(resp.open_!.toResponse.code, 400)
      Assert.assertEquals(resp.open_!.asInstanceOf[ResponseWithReason].reason, "Retention date exceeds maximum")
    }
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-94")))
  @Test
  def cantDeleteDefsWithoutWriteAuth() {
    val mockReq = new MockNoAuthReq("/defs/test_def2")
    mockReq.method = "DELETE"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertEquals(resp.open_!.toResponse.code, 403)
      Assert.assertTrue(resp.open_!.isInstanceOf[ForbiddenResponse])
    }
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-97")))
  @Test
  def cantDeleteDefsWithoutCorrectAcceptHeader() {

    val mockReq = new MockWriteAuthReq("/defs/test_def2")
    mockReq.method = "DELETE"

    mockReq.headers += ("Accept" -> List("application/vnd.ksmpartners.ernie+xml"))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[NotAcceptableResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 406)
    }
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-45")))
  @Test
  def cantPostDefsWithoutWriteAuth() {
    val mockReq = new MockNoAuthReq("/defs")
    mockReq.method = "POST"

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    val defEnt = new DefinitionEntity()
    defEnt.setCreatedUser("default")
    defEnt.setDefId("test_def2")
    mockReq.body = DispatchRestAPI.serialize(defEnt).getBytes

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)

      Assert.assertEquals(resp.open_!.toResponse.code, 403)

      Assert.assertTrue(resp.open_!.isInstanceOf[ForbiddenResponse])

    }
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-50")))
  @Test
  def cantPostDefsWithoutJSONRequest() {
    val mockReq = new MockWriteAuthReq("/defs")
    mockReq.method = "POST"

    mockReq.headers += ("Accept" -> List("application/vnd.ksmpartners.ernie+xml"))

    val defEnt = new DefinitionEntity()
    defEnt.setCreatedUser("default")
    defEnt.setDefId("test_def2")
    mockReq.body = DispatchRestAPI.serialize(defEnt).getBytes

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)

      Assert.assertEquals(resp.open_!.toResponse.code, 406)

      Assert.assertTrue(resp.open_!.isInstanceOf[NotAcceptableResponse])

    }
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-98")))
  @Test
  def cantPutDefsWithoutWriteAuth() {
    val mockReq = new MockNoAuthReq("/defs/test_def2/rptdesign")
    mockReq.method = "PUT"

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    mockReq.headers += ("Content-Type" -> List("application/rptdesign+xml"))

    val file = new File(Thread.currentThread.getContextClassLoader.getResource("in/test_def.rptdesign").getPath)

    mockReq.body = scala.xml.XML.loadFile(file)

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)

      Assert.assertEquals(resp.open_!.toResponse.code, 403)

      Assert.assertTrue(resp.open_!.isInstanceOf[ForbiddenResponse])

    }
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-102")))
  @Test
  def cantPutDefsWithoutJSONRequest() {
    val mockReq = new MockWriteAuthReq("/defs/test_def2/rptdesign")
    mockReq.method = "PUT"

    mockReq.headers += ("Accept" -> List("application/vnd.ksmpartners.ernie+xml"))

    mockReq.headers += ("Content-Type" -> List("application/rptdesign+xml"))

    val file = new File(Thread.currentThread.getContextClassLoader.getResource("in/test_def.rptdesign").getPath)

    mockReq.body = scala.xml.XML.loadFile(file)

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)

      Assert.assertEquals(resp.open_!.toResponse.code, 406)

      Assert.assertTrue(resp.open_!.isInstanceOf[NotAcceptableResponse])

    }
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-133")))
  @Test
  def cantPostJobWithInvalidParamDataTypes() {
    var mockReq: MockHttpServletRequest = new MockWriteAuthReq("/jobs")
    mockReq.method = "POST"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    val mockReportReq = new ReportRequest()
    mockReportReq.setDefId(testDef)
    mockReportReq.setRptType(ReportType.PDF)
    val rptParams: java.util.HashMap[String, String] = new java.util.HashMap[String, String]()
    rptParams.put("MinQuantityInStock", "string data")
    mockReportReq.setReportParameters(rptParams)
    mockReq.body = DispatchRestAPI.serialize(mockReportReq).getBytes
    var testParamsJobID = -1L
    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()

      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[PlainTextResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 201)
      val reportResponse: ReportResponse = DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[ReportResponse])
      testParamsJobID = reportResponse.getJobId()
      Assert.assertTrue(testParamsJobID > -1L)
    }

    mockReq = new MockReadAuthReq("/jobs/" + testParamsJobID + "/status")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    var jobRunning = true
    val end = System.currentTimeMillis + (1000 * 10)
    var failed = false
    while (jobRunning && (System.currentTimeMillis < end)) {
      MockWeb.testReq(mockReq) { req =>
        val resp = DispatchRestAPI(req)()
        resp.map(r =>
          if (r.isInstanceOf[PlainTextResponse]) {
            if (DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[StatusResponse]).getJobStatus != JobStatus.IN_PROGRESS) {
              jobRunning = false
              Assert.assertTrue(DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[StatusResponse]).getJobStatus == JobStatus.FAILED_INVALID_PARAMETER_VALUES)
              failed = true
            }
          })
      }
    }
    Assert.assertTrue(failed)
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-133")))
  @Test
  def cantPostJobWithoutNonNullParam() {
    var mockReq: MockHttpServletRequest = new MockWriteAuthReq("/jobs")
    mockReq.method = "POST"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    val mockReportReq = new ReportRequest()
    mockReportReq.setDefId(testDef)
    mockReportReq.setRptType(ReportType.PDF)

    mockReq.body = DispatchRestAPI.serialize(mockReportReq).getBytes
    var testParamsJobID = -1L
    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()

      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[PlainTextResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 201)
      val reportResponse: ReportResponse = DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[ReportResponse])
      testParamsJobID = reportResponse.getJobId()
      Assert.assertTrue(testParamsJobID > -1L)
    }

    mockReq = new MockReadAuthReq("/jobs/" + testParamsJobID + "/status")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    var jobRunning = true
    val end = System.currentTimeMillis + (1000 * 300)
    while (jobRunning && (System.currentTimeMillis < end)) {
      MockWeb.testReq(mockReq) { req =>
        val resp = DispatchRestAPI(req)()
        resp.map(r =>
          if (r.isInstanceOf[PlainTextResponse])
            if (DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[StatusResponse]).getJobStatus != JobStatus.IN_PROGRESS) {
            jobRunning = false
            Assert.assertEquals(DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[StatusResponse]).getJobStatus, JobStatus.FAILED_PARAMETER_NULL)
          })
      }
    }
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-147")))
  @Test
  def cantGetReportDetailWithoutJSONRequest() {

    val mockReq = new MockReadAuthReq("/jobs/1/result/detail")
    mockReq.headers += ("Accept" -> List("application/vnd.ksmpartners.ernie+xml"))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[NotAcceptableResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 406)
    }
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-143")))
  @Test
  def cantGetReportDetailWithoutReadAuth() {
    val mockReq = new MockNoAuthReq("/jobs/1/result/detail")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[ForbiddenResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 403)
    }
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-141")))
  @Test
  def cantGetJobDetailWithoutJSONRequest() {

    val mockReq = new MockReadAuthReq("/jobs/1")
    mockReq.headers += ("Accept" -> List("application/vnd.ksmpartners.ernie+xml"))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[NotAcceptableResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 406)
    }
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-137")))
  @Test
  def cantGetJobDetailWithoutReadAuth() {
    val mockReq = new MockNoAuthReq("/jobs/1")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[ForbiddenResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 403)
    }
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-154")))
  @Test
  def cantGetJobsCatalogWithoutJSONAcceptHeader() {
    val mockReq = new MockReadAuthReq("/jobs/catalog")
    mockReq.headers += ("Accept" -> List("application/vnd.ksmpartners.ernie+xml"))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[NotAcceptableResponse])

    }
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-150")))
  @Test
  def cantGetJobsCatalogWithoutReadAuth() {
    val mockReq = new MockNoAuthReq("/jobs/catalog")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[ForbiddenResponse])

    }
  }

}
