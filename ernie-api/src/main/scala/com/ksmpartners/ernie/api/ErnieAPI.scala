/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.api

import com.ksmpartners.ernie.api.service.{ JobDependencies, ServiceRegistry }
import com.ksmpartners.ernie.model
import scala.xml.NodeSeq
import java.io.{ Closeable, ByteArrayOutputStream, ByteArrayInputStream }
import scala.collection.immutable
import org.apache.commons.io.IOUtils
import com.ksmpartners.ernie.api._
import com.ksmpartners.ernie.util.Utility._
import java.util.concurrent.TimeoutException
import akka.pattern.AskTimeoutException

/**
 * Object containing the
 */
class ErnieAPI {

  private var _fileReportManager_? = true
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
  def workerCount = _workerCount

  def createDefinition(rptDesign: Option[Either[ByteArrayInputStream, Array[Byte]]], description: String, createdUser: String): Definition =
    try {
      ServiceRegistry.defsResource.putDefinition(None,
        rptDesign.map(r => if (r.isLeft) r.left.get
        else fTry_(new ByteArrayInputStream(r.right.get)) { bAIS => bAIS }),
        {
          val defEnt = new model.DefinitionEntity
          defEnt.setDefDescription(description)
          defEnt.setCreatedUser(createdUser)
          Some(defEnt)
        })
    } catch {
      case e: TimeoutException => Definition(None, None, Some(e))
      case e: AskTimeoutException => Definition(None, None, Some(new TimeoutException(e.getMessage)))
      case e: NullPointerException => Definition(None, None, Some(InvalidDefinitionException("Design byte array null")))
      case e: Exception => Definition(None, None, Some(e))
    }

  def getDefinitionsCatalog(): DefinitionCatalog =
    try {
      ServiceRegistry.defsResource.getCatalog()
    } catch {
      case e: TimeoutException => DefinitionCatalog(Nil, Some(e))
      case e: AskTimeoutException => DefinitionCatalog(Nil, Some(new TimeoutException(e.getMessage)))
      case e: Exception => DefinitionCatalog(Nil, Some(e))
    }

  def updateDefinition(defId: String, definition: Definition): Definition = {
    try {
      if (defId == null) throw new MissingArgumentException("Null definition ID")
      if (definition.rptDesign.isDefined) fTry_(new ByteArrayInputStream((definition.rptDesign.get))) { bAIS =>
        ServiceRegistry.defsResource.putDefinition(Some(defId), Some(bAIS), definition.defEnt)
      }
      else ServiceRegistry.defsResource.putDefinition(Some(defId), None, definition.defEnt)
    } catch {
      case e: TimeoutException => Definition(None, None, Some(e))
      case e: AskTimeoutException => Definition(None, None, Some(new TimeoutException(e.getMessage)))
      case e: Exception => Definition(None, None, Some(e))
    }
  }

  def updateDefinition(defId: String, rptDesign: ByteArrayInputStream, definition: Definition): Definition = try {
    if (defId == null) throw new MissingArgumentException("Null definition ID")
    if (rptDesign == null) throw new MissingArgumentException("Design input stream null")
    else if (definition == null) throw new MissingArgumentException("Definition null")
    updateDefinition(defId, Definition(definition.defEnt, Some(IOUtils.toByteArray(rptDesign)), None))
  } catch {
    case e: TimeoutException => Definition(None, None, Some(e))
    case e: AskTimeoutException => Definition(None, None, Some(new TimeoutException(e.getMessage)))
    case e: Exception => Definition(None, None, Some(e))
  }

  def getDefinition(defId: String): Definition = try {
    if (defId == null) throw new MissingArgumentException("Null definition ID")
    ServiceRegistry.defsResource.getDefinition(defId)
  } catch {
    case e: TimeoutException => Definition(None, None, Some(e))
    case e: AskTimeoutException => Definition(None, None, Some(new TimeoutException(e.getMessage)))
    case e: Exception => Definition(None, None, Some(e))
  }

