/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.api

import com.ksmpartners.ernie.api.service._
import com.ksmpartners.ernie.model
import scala.xml.NodeSeq
import java.io.{ InputStream, Closeable, ByteArrayOutputStream, ByteArrayInputStream }
import scala.collection.immutable
import org.apache.commons.io.IOUtils
import com.ksmpartners.ernie.api._
import com.ksmpartners.ernie.util.Utility._
import java.util.concurrent.TimeoutException
import akka.pattern.AskTimeoutException

import scala.Some
import com.ksmpartners.ernie.api.ErnieBuilder._
import scala.concurrent.duration.FiniteDuration

/**
 * Object containing the
 */
protected[api] class ErnieControl extends ErnieDependencies {

  /*private var _fileReportManager_? = true
  def fileReportManager_=(value: Boolean): Unit = _fileReportManager_? = value
  def fileReportManager = _fileReportManager_?

  private var _jobsDir = ""
  def jobsDir_=(value: String): Unit = _jobsDir = value
  def jobsDir = _jobsDir

  private var _defDir = ""
  def defDir_=(value: String): Unit = _defDir = value
  def defDir = _defDir

  private var _outputDir = ""
  def outputDir_=(value: String): Unit = _outputDir = value
  def outputDir = _outputDir

  private var _timeout = 1000L
  def timeout_=(value: Long): Unit = _timeout = value
  def timeout = _timeout

  private var _defaultRetentionDays = 7
  def defaultRetentionDays_=(value: Int): Unit = _defaultRetentionDays = value
  def defaultRetentionDays = _defaultRetentionDays

  private var _maxRetentionDays = 14
  def maxRetentionDays_=(value: Int): Unit = _maxRetentionDays = value
  def maxRetentionDays = _maxRetentionDays

  private var _workerCount = 14
  def workerCount_=(value: Int): Unit = _workerCount = value
  def workerCount = _workerCount*/

  private def wrapper[B](func: () => B): B = try {
    func()
  } catch {
    case a: AskTimeoutException => {
      val t = new TimeoutException(a.getMessage)
      t.initCause(a.getCause())
      t.setStackTrace(a.getStackTrace)
      throw t
    }
    case t: Throwable => throw t
  }

  def createDefinition(rptDesign: Option[ByteArrayInputStream], description: String, createdUser: String): model.DefinitionEntity =
    wrapper(() => {
      defsResource.putDefinition(None,
        rptDesign,
        {
          val defEnt = new model.DefinitionEntity
          defEnt.setDefDescription(description)
          defEnt.setCreatedUser(createdUser)
          Some(defEnt)
        })
    })

  def getDefinitionsCatalog(): List[model.DefinitionEntity] =
    wrapper(() => defsResource.getCatalog())

  def updateDefinition(defId: String, defEnt: Option[model.DefinitionEntity], rptDesign: Option[ByteArrayInputStream]): model.DefinitionEntity =
    wrapper(() => {
      if (defId == null) throw new MissingArgumentException("Null definition ID")
      defsResource.putDefinition(Some(defId), rptDesign, defEnt)
    })

  /* def updateDefinition(defId: String, rptDesign: ByteArrayInputStream, definition: model.DefinitionEntity): Definition = try {
    if (defId == null) throw new MissingArgumentException("Null definition ID")
    if (rptDesign == null) throw new MissingArgumentException("Design input stream null")
    else if (definition == null) throw new MissingArgumentException("Definition null")
    updateDefinition(defId, Definition(definition.defEnt, Some(IOUtils.toByteArray(rptDesign)), None))
  } catch {
    case e: AskTimeoutException => Definition(None, None, Some(new TimeoutException(e.getMessage)))

    case e: TimeoutException => Definition(None, None, Some(e))
    case e: Exception => Definition(None, None, Some(e))
  }

  def getDefinition(defId: String): Definition = wrapper(() => {
    if (defId == null) throw new MissingArgumentException("Null definition ID")
    defsResource.getDefinition(defId)
  }) */

  def getDefinitionEntityOpt(defId: String): Option[model.DefinitionEntity] = wrapper(() => {
    if (defId == null) throw new MissingArgumentException("Null definition ID")
    defsResource.getDefinitionEntity(defId)
  })

  def getDefinitionEntity(defId: String): model.DefinitionEntity = getDefinitionEntityOpt(defId).getOrElse(throw new NotFoundException(defId + " not found"))

  def getDefinitionDesignOpt(defId: String)(b: (Option[InputStream]) => Unit): Unit = wrapper(() => {
    if (defId == null) throw new MissingArgumentException("Null definition ID")
    b(defsResource.getDefinitionDesign(defId))
  })

  def getDefinitionDesign(defId: String)(b: (InputStream) => Unit): Unit = {
    if (defId == null) throw new MissingArgumentException("Null definition ID")
    wrapper(() => defsResource.getDefinitionDesign(defId)).map(is => b(is)).getOrElse(throw new NotFoundException(defId + " design not found"))
  }

  def deleteDefinition(defId: String): model.DeleteStatus = wrapper(() => {
    if (defId == null) throw new MissingArgumentException("Null definition ID")
    defsResource.deleteDefinition(defId)
  })

  def createJob(defId: String, rptType: model.ReportType, retentionPeriod: Option[Int], reportParameters: immutable.Map[String, String], userName: String): (Long, model.JobStatus) = wrapper(() => {
    jobsResource.createJob(defId, rptType, retentionPeriod, reportParameters, userName)
  })

  def getJobStatus(jobId: Long): model.JobStatus = wrapper(() =>
    if (jobId <= 0) throw new MissingArgumentException("Null job ID")
    else jobStatusResource.get(jobId))

  def getJobCatalog(catalog: Option[model.JobCatalog]): List[model.JobEntity] = wrapper(() => jobCatalogResource.getCatalog(catalog))

  def getJobList(): List[String] = wrapper(() => jobsResource.getList)

  def getDefinitionList(): List[String] = wrapper(() => defsResource.getList)

  def getJobEntity(jobId: Long): Option[model.JobEntity] = wrapper(() =>
    if (jobId <= 0) throw new MissingArgumentException("Null job ID")
    else jobEntityResource.getJobEntity(jobId))

  def getReportEntity(jobId: Long): Option[model.ReportEntity] = wrapper(() =>
    if (jobId <= 0) throw new MissingArgumentException("Null job ID")
    else jobResultsResource.getReportEntity(jobId))

  def getReportEntity(rptId: String): Option[model.ReportEntity] = wrapper(() =>
    if (rptId == null) throw new MissingArgumentException("Null report ID")
    else jobResultsResource.getReportEntity(com.ksmpartners.ernie.util.Utility.rptToJobId(rptId)))

  def getReportOutput(jobId: Long): Option[InputStream] = wrapper(() =>
    if (jobId <= 0) throw new MissingArgumentException("Null job ID")
    else jobResultsResource.get(jobId, false, true))

  /*def getReportOutputFile(jobId: Long): ReportOutput = try {
    if (jobId <= 0) throw new MissingArgumentException("Null job ID")
    else if (!fileReportManager) ReportOutput(None, None, null, Some(new NothingToReturnException("Memory report manager does not provide files")))
    else {
      jobResultsResource.get(jobId, true, false)
    }
  } catch {
    case e: AskTimeoutException => ReportOutput(None, None, null, Some(new TimeoutException(e.getMessage)))

    case e: TimeoutException => ReportOutput(None, None, null, Some(e))
    case e: Exception => ReportOutput(None, None, null, Some(e))
  }

  def getReportOutput(jobId: Long): ReportOutput = try {
    if (jobId <= 0) throw new MissingArgumentException("Null job ID")
    jobResultsResource.get(jobId, fileReportManager, true)
  } catch {
    case e: AskTimeoutException => ReportOutput(None, None, null, Some(new TimeoutException(e.getMessage)))

    case e: TimeoutException => ReportOutput(None, None, null, Some(e))
    case e: Exception => ReportOutput(None, None, null, Some(e))
  } */

  def deleteReportOutput(jobId: Long): model.DeleteStatus = wrapper(() =>
    if (jobId <= 0) throw new MissingArgumentException("Null job ID")
    else jobResultsResource.del(jobId))

  def purgeExpiredReports(): (model.DeleteStatus, List[String]) = wrapper(() => jobCatalogResource.purge)

  def shutDown() = shutdownResource.shutdown

  /*protected def init(config:ErnieConfig):ErnieControl = {
    init(config)
    this
  }           */

}

