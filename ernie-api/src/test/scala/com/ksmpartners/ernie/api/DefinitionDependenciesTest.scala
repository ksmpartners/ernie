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

package com.ksmpartners.ernie.api.service

import com.ksmpartners.ernie.engine._
import com.ksmpartners.ernie.engine.report._
import com.ksmpartners.ernie.model
import com.ksmpartners.ernie.util.Utility._
import java.io._
import org.testng.annotations._
import org.testng.Assert
import org.slf4j.{ LoggerFactory, Logger }
import scala.Array

import com.ksmpartners.ernie.model.{ DefinitionEntity, DeleteStatus }
import akka.actor.{ ActorSystem, ActorRef, ActorDSL }
import scala.concurrent.duration._

@Test(dependsOnGroups = Array("timeout"))
class DefinitionDependenciesTest extends DefinitionDependencies with RequiresCoordinator with RequiresReportManager { //TestLogger

  private val tempInputDir = createTempDirectory
  private val tempOutputDir = createTempDirectory
  private val tempJobDir = createTempDirectory

  def timeoutDuration = (5 minutes)

  protected def workerCount: Int = 5

  protected val system: ActorSystem = ActorSystem("definition-dependencies-test")

  @BeforeClass
  def jobsDir = tempJobDir.getAbsolutePath

  @BeforeClass
  def outputDir = tempOutputDir.getAbsolutePath

  @BeforeClass
  def defDir = tempInputDir.getAbsolutePath

  @BeforeClass
  private var testDef = ""
  private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.api.DefinitionDependenciesTest")

  @BeforeClass
  val timeout = 300 * 1000L

  @BeforeClass(dependsOnGroups = Array("timeout"))
  def startup() {
  }

  @BeforeClass
  protected val reportManager = {
    val rm = new FileReportManager(tempInputDir.getAbsolutePath, tempOutputDir.getAbsolutePath)
    rm
  }

  @BeforeClass
  val coordinator: ActorRef = {
    val coord = ActorDSL.actor(system)(new Coordinator(Some(tempJobDir.getAbsolutePath), reportManager, Some(30 minutes), 5) with BirtReportGeneratorFactory)
    coord
  }

  @AfterTest
  def shutdown() {
    recDel(tempInputDir)
    recDel(tempOutputDir)
    recDel(tempJobDir)
  }

  @BeforeClass
  private var defId = ""

  @Test(dependsOnGroups = Array("timeout"))
  def createDefinition() {
    val defsRes = new DefsResource
    val resp = defsRes.putDefinition(None, None, Some({
      val dE = new DefinitionEntity
      dE.setCreatedUser("testUser")
      dE.setDefDescription("Test def    ")
      dE
    }))
    defId = resp.getDefId
    Assert.assertEquals(resp.getDefDescription, "Test def")
  }

  @Test(dependsOnMethods = Array("createDefinition"))
  def updateDefinition() {
    val defsRes = new DefsResource
    var res: Option[model.DefinitionEntity] = None
    val xml = scala.xml.XML.loadFile(new File(Thread.currentThread.getContextClassLoader.getResource("in/test_def_params.rptdesign").getPath))
    try_(new ByteArrayInputStream(xml.toString.getBytes)) { bAIS =>
      res = Some(defsRes.putDefinition(Some(defId), Some(bAIS), defsRes.getDefinitionEntity(defId)))
    }
    Assert.assertTrue(res.isDefined)
    Assert.assertTrue(res.get.getParams.size > 0)
  }

  @Test(groups = Array("getGroup"), dependsOnMethods = Array("updateDefinition"))
  def getCatalog() {
    val defsRes = new DefsResource
    Assert.assertTrue(defsRes.getCatalog().find(p => p.getDefDescription == "Test def").isDefined)
  }

  @Test(groups = Array("getGroup"), dependsOnMethods = Array("updateDefinition"))
  def getList() {
    val defsRes = new DefsResource
    Assert.assertTrue(defsRes.getList().size > 0)
  }

  @Test(groups = Array("ddTestFinish"), dependsOnGroups = Array("getGroup"))
  def get() {
    val defsRes = new DefsResource
    Assert.assertTrue(defsRes.getDefinitionEntity(defId).isDefined)
  }

  @Test(groups = Array("ddTestFinish"), dependsOnMethods = Array("get"))
  def delete() {
    val defRes = new DefsResource
    Assert.assertEquals(defRes.deleteDefinition(defId), DeleteStatus.SUCCESS)
  }

}
