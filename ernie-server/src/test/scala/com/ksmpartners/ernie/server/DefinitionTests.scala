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
import java.io.{ FileOutputStream, FileInputStream, File }
import com.ksmpartners.ernie.util.Utility._
import org.slf4j.{ Logger, LoggerFactory }
import net.liftweb.json.JsonAST
import net.liftweb.json.JsonAST.{ JBool, JField, JObject }
import net.liftweb.http.auth.{ AuthRole }
import net.liftweb.mockweb.MockWeb.useLiftRules
import net.liftweb.util.NamedPF

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
import org.joda.time.DateTime
import com.ksmpartners.ernie.server.service.ServiceRegistry._
import net.liftweb.http.ResponseWithReason
import com.ksmpartners.ernie.server.service.ConflictResponse
import net.liftweb.common.{ Full, Box }

class DefinitionTest extends WebSpec(() => {
  val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.server.DefinitionTest")

  val properties: Properties = {
    val url = Thread.currentThread.getContextClassLoader.getResource("test.props")
    System.setProperty(propertiesFileNameProp, url.getPath)
    val propsPath = System.getProperty(propertiesFileNameProp)

    if (null == propsPath) {
      throw new RuntimeException("System property " + propertiesFileNameProp + " is undefined")
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

  val outputDir = new File(properties.get("output.dir").toString)
  val jobsDir = new File(properties.get("jobs.dir").toString)
  val defsDir = new File(properties.get("rpt.def.dir").toString)

  if (outputDir.listFiles != null) for (file <- outputDir.listFiles()) {
    recDel(file)
  }
  if (jobsDir.listFiles != null) for (file <- jobsDir.listFiles()) {
    recDel(file)
  }
  if (defsDir.listFiles != null) for (file <- defsDir.listFiles()) {
    if (!file.getName.contains("test_def")) recDel(file)
  }

  for (i <- 1 to 4) {
    var report = new ReportEntity(DateTime.now, if (i % 2 == 0) DateTime.now.minusDays(10) else DateTime.now.plusDays(4), "REPORT_" + i, "test_def", "default", null, ReportType.PDF, null, null)
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

      val job = new File(jobsDir, rptToJobId(report.getRptId) + ".entity")
      val jobEnt: JobEntity = new JobEntity(rptToJobId(report.getRptId), if (i % 2 == 0) JobStatus.COMPLETE else JobStatus.IN_PROGRESS, DateTime.now, report.getRptId, if (i % 2 == 0) null else report)
      try_(new FileOutputStream(job)) { fos =>
        mapper.writeValue(fos, jobEnt)
      }
    } catch {
      case e: Exception => log.info("Caught exception while generating test entities: {}", e.getMessage + "\n" + e.getStackTraceString)
    }
  }

  (new TestBoot).setUpAndBoot()
}) {

  val properties: Properties = {
    val url = Thread.currentThread.getContextClassLoader.getResource("test.props")
    System.setProperty(propertiesFileNameProp, url.getPath)
    val propsPath = System.getProperty(propertiesFileNameProp)

    if (null == propsPath) {
      throw new RuntimeException("System property " + propertiesFileNameProp + " is undefined")
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

  @Test(enabled = false)
  private var outputDir: File = null

  @Test(enabled = false)
  private var jobsDir: File = null

  @Test(enabled = false)
  private var defsDir: File = null

  @Test(enabled = false)
  private[this] var testDef: String = ""

  private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.server.DefinitionTest")

  @Test(enabled = false)
  val liftRules = new LiftRules()

  object userRoles {
    private var r: List[net.liftweb.http.auth.Role] = Nil
    def set(roles: List[net.liftweb.http.auth.Role]) {
      r = roles
    }
    def get = r
  }

  @BeforeSuite
  def setup() {
    outputDir = new File(properties.get("output.dir").toString)
    jobsDir = new File(properties.get("jobs.dir").toString)
    defsDir = new File(properties.get("rpt.def.dir").toString)

    System.setProperty(PropertyNames.authModeProp, "BASIC")

    LiftRulesMocker.devTestLiftRulesInstance.doWith(liftRules) {

      DispatchRestAPI.basicAuthentication = {
        case (u: String, p: String, req) => {
          userRoles.set(Nil)
          if (u == "") false
          else {
            if (u == "mockReadUser") userRoles.set(AuthRole(SAMLConstants.readRole) :: Nil)
            if (u == "mockWriteUser") userRoles.set(AuthRole(SAMLConstants.writeRole) :: Nil)
            if (u == "mockRunUser") userRoles.set(AuthRole(SAMLConstants.runRole) :: Nil)
            true
          }
        }
      }

      LiftRules.statelessDispatch.prepend(DispatchRestAPI)

      if (System.getProperty(PropertyNames.authModeProp) == "BASIC") {
        LiftRules.authentication = net.liftweb.http.auth.HttpBasicAuthentication("Ernie Server")(DispatchRestAPI.basicAuthentication)
        LiftRules.httpAuthProtectedResource.prepend(DispatchRestAPI.protectedResources)
      }
    }

  }

  @AfterClass(groups = Array("REST"))
  def finish() {

  }

  @AfterSuite
  def shutdown() {

    DispatchRestAPI.shutdown()

    if (outputDir.listFiles != null) for (file <- outputDir.listFiles()) {
      recDel(file)
    }

    if (jobsDir.listFiles != null) for (file <- jobsDir.listFiles()) {
      recDel(file)
    }

    if (defsDir.listFiles != null) for (file <- defsDir.listFiles()) {
      if (!file.getName.contains("test_def")) recDel(file)
    }

    var keep = new File(outputDir, ".keep")

    keep.createNewFile()

    keep = new File(jobsDir, ".keep")

    keep.createNewFile()

  }

  @Test
  def basicAuthGetJobs() {
    val read = new MockReadAuthReq("/jobs")
    read.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    basicAuthentication(read, Some(200))

    val noauth = new MockNoAuthReq("/jobs")
    noauth.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    basicAuthentication(noauth, None)

    val nocred = new MockNoCredReq("/jobs")
    nocred.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    basicAuthentication(nocred, None)
  }

  def basicAuthentication(request: MockHttpServletRequest, successful: Option[Int]) { //This method imitates the functionality in LiftServlet that authenticates requests
    LiftRulesMocker.devTestLiftRulesInstance.doWith(liftRules) {
      useLiftRules.doWith(true) {
        MockWeb.testReq(request) {
          req =>
            {
              val roleForReq = NamedPF.applyBox(req, LiftRules.httpAuthProtectedResource.toList)

              def checkUserRolesInResourceRoles(resRole: net.liftweb.http.auth.Role, userRoles: List[net.liftweb.http.auth.Role]): Boolean = {
                val result = userRoles.foldLeft(false)((s: Boolean, d: net.liftweb.http.auth.Role) => {
                  val res = s || (d.name == resRole.name) || resRole.isParentOf(d.name)
                  res
                })
                result
              }

              val auth = roleForReq.map {
                case Full(r) => LiftRules.authentication.verified_?(req) match {
                  case true => {
                    checkUserRolesInResourceRoles(r, userRoles.get)
                  }
                  case _ => false
                }
                case _ => LiftRules.authentication.verified_?(req)
              } openOr true

              if (successful.isDefined) Assert.assertTrue(auth)
              else Assert.assertFalse(auth)

              LiftRules.statelessDispatch.toList.foreach(f => {
                val resp = f.apply(req)()
                Assert.assertTrue(resp.isDefined)
                successful match {
                  case Some(i: Int) => Assert.assertEquals(resp.open_!.toResponse.code, i)
                  case None => Assert.assertEquals(resp.open_!.toResponse.code, ForbiddenResponse().toResponse.code)
                }
              })
            }
        }
      }
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

  @Test
  def downloadServiceReturn410ForExpiredReports() {
    val mockReq = new MockReadAuthReq("/jobs/2/result")

    mockReq.headers += ("Accept" -> List("application/pdf"))

    MockWeb.testReq(mockReq) { req =>
      val respBox = DispatchRestAPI(req)()
      Assert.assertTrue(respBox.isDefined)
      Assert.assertEquals(respBox.open_!.getClass, classOf[ResponseWithReason])
      Assert.assertEquals(respBox.open_!.toResponse.code, 410)
    }
  }

  @Test(dependsOnMethods = Array("downloadServiceReturn410ForExpiredReports"))
  def canPurgeJobs() {
    val mockReq = new MockWriteAuthReq("/jobs/expired")
    mockReq.method = "DELETE"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[OkResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 200)

    }

    var mockReq2 = new MockReadAuthReq("/jobs/2/status")

    mockReq2.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq2) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertEquals(resp.open_!.getClass, classOf[ResponseWithReason])
      Assert.assertEquals(resp.open_!.toResponse.code, GoneResponse().toResponse.code)
    }

    mockReq2 = new MockReadAuthReq("/jobs/4/status")

    mockReq2.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq2) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertEquals(resp.open_!.getClass, classOf[ResponseWithReason])
      Assert.assertEquals(resp.open_!.toResponse.code, GoneResponse().toResponse.code)
    }

  }

  @Test
  def jobsInProgressOnShutDownAreRestarted() {
    var jobRunning = true
    var end = System.currentTimeMillis + (1000 * 30)
    var job1Complete = false
    var job2Complete = false
    while (jobRunning && (System.currentTimeMillis < end)) {
      val respOpt = DispatchRestAPI.deserialize(jobStatusResource.get("1").open_!.asInstanceOf[PlainTextResponse].text.getBytes, classOf[StatusResponse])
      if (respOpt.getJobStatus == JobStatus.COMPLETE) {
        job1Complete = true
        jobRunning = false
      }
    }
    jobRunning = true
    end = System.currentTimeMillis + (1000 * 30)
    while (jobRunning && (System.currentTimeMillis < end)) {
      val respOpt = DispatchRestAPI.deserialize(jobStatusResource.get("3").open_!.asInstanceOf[PlainTextResponse].text.getBytes, classOf[StatusResponse])
      if (respOpt.getJobStatus == JobStatus.COMPLETE) {
        job2Complete = true
        jobRunning = false
      }
    }
    Assert.assertTrue(job1Complete)
    Assert.assertTrue(job2Complete)
  }

  @Test
  def canGetDefs() {
    val mockReq = new MockReadAuthReq("/defs")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[PlainTextResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 200)
    }
  }

  @Test
  def getDefsReturnsJSON() {
    val mockReq = new MockReadAuthReq("/defs")

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.toResponse.headers.contains(("Content-Type", "application/vnd.ksmpartners.ernie+json")))
      val body = resp.open_!.asInstanceOf[PlainTextResponse].text
      val respObj = mapper.readValue(body, classOf[ReportDefinitionMapResponse])
      Assert.assertNotNull(respObj.getReportDefMap)
    }
  }

  @Test(enabled = false)
  def cantDeleteInUseDef() {
    val mockReq = new MockWriteAuthReq("/defs/" + testDef)
    mockReq.method = "DELETE"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)

      Assert.assertTrue(resp.open_!.isInstanceOf[ConflictResponse])
    }
  }

  @Test
  def canGetDefDetails() {
    val mockReq = new MockReadAuthReq("/defs/test_def")
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[PlainTextResponse])
      Assert.assertEquals(resp.open_!.toResponse.code, 200)
      val body = resp.open_!.asInstanceOf[PlainTextResponse].text
      val respObj = mapper.readValue(body, classOf[DefinitionEntity])
      Assert.assertNotNull(respObj.getDefId)
    }
  }

  @Test
  def defDetailServiceReturnsJSON() {
    val mockReq = new MockReadAuthReq("/defs/test_def")
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))
    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.toResponse.headers.contains(("Content-Type", "application/vnd.ksmpartners.ernie+json")))
    }
  }

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

  @Test
  def canPostDefs() {
    val mockReq = new MockWriteAuthReq("/defs")
    mockReq.method = "POST"

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    val defEnt = new DefinitionEntity()
    defEnt.setCreatedUser("default")
    defEnt.setDefId("test_def2")
    mockReq.body = DispatchRestAPI.serialize(defEnt).getBytes

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)

      Assert.assertEquals(resp.open_!.toResponse.code, 201)

      Assert.assertTrue(resp.open_!.isInstanceOf[PlainTextResponse])

      val defEntRsp: DefinitionEntity = DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[DefinitionEntity])
      Assert.assertEquals(defEntRsp.getCreatedUser, "mockWriteUser")
      testDef = defEntRsp.getDefId
      Assert.assertTrue(resp.open_!.toResponse.headers.contains(("Location", req.hostAndPath + "/defs/" + defEntRsp.getDefId)))

    }

  }

  @Test(dependsOnMethods = Array("canPostDefs"))
  def canDeleteDefs() {
    val mockReq = new MockWriteAuthReq("/defs/" + testDef)
    mockReq.method = "DELETE"
    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)
      Assert.assertTrue(resp.open_!.isInstanceOf[PlainTextResponse])
      val deleteDefinitionResponse = DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[DeleteDefinitionResponse])
      Assert.assertEquals(deleteDefinitionResponse.getDeleteStatus, DeleteStatus.SUCCESS)
    }

    canPostDefs
  }

  @Test(dependsOnMethods = Array("canDeleteDefs"))
  def canPutDefs() {
    val mockReq = new MockWriteAuthReq("/defs/" + testDef + "/rptdesign")
    mockReq.method = "PUT"

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    mockReq.headers += ("Content-Type" -> List("application/rptdesign+xml"))

    val file = new File(Thread.currentThread.getContextClassLoader.getResource("in/test_def.rptdesign").getPath)

    mockReq.body = scala.xml.XML.loadFile(file)

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)

      Assert.assertEquals(resp.open_!.toResponse.code, 201)

      Assert.assertTrue(resp.open_!.isInstanceOf[PlainTextResponse])

      val defEntRsp: DefinitionEntity = DispatchRestAPI.deserialize(resp.open_!.asInstanceOf[PlainTextResponse].toResponse.data, classOf[DefinitionEntity])

      Assert.assertEquals(defEntRsp.getDefId, testDef)
    }
  }

  @Test(dependsOnMethods = Array("canPutDefs"))
  def canPutDefsWithParams() {
    val mockReq = new MockWriteAuthReq("/defs/" + testDef + "/rptdesign")
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

  @Test(dependsOnMethods = Array("canPostDefs"))
  def invalidDefinitionPutReturns400() {
    val mockReq = new MockWriteAuthReq("/defs/" + testDef + "/rptdesign")
    mockReq.method = "PUT"

    mockReq.headers += ("Accept" -> List(ModelObject.TYPE_FULL))

    mockReq.headers += ("Content-Type" -> List("application/rptdesign+xml"))

    mockReq.body = <report><invalid>Definition</invalid></report>

    MockWeb.testReq(mockReq) { req =>
      val resp = DispatchRestAPI(req)()
      Assert.assertTrue(resp.isDefined)

      Assert.assertEquals(resp.open_!.toResponse.code, 400)

      Assert.assertTrue(resp.open_!.isInstanceOf[ResponseWithReason])

    }
  }

}

