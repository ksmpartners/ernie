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
 *
 *
 */

package com.ksmpartners.ernie.api

import scala.concurrent.duration._
import org.testng.annotations._
import org.testng.Assert
import org.slf4j.{ LoggerFactory, Logger }
import com.ksmpartners.ernie.engine._
import akka.actor.{ ActorSystem, ActorDSL }
import java.util.concurrent.TimeoutException
import com.ksmpartners.ernie.api.ErnieBuilder._

class TestCoordinator extends ErnieCoordinator {

  override def receive() = {
    case _ =>
  }

}

@Test(groups = Array("timeout"))
class TimeoutTest {

  private val ernie = {
    val e = ErnieEngine(ernieBuilder withMemoryReportManager () timeoutAfter (1 nanosecond) build ()).start
    e.setCoordinator(ActorDSL.actor(ActorSystem("timeout-test-system"))(new TestCoordinator))
    e
  }

  private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.api.APITest")

  @BeforeClass
  def startup() {
  }

  @AfterClass
  def shutdown() {

  }

  private var defId = ""
  private var jobId = -1L

  @Test
  def checkTimeout() {
    try {
      ernie.getJobStatus(5L)
      Assert.assertTrue(false)
    } catch {
      case t: Throwable => Assert.assertEquals(t.getClass, classOf[TimeoutException])
    }
  }

  /*

  @Test(groups = Array("tTSetup"))
  def createJob() {
    val resp = ernie.createJob("test", ReportType.PDF, None, null, "test")
    checkTimeout(resp.error)
  }

  @Test(dependsOnGroups = Array("tTSetup"), groups = Array("tTMain"))
  def getStatus(): Option[com.ksmpartners.ernie.model.JobStatus] = {
    checkTimeout(ernie.getJobStatus(1L).error)
    None
  }

  @Test(dependsOnGroups = Array("tTSetup"), groups = Array("tTMain"))
  def getJobEntity() {
    checkTimeout(ernie.getJobEntity(1L).error)
  }

  @Test(dependsOnGroups = Array("tTSetup"), groups = Array("tTMain"))
  def getJobCatalog() {
    checkTimeout(ernie.getJobCatalog(None)._2)
  }

  @Test(dependsOnGroups = Array("tTSetup"), groups = Array("tTMain"))
  def getJobList() {
    checkTimeout(ernie.getJobList._2)
  }

  @Test(dependsOnGroups = Array("tTMain"), groups = Array("tTCompleted"))
  def getRptEnt() {
    checkTimeout(ernie.getReportEntity(1L).error)
  }

  @Test(dependsOnGroups = Array("tTMain"), groups = Array("tTCompleted"))
  def getOutputStream() {
    checkTimeout(ernie.getReportOutput(1L).error)
  }

  @Test(dependsOnGroups = Array("tTMain"), groups = Array("tTCompleted"))
  def getOutput() {
    checkTimeout(ernie.getReportOutput(1L).error)
  }

  @Test(dependsOnGroups = Array("tTCompleted"), groups = Array("tTCleanUp"))
  def deleteOutput() {
    checkTimeout(ernie.deleteReportOutput(1L)._2)
  }

  @Test(dependsOnGroups = Array("tTCompleted"), groups = Array("tTCleanUp"))
  def deleteDef() {
    checkTimeout(ernie.deleteDefinition("test")._2)
  }

  @Test(dependsOnGroups = Array("tTCompleted"), groups = Array("tTCleanUp"))
  def purge() {
    checkTimeout(ernie.purgeExpiredReports().error)
  }        */

}