class ErnieEngine private () {
  private val ec = new ErnieControl
  def start: ErnieControl = {
    ec.start()
    ec
  }
}

object ErnieEngine {
  def apply(config: ErnieConfiguration): ErnieEngine = {
    val e = new ErnieEngine()
    e.ec.configure(config)
    e
  }
}

/*object derp {
  import scala.concurrent.duration._
  import ErnieBuilder._

  val ec = ernieBuilder withFileReportManager("dir", "dir", "dir") timeoutAfter(10 seconds) withWorkers(50) build()
  val ee = ErnieEngine(ec)
  val ernie = ee.start


}         */

object ErnieBuilder {

  sealed abstract class ReportManager
  case class FileReportManager(jobDir: String, defDir: String, outputDir: String) extends ReportManager
  case class MemoryReportManager() extends ReportManager

  case class ErnieConfiguration(val reportManager: ReportManager, val timeout: Option[FiniteDuration], val defaultRetentionDays: Option[Int], val maxRetentionDays: Option[Int], val workerCount: Option[Int])

  abstract class BOOL
  abstract class TRUE extends BOOL
  abstract class FALSE extends BOOL

  class ConfigBuilder[HRM](val reportManager: Option[ReportManager], val timeout: Option[FiniteDuration], val defaultRetentionDays: Option[Int], val maxRetentionDays: Option[Int], val workerCount: Option[Int]) {

