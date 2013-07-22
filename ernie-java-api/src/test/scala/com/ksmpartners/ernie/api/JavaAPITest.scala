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
 *
 */

package com.ksmpartners.ernie.api

import _root_.java.io.ByteArrayInputStream
import com.ksmpartners.ernie.util.Utility._
import org.testng.annotations._
import scala.concurrent.duration._
import org.testng.Assert
import java.io.{ ByteArrayInputStream, File }
import com.ksmpartners.ernie.model.{ DeleteStatus, ReportType }
import org.slf4j.{ LoggerFactory, Logger }
import scala.Array
import ErnieBuilder._
import ApiTestUtil._
import org.joda.time.DateTime

object ApiTestUtil {
  def testException[B <: Exception](func: () => Unit, ex: Class[B]) = try {
    func()
    Assert.assertTrue(false)
  } catch {
    case t: Throwable => Assert.assertEquals(t.getClass, ex)
  }
}

class JavaAPITest { //extends TestLogger {

  @BeforeClass
  private var ernie: ErnieController = null
  private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.api.APITest")

  @BeforeClass(dependsOnGroups = Array("tTCleanUp"))
  def startup() {
  }

  @AfterClass
  def shutdown() {
    ernie.shutDown
  }

  @BeforeClass
  private var defId = ""

  @BeforeClass
  private var jobId = -1L

  @Test
  def init() {
    log.info("Beginning APITest")
    val ec = new ErnieConfig.Builder(new FileReportManager(createTempDirectory.getAbsolutePath, createTempDirectory().getAbsolutePath, createTempDirectory.getAbsolutePath))
      .withDefaultRetentionDays(5)
      .withMaxRetentionDays(10)
      .timeoutAfter(5 minutes)
      .withWorkers(5)
      .build()

    ernie = new ErnieController
    ernie.configure(ec)
    ernie.start
  }

  @Test(groups = Array("setup"), dependsOnMethods = Array("init"))
  def createDefinition() {
    val xml = scala.xml.XML.loadFile(new File(Thread.currentThread.getContextClassLoader.getResource("in/test_def.rptdesign").getPath))
    try_(new ByteArrayInputStream(xml.toString.getBytes)) { bAIS: ByteArrayInputStream =>
      {
        val resp = ernie.createDefinition(bAIS, "test", "test")
        defId = resp.getDefId
      }
    }
    Assert.assertNotSame(defId, "")
  }

  //@TestSpecs(Array(new TestSpec(key = "ERNIE-185"), new TestSpec(key = "ERNIE-186")))
  @Test(dependsOnMethods = Array("createDefinition"), groups = Array("setup"))
  def updateDefinition() {
    val resp = ernie.updateDefinition(defId, ernie.getDefinitionEntity(defId))
    Assert.assertEquals(resp.getDefId, defId)
    try_(new ByteArrayInputStream(<break/>.toString.getBytes)) { bAIS =>
      testException(() => ernie.updateDefinition(defId, bAIS), classOf[InvalidDefinitionException])
    }
    val xml = scala.xml.XML.loadFile(new File(Thread.currentThread.getContextClassLoader.getResource("in/test_def_params.rptdesign").getPath))
    try_(new ByteArrayInputStream(xml.toString.getBytes)) { bAIS =>
      Assert.assertTrue(ernie.updateDefinition(defId, bAIS).getParams.size > 0)
    }
  }

  @Test(dependsOnMethods = Array("updateDefinition"), groups = Array("setup"))
  def createJob() {
    var je = ernie.createJob("test", ReportType.PDF, null, "test")
    val id = je.getJobId
    val status = je.getJobStatus
    Assert.assertEquals(status, com.ksmpartners.ernie.model.JobStatus.FAILED_NO_SUCH_DEFINITION)
    jobId = ernie.createJob(defId, ReportType.PDF, null, "test").getJobId
    Assert.assertTrue(jobId > 0)
  }

  @Test(dependsOnGroups = Array("setup"), groups = Array("main"))
  def getDefinitionsCatalog() {
    Assert.assertTrue(ernie.getDefinitionsCatalog.size > 0)
  }

  @Test(dependsOnGroups = Array("setup"), groups = Array("main"))
  def getDefList() {
    Assert.assertTrue(ernie.getDefinitionList.size > 0)
  }

  @Test(dependsOnGroups = Array("setup"), groups = Array("main"))
  def getDefinitionEntity() {
    testException(() => ernie.getDefinitionEntity(null), classOf[MissingArgumentException])
    testException(() => ernie.getDefinitionEntity("test"), classOf[NotFoundException])
    Assert.assertEquals(ernie.getDefinitionEntity(defId).getDefId, defId)
  }

