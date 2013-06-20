/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 *
 */

package com.ksmpartners.ernie.api

import com.ksmpartners.ernie.util.TestLogger
import com.ksmpartners.ernie.api.service.{ RequiresReportManager, RequiresCoordinator, DefinitionDependencies }
import com.ksmpartners.ernie.util.Utility._
import org.testng.annotations._
import org.testng.Assert
import java.io.{ ByteArrayInputStream, File }
import com.ksmpartners.ernie.model.{ DefinitionEntity, DeleteStatus, ReportType }
import org.slf4j.{ LoggerFactory, Logger }

//@Test(dependsOnGroups = Array("timeout"))
class APITest { //extends TestLogger {

  @BeforeClass
  private var ernie: ErnieAPI = null
  private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.api.APITest")

  @BeforeClass(dependsOnGroups = Array("tTCleanUp"))
  def startup() {
  }

  @AfterClass
  def shutdown() {

  }

  @BeforeClass
  private var defId = ""

  @BeforeClass
  private var jobId = -1L

  @Test(dependsOnGroups = Array("timeout"))
  def init() {
    ernie = ErnieAPI(createTempDirectory.getAbsolutePath, createTempDirectory().getAbsolutePath, createTempDirectory.getAbsolutePath, 1000L * 300, 7, 14)
  }

  @Test(groups = Array("setup"), dependsOnMethods = Array("init"))
  def createDefinition() {
    Assert.assertEquals(ernie.createDefinition(Some(Left(null)), "test", "test").error.get.getClass, classOf[InvalidDefinitionException])
    Assert.assertEquals(ernie.createDefinition(Some(Right(null)), "test", "test").error.get.getClass, classOf[InvalidDefinitionException])
    Assert.assertEquals(ernie.createDefinition(None, null, null).error.get.getClass, classOf[IllegalArgumentException])
    val xml = scala.xml.XML.loadFile(new File(Thread.currentThread.getContextClassLoader.getResource("in/test_def.rptdesign").getPath))
    try_(new ByteArrayInputStream(xml.toString.getBytes)) { bAIS =>
      {
        val resp = ernie.createDefinition(Some(Left(bAIS)), "test", "test")
        log.info(resp.error + "")
        Assert.assertEquals(resp.error, None)
        defId = resp.defEnt.get.getDefId
      }
    }
    Assert.assertNotSame(defId, "")
  }

  @Test(dependsOnMethods = Array("createDefinition"), groups = Array("setup"))
  def updateDefinition() {
    Assert.assertEquals(ernie.updateDefinition(null, null, null).error.get.getClass, classOf[MissingArgumentException])
    Assert.assertEquals(ernie.updateDefinition(defId, null, null).error.get.getClass, classOf[MissingArgumentException])
    Assert.assertEquals(ernie.updateDefinition(defId, new ByteArrayInputStream(Array()), null).error.get.getClass, classOf[MissingArgumentException])
    Assert.assertEquals(ernie.updateDefinition(null, null).error.get.getClass, classOf[MissingArgumentException])
    val resp = ernie.updateDefinition(defId, Definition(Some(ernie.getDefinitionEntity(defId).defEnt.get), None, None))
    Assert.assertTrue(resp.error.isEmpty)
    try_(new ByteArrayInputStream(<break/>.toString.getBytes)) { bAIS =>
      {
        val resp = ernie.updateDefinition(defId, bAIS, ernie.getDefinition(defId))
        Assert.assertTrue(resp.error.isDefined)
        Assert.assertEquals(resp.error.get.getClass, classOf[InvalidDefinitionException])
      }
    }
    val xml = scala.xml.XML.loadFile(new File(Thread.currentThread.getContextClassLoader.getResource("in/test_def_params.rptdesign").getPath))
    try_(new ByteArrayInputStream(xml.toString.getBytes)) { bAIS =>
      {
        val resp = ernie.updateDefinition(defId, bAIS, ernie.getDefinition(defId))
        Assert.assertEquals(resp.error, None)
        Assert.assertTrue(resp.defEnt.get.getParams.size > 0)
      }
    }
  }

  @Test(dependsOnMethods = Array("updateDefinition"), groups = Array("setup"))
  def createJob() {
    var resp = ernie.createJob("test", ReportType.PDF, None, null, "test")
    Assert.assertTrue(resp.jobStatus.isDefined)
    Assert.assertEquals(resp.jobStatus.get, com.ksmpartners.ernie.model.JobStatus.FAILED_NO_SUCH_DEFINITION)

    resp = ernie.createJob(defId, ReportType.PDF, None, null, "test")
    jobId = resp.jobId
    Assert.assertTrue(jobId > 0)
    Assert.assertTrue(resp.error.isEmpty)
  }