  def getDefinitionEntity(defId: String): Definition = try {
    if (defId == null) throw new MissingArgumentException("Null definition ID")
    ServiceRegistry.defsResource.getDefinitionEntity(defId)
  } catch {
    case e: TimeoutException => Definition(None, None, Some(e))
    case e: AskTimeoutException => Definition(None, None, Some(new TimeoutException(e.getMessage)))
    case e: Exception => Definition(None, None, Some(e))
  }

  def getDefinitionDesign(defId: String): Definition = try {
    if (defId == null) throw new MissingArgumentException("Null definition ID")
    ServiceRegistry.defsResource.getDefinitionDesign(defId)
  } catch {
    case e: TimeoutException => Definition(None, None, Some(e))
    case e: AskTimeoutException => Definition(None, None, Some(new TimeoutException(e.getMessage)))
    case e: Exception => Definition(None, None, Some(e))
  }

  def deleteDefinition(defId: String): (model.DeleteStatus, Option[Exception]) = try {
    if (defId == null) throw new MissingArgumentException("Null definition ID")
    (ServiceRegistry.defsResource.deleteDefinition(defId), None)
  } catch {
    case e: TimeoutException => (model.DeleteStatus.FAILED, Some(e))
    case e: AskTimeoutException => (model.DeleteStatus.FAILED, Some(new TimeoutException(e.getMessage)))
    case e: Exception => (model.DeleteStatus.FAILED, Some(e))
  }

  def createJob(defId: String, rptType: model.ReportType, retentionPeriod: Option[Int], reportParameters: immutable.Map[String, String], userName: String): JobStatus = try {
    ServiceRegistry.jobsResource.createJob(defId, rptType, retentionPeriod, reportParameters, userName)
  } catch {
    case e: TimeoutException => JobStatus(-1L, Some(com.ksmpartners.ernie.model.JobStatus.FAILED), Some(e))
    case e: AskTimeoutException => JobStatus(-1L, Some(com.ksmpartners.ernie.model.JobStatus.FAILED), Some(new TimeoutException(e.getMessage)))
    case e: Exception => JobStatus(-1L, Some(com.ksmpartners.ernie.model.JobStatus.FAILED), Some(e))
  }

  def getJobStatus(jobId: Long): JobStatus = try {
    if (jobId <= 0) throw new MissingArgumentException("Null job ID")
    ServiceRegistry.jobStatusResource.get(jobId)
  } catch {
    case e: TimeoutException => JobStatus(jobId, None, Some(e))
    case e: AskTimeoutException => JobStatus(jobId, None, Some(new TimeoutException(e.getMessage)))
    case e: Exception => JobStatus(jobId, None, Some(e))
  }

  def getJobCatalog(catalog: Option[model.JobCatalog]): (List[com.ksmpartners.ernie.model.JobEntity], Option[Exception]) = try {
    (ServiceRegistry.jobCatalogResource.getCatalog(catalog), None)
  } catch {
    case e: TimeoutException => (Nil, Some(e))
    case e: AskTimeoutException => (Nil, Some(new TimeoutException(e.getMessage)))
    case e: Exception => (Nil, Some(e))
  }

  def getJobList(): (List[String], Option[Exception]) = try {
    (ServiceRegistry.jobsResource.getList, None)
  } catch {
    case e: TimeoutException => (Nil, Some(e))
    case e: AskTimeoutException => (Nil, Some(new TimeoutException(e.getMessage)))
    case e: Exception => (Nil, Some(e))
  }

  def getDefinitionList(): (List[String], Option[Exception]) = try {
    (ServiceRegistry.defsResource.getList, None)
  } catch {
    case e: TimeoutException => (Nil, Some(e))
    case e: AskTimeoutException => (Nil, Some(new TimeoutException(e.getMessage)))
    case e: Exception => (Nil, Some(e))
  }

  def getJobEntity(jobId: Long): JobEntity = try {
    if (jobId <= 0) throw new MissingArgumentException("Null job ID")
    ServiceRegistry.jobEntityResource.getJobEntity(jobId)
  } catch {
    case e: TimeoutException => JobEntity(None, Some(e))
    case e: AskTimeoutException => JobEntity(None, Some(new TimeoutException(e.getMessage)))
    case e: Exception => JobEntity(None, Some(e))
  }