  @Test(dependsOnGroups = Array("setup"), groups = Array("main"))
  def getDefinitionDesign() {
    testException(() => ernie.getDefinitionDesign(null), classOf[MissingArgumentException])
    val b = ernie.getDefinitionDesign(defId)
    Assert.assertTrue(scala.xml.XML.load(b).toString.length > 10)
  }

  @Test(dependsOnGroups = Array("setup"), groups = Array("main"))
  def getStatus(): com.ksmpartners.ernie.model.JobStatus = {
    testException(() => ernie.getJobStatus(-1L), classOf[MissingArgumentException])
    ernie.getJobStatus(jobId)
  }

  @Test(dependsOnGroups = Array("setup"), groups = Array("main"))
  def getJobEntity() {
    testException(() => ernie.getJobEntity(-1L), classOf[MissingArgumentException])
    Assert.assertEquals(ernie.getJobEntity(jobId).getJobId, jobId)
  }

  @Test(dependsOnGroups = Array("setup"), groups = Array("main"))
  def getJobCatalog() {
    Assert.assertTrue(ernie.getJobCatalog().size > 0)
  }

  @Test(dependsOnGroups = Array("setup"), groups = Array("main"))
  def getJobList() {
    Assert.assertTrue(ernie.getJobList.size > 0)
  }

  @Test(dependsOnGroups = Array("setup"), groups = Array("main"))
  def completeJob() {
    val endTime = DateTime.now.plus(ernie.timeoutDuration.toMillis)
    var inProgress = true
    while ((DateTime.now.isBefore(endTime)) && inProgress) {
      if (getStatus == com.ksmpartners.ernie.model.JobStatus.COMPLETE) inProgress = false
    }
    Assert.assertFalse(inProgress)
  }

  @Test(dependsOnGroups = Array("main"), groups = Array("completed"))
  def getRptEnt() {
    testException(() => ernie.getReportEntity(-1L), classOf[MissingArgumentException])
    var resp = ernie.getReportEntity(jobId)
    Assert.assertEquals(resp.getRptId, com.ksmpartners.ernie.util.Utility.jobToRptId(jobId));
    resp = ernie.getReportEntity(jobToRptId(jobId))
    Assert.assertTrue(resp.getStartDate.isBeforeNow)
  }

  @Test(dependsOnGroups = Array("main"), groups = Array("completed"))
  def getOutputStream() {
    testException(() => ernie.getReportOutput(-1L), classOf[MissingArgumentException])
    val resp = ernie.getReportOutput(jobId)
    Assert.assertTrue(resp.available() > 0)
  }

  /*@Test(dependsOnGroups = Array("main"), groups = Array("completed"))
  def getOutputFile() {
    Assert.assertEquals(ernie.getReportOutputFile(-1L).error.get.getClass, classOf[MissingArgumentException])
    val resp = ernie.getReportOutputFile(jobId)
    Assert.assertTrue(resp.file.isDefined)
    Assert.assertTrue(resp.file.get.isFile)
    Assert.assertEquals(resp.file.get.getParent, ernie.outputDir)
  }

  @Test(dependsOnGroups = Array("main"), groups = Array("completed"))
  def getOutput() {
    Assert.assertEquals(ernie.getReportOutput(-1L).error.get.getClass, classOf[MissingArgumentException])
    val resp = ernie.getReportOutput(jobId)
    Assert.assertTrue(resp.stream.isDefined)
    Assert.assertTrue(resp.file.isDefined)
    Assert.assertTrue(resp.file.get.isFile)
    Assert.assertEquals(resp.file.get.getParent, ernie.outputDir)
  }      */

  @Test(dependsOnGroups = Array("completed"), groups = Array("aTCleanUp"))
  def deleteOutput() {
    val resp = ernie.deleteReportOutput(jobId)
    Assert.assertEquals(resp, DeleteStatus.SUCCESS)
  }

  @Test(dependsOnGroups = Array("completed"), groups = Array("aTCleanUp"))
  def deleteDef() {
    val resp = ernie.deleteDefinition(defId)
    Assert.assertEquals(resp, DeleteStatus.SUCCESS)
  }

  @Test(dependsOnGroups = Array("completed"), groups = Array("aTCleanUp"))
  def purge() {
    val resp = ernie.purgeExpiredReports()
    Assert.assertEquals(resp.deleteStatus, DeleteStatus.SUCCESS)
  }

}