class MockReadAuthReq(path: String) extends MockHttpServletRequest(path) {
  override def isUserInRole(role: String) = role match {
    case SAMLConstants.readRole => true
    case _ => false
  }
  override def getRemoteUser: String = "mockReadUser"
  addBasicAuth(getRemoteUser, "pass")
}

class MockWriteAuthReq(path: String) extends MockHttpServletRequest(path) {
  override def isUserInRole(role: String) = role match {
    case SAMLConstants.writeRole => true
    case _ => false
  }
  override def getRemoteUser: String = "mockWriteUser"
  addBasicAuth(getRemoteUser, "pass")

}

class MockRunAuthReq(path: String) extends MockHttpServletRequest(path) {
  override def isUserInRole(role: String) = role match {
    case SAMLConstants.runRole => true
    case _ => false
  }
  override def getRemoteUser: String = "mockRunUser"
  addBasicAuth(getRemoteUser, "pass")

}

class MockNoAuthReq(path: String) extends MockHttpServletRequest(path) {
  override def isUserInRole(role: String) = false
  override def getRemoteUser: String = "mockNoAuthUser"
  addBasicAuth(getRemoteUser, "pass")
}

class MockNoCredReq(path: String) extends MockHttpServletRequest(path) {
  override def isUserInRole(role: String) = false
  override def getRemoteUser: String = ""
  addBasicAuth(getRemoteUser, "")
}

class TestBoot extends Boot {
  private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.server.DispatchRestAPITest")

  def setUpAndBoot() {

    boot()
  }
}