  @Test(dependsOnGroups = Array("setup"), groups = Array("main"))
  def getDefinitionsCatalog() {
    Assert.assertTrue(ernie.getDefinitionsCatalog.catalog.size > 0)
  }

  @Test(dependsOnGroups = Array("setup"), groups = Array("main"))
  def getDefList() {
    Assert.assertTrue(ernie.getDefinitionList._1.size > 0)
  }

  @Test(dependsOnGroups = Array("setup"), groups = Array("main"))
  def getDefinition() {
    Assert.assertEquals(ernie.getDefinition(null).error.get.getClass, classOf[MissingArgumentException])
    Assert.assertTrue(ernie.getDefinition(defId).defEnt.isDefined)
  }

  @Test(dependsOnGroups = Array("setup"), groups = Array("main"))
  def getDefinitionEntity() {
    Assert.assertEquals(ernie.getDefinitionEntity(null).error.get.getClass, classOf[MissingArgumentException])
    Assert.assertTrue(ernie.getDefinitionEntity(defId).defEnt.isDefined)
  }

  @Test(dependsOnGroups = Array("setup"), groups = Array("main"))
  def getDefinitionDesign() {
    Assert.assertEquals(ernie.getDefinitionDesign(null).error.get.getClass, classOf[MissingArgumentException])
    Assert.assertTrue(ernie.getDefinitionDesign(defId).rptDesign.isDefined)
  }

  @Test(dependsOnGroups = Array("setup"), groups = Array("main"))
  def getStatus(): Option[com.ksmpartners.ernie.model.JobStatus] = {
    Assert.assertEquals(ernie.getJobStatus(-1L).error.get.getClass, classOf[MissingArgumentException])
    val resp = ernie.getJobStatus(jobId).jobStatus
    Assert.assertTrue(resp isDefined)
    resp
  }

  @Test(dependsOnGroups = Array("setup"), groups = Array("main"))
  def getJobEntity() {
    Assert.assertEquals(ernie.getJobEntity(-1L).error.get.getClass, classOf[MissingArgumentException])
    Assert.assertTrue(ernie.getJobEntity(jobId).jobEntity.isDefined)
  }

  @Test(dependsOnGroups = Array("setup"), groups = Array("main"))
  def getJobCatalog() {
    Assert.assertTrue(ernie.getJobCatalog(None)._1.size > 0)
  }

  @Test(dependsOnGroups = Array("setup"), groups = Array("main"))
  def getJobList() {
    Assert.assertTrue(ernie.getJobList._1.size > 0)
  }

  @Test(dependsOnGroups = Array("setup"), groups = Array("main"))
  def completeJob() {
    var ticks = 0
    var inProgress = true
    while ((ticks < ernie.timeout) && inProgress) {
      getStatus.map(f => if (f == com.ksmpartners.ernie.model.JobStatus.COMPLETE) inProgress = false)
      ticks += 1
    }
    Assert.assertFalse(inProgress)
  }

  @Test(dependsOnGroups = Array("main"), groups = Array("completed"))
  def getRptEnt() {
    Assert.assertEquals(ernie.getReportEntity(-1L).error.get.getClass, classOf[MissingArgumentException])
    var resp = ernie.getReportEntity(jobId).rptEntity
    Assert.assertTrue(resp.isDefined)
    resp = ernie.getReportEntity(jobToRptId(jobId)).rptEntity
    Assert.assertTrue(resp.isDefined)
    log.info("Look in " + (new File(ernie.outputDir)).getAbsolutePath)
    Assert.assertTrue(resp.get.getStartDate.isBeforeNow)
  }

  @Test(dependsOnGroups = Array("main"), groups = Array("completed"))
  def getOutputStream() {
    Assert.assertEquals(ernie.getReportOutputStream(-1L).error.get.getClass, classOf[MissingArgumentException])
    val resp = ernie.getReportOutputStream(jobId)
    Assert.assertTrue(resp.stream.isDefined)
  }

  @Test(dependsOnGroups = Array("main"), groups = Array("completed"))
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
  }

  @Test(dependsOnGroups = Array("completed"), groups = Array("aTCleanUp"))
  def deleteOutput() {
    val resp = ernie.deleteReportOutput(jobId)
    Assert.assertEquals(resp._1, DeleteStatus.SUCCESS)
  }

  @Test(dependsOnGroups = Array("completed"), groups = Array("aTCleanUp"))
  def deleteDef() {
    val resp = ernie.deleteDefinition(defId)
    Assert.assertEquals(resp._1, DeleteStatus.SUCCESS)
  }

  @Test(dependsOnGroups = Array("completed"), groups = Array("aTCleanUp"))
  def purge() {
    val resp = ernie.purgeExpiredReports()
    Assert.assertEquals(resp.deleteStatus, DeleteStatus.SUCCESS)
  }

}
