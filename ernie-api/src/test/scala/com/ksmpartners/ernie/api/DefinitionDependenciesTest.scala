/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 *
 */

package com.ksmpartners.ernie.api.service

import com.ksmpartners.ernie.engine._
import com.ksmpartners.ernie.engine.report._
import com.ksmpartners.ernie.{ engine, model }
import com.ksmpartners.ernie.util.MapperUtility._
import com.ksmpartners.ernie.util.Utility._
import java.io._
import com.ksmpartners.ernie.api._
import org.testng.annotations._
import org.testng.Assert
import collection.mutable
import org.joda.time.DateTime
import org.slf4j.{ LoggerFactory, Logger }
import com.ksmpartners.common.annotations.tracematrix.{ TestSpec, TestSpecs }
import scala.Array

import com.ksmpartners.ernie.engine.PurgeResponse
import com.ksmpartners.ernie.engine.PurgeRequest
import com.ksmpartners.ernie.util.TestLogger
import com.ksmpartners.ernie.api.ErnieAPI
import com.ksmpartners.ernie.model.{ DefinitionEntity, DeleteStatus }
import org.apache.cxf.helpers.FileUtils
import akka.actor.{ ActorSystem, ActorRef, ActorDSL }
import akka.pattern.ask
import scala.concurrent.duration._

@Test(dependsOnGroups = Array("timeout"))
class DefinitionDependenciesTest extends DefinitionDependencies with RequiresCoordinator with RequiresReportManager { //TestLogger

  private val tempInputDir = createTempDirectory
  private val tempOutputDir = createTempDirectory
  private val tempJobDir = createTempDirectory

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
    defId = resp.defEnt.get.getDefId
    Assert.assertEquals(resp.defEnt.get.getDefDescription, "Test def")
  }

  @Test(dependsOnMethods = Array("createDefinition"))
  def updateDefinition() {
    val defsRes = new DefsResource
    var res: Option[com.ksmpartners.ernie.api.Definition] = None
    val xml = scala.xml.XML.loadFile(new File(Thread.currentThread.getContextClassLoader.getResource("in/test_def_params.rptdesign").getPath))
    try_(new ByteArrayInputStream(xml.toString.getBytes)) { bAIS =>
      res = Some(defsRes.putDefinition(Some(defId), Some(bAIS), defsRes.getDefinition(defId).defEnt))
    }
    Assert.assertTrue(res.isDefined)
    Assert.assertTrue(res.get.error.isEmpty)
    Assert.assertTrue(res.get.defEnt.isDefined)
    Assert.assertTrue(res.get.defEnt.get.getParams.size > 0)
  }

  @Test(groups = Array("getGroup"), dependsOnMethods = Array("updateDefinition"))
  def getCatalog() {
    val defsRes = new DefsResource
    Assert.assertTrue(defsRes.getCatalog().catalog.find(p => p.getDefDescription == "Test def").isDefined)
  }

  @Test(groups = Array("getGroup"), dependsOnMethods = Array("updateDefinition"))
  def getList() {
    val defsRes = new DefsResource
    Assert.assertTrue(defsRes.getList().size > 0)
  }

  @Test(groups = Array("ddTestFinish"), dependsOnGroups = Array("getGroup"))
  def get() {
    val defsRes = new DefsResource
    Assert.assertTrue(defsRes.getDefinition(defId).defEnt.isDefined)
  }

  @Test(groups = Array("ddTestFinish"), dependsOnMethods = Array("get"))
  def delete() {
    val defRes = new DefsResource
    Assert.assertEquals(defRes.deleteDefinition(defId), DeleteStatus.SUCCESS)
  }

}