  def getReportEntity(jobId: Long): ReportEntity = try {
    if (jobId <= 0) throw new MissingArgumentException("Null job ID")
    ServiceRegistry.jobResultsResource.getReportEntity(jobId)
  } catch {
    case e: TimeoutException => ReportEntity(None, Some(e))
    case e: AskTimeoutException => ReportEntity(None, Some(new TimeoutException(e.getMessage)))
    case e: Exception => ReportEntity(None, Some(e))
  }

  def getReportEntity(rptId: String): ReportEntity = try {
    if (rptId == null) throw new MissingArgumentException("Null report ID")
    ServiceRegistry.jobResultsResource.getReportEntity(com.ksmpartners.ernie.util.Utility.rptToJobId(rptId))
  } catch {
    case e: TimeoutException => ReportEntity(None, Some(e))
    case e: AskTimeoutException => ReportEntity(None, Some(new TimeoutException(e.getMessage)))
    case e: Exception => ReportEntity(None, Some(e))
  }

  def getReportOutputStream(jobId: Long): ReportOutput = try {
    if (jobId <= 0) throw new MissingArgumentException("Null job ID")
    ServiceRegistry.jobResultsResource.get(jobId, false, true)
  } catch {
    case e: TimeoutException => ReportOutput(None, None, null, Some(e))
    case e: AskTimeoutException => ReportOutput(None, None, null, Some(new TimeoutException(e.getMessage)))
    case e: Exception => ReportOutput(None, None, null, Some(e))
  }

  def getReportOutputFile(jobId: Long): ReportOutput = try {
    if (jobId <= 0) throw new MissingArgumentException("Null job ID")
    else if (!fileReportManager) ReportOutput(None, None, null, Some(new NothingToReturnException("Memory report manager does not provide files")))
    else {
      ServiceRegistry.jobResultsResource.get(jobId, true, false)
    }
  } catch {
    case e: TimeoutException => ReportOutput(None, None, null, Some(e))
    case e: AskTimeoutException => ReportOutput(None, None, null, Some(new TimeoutException(e.getMessage)))
    case e: Exception => ReportOutput(None, None, null, Some(e))
  }

  def getReportOutput(jobId: Long): ReportOutput = try {
    if (jobId <= 0) throw new MissingArgumentException("Null job ID")
    ServiceRegistry.jobResultsResource.get(jobId, fileReportManager, true)
  } catch {
    case e: TimeoutException => ReportOutput(None, None, null, Some(e))
    case e: AskTimeoutException => ReportOutput(None, None, null, Some(new TimeoutException(e.getMessage)))
    case e: Exception => ReportOutput(None, None, null, Some(e))
  }

  def deleteReportOutput(jobId: Long): (model.DeleteStatus, Option[Exception]) = try {
    if (jobId <= 0) throw new MissingArgumentException("Null job ID")
    (ServiceRegistry.jobResultsResource.del(jobId), None)
  } catch {
    case e: TimeoutException => (model.DeleteStatus.FAILED, Some(e))
    case e: AskTimeoutException => (model.DeleteStatus.FAILED, Some(new TimeoutException(e.getMessage)))
    case e: Exception => (model.DeleteStatus.FAILED, Some(e))
  }

  def purgeExpiredReports(): PurgeResult = try {
    ServiceRegistry.jobCatalogResource.purge
  } catch {
    case e: TimeoutException => PurgeResult(model.DeleteStatus.FAILED, Nil, Some(e))
    case e: AskTimeoutException => PurgeResult(model.DeleteStatus.FAILED, Nil, Some(new TimeoutException(e.getMessage)))
    case e: Exception => PurgeResult(model.DeleteStatus.FAILED, Nil, Some(e))
  }

  def shutDown() = ServiceRegistry.shutdownResource.shutdown

  protected def init() {
    ServiceRegistry.init(ErnieConfig(fileReportManager, jobsDir, defDir, outputDir, timeout, defaultRetentionDays, maxRetentionDays, workerCount))
  }

  def spawnWorker() {
    ServiceRegistry.spawnWorker()
  }

}

object ErnieAPI {
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
}