    // def withReportManager(rptMgr:ReportManager):ConfigBuilder[TRUE] = new ConfigBuilder[TRUE](Some(rptMgr), timeout, defaultRetentionDays, maxRetentionDays, workerCount)

    def withFileReportManager(jobDir: String, defDir: String, outputDir: String): ConfigBuilder[TRUE] = new ConfigBuilder[TRUE](Some(FileReportManager(jobDir, defDir, outputDir)),
      timeout, defaultRetentionDays, maxRetentionDays, workerCount)

    def withMemoryReportManager(): ConfigBuilder[TRUE] = new ConfigBuilder[TRUE](Some(MemoryReportManager()),
      timeout, defaultRetentionDays, maxRetentionDays, workerCount)

    def timeoutAfter(fD: FiniteDuration) =
      new ConfigBuilder[HRM](reportManager, Some(fD), defaultRetentionDays, maxRetentionDays, workerCount)

    def withDefaultRetentionDays(days: Int) =
      new ConfigBuilder[HRM](reportManager, timeout, Some(days), maxRetentionDays, workerCount)

    def withMaxRetentionDays(days: Int) =
      new ConfigBuilder[HRM](reportManager, timeout, defaultRetentionDays, Some(days), workerCount)

    def withWorkers(count: Int) =
      new ConfigBuilder[HRM](reportManager, timeout, defaultRetentionDays, maxRetentionDays, Some(count))
  }

  implicit def enableBuild(builder: ConfigBuilder[TRUE]) = new {
    def build() =
      new ErnieConfiguration(builder.reportManager.get, builder.timeout, builder.defaultRetentionDays, builder.maxRetentionDays, builder.workerCount)
  }

  def ernieBuilder = new ConfigBuilder[FALSE](None, None, None, None, None)
}

/*

class ErnieConfig private(fileMgr: Boolean = true, jobsDir: String = "", defDir: String = "", outputDir: String = "", timeout: Long = 5000L, defaultRetentionDays: Int = 7, maxRetentionDays: Int = 14, workerCount: Int = 50) {
 }

object ErnieConfig {
  def apply():ErnieConfig = new ErnieConfig()
  def fileManaged() = new ErnieConfig(true, jobsDir, defDir, outputDir, timeout, defaultRetentionDays, max RetentionDays, workerCount)
}


object ErnieEngine {
  def apply = new ErnieAPI
  def apply(jobsDir: String, defDir: String, outputDir: String, timeout: Long, defaultRetentionDays: Int, maxRetentionDays: Int, workerCount: Int = 5): ErnieAPI = {
    val api = new ErnieAPI
    api.jobsDir = jobsDir
    api.fileReportManager = true
    api.defDir = defDir
    api.outputDir = outputDir
    api.timeout = timeout
    api.defaultRetentionDays = defaultRetentionDays
    api.maxRetentionDays = maxRetentionDays
    api.workerCount = workerCount
    api.init
    api
  }
  def apply(timeout: Long, defaultRetentionDays: Int, maxRetentionDays: Int, workerCount: Int): ErnieAPI = {
    val api = new ErnieAPI
    api.timeout = timeout
    api.defaultRetentionDays = defaultRetentionDays
    api.maxRetentionDays = maxRetentionDays
    api.fileReportManager = false
    api.workerCount = workerCount
    api.init
    api
  }
}      */ 