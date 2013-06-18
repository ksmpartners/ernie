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
import java.io.{ ByteArrayOutputStream, ByteArrayInputStream }
import scala.collection.immutable
import org.apache.commons.io.IOUtils
import com.ksmpartners.ernie.api._

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

  // def createDefinition(rptDesign: Option[Array[Byte]], description: String, createdUser: String): Definition = createDefinition(rptDesign.map(f => new ByteArrayInputStream(f)), description, createdUser)

  def createDefinition(rptDesign: Option[Either[ByteArrayInputStream, Array[Byte]]], description: String, createdUser: String): Definition =
    try {
      ServiceRegistry.defsResource.putDefinition(None, rptDesign.map(r => if (r.isLeft) r.left.get else new ByteArrayInputStream(r.right.get)), {
        val defEnt = new model.DefinitionEntity
        defEnt.setDefDescription(description)
        defEnt.setCreatedUser(createdUser)
        Some(defEnt)
      })
    } catch {
      case e: Exception => Definition(None, None, Some(e))
    }

  def getDefinitionsCatalog(): DefinitionCatalog =
    try {
      ServiceRegistry.defsResource.getCatalog()
    } catch {
      case e: Exception => DefinitionCatalog(Nil, Some(e))
    }

  def updateDefinition(defId: String, definition: Definition): Definition = {
    try {
      if (defId == null) throw new MissingArgumentException("Null definition ID")
      ServiceRegistry.defsResource.putDefinition(Some(defId), if (definition.rptDesign.isDefined)
        Some(new ByteArrayInputStream(definition.rptDesign.get))
      else None, definition.defEnt)
    } catch {
      case e: Exception => Definition(None, None, Some(e))
    }
  }

  def updateDefinition(defId: String, rptDesign: ByteArrayInputStream, definition: Definition): Definition = try {
    if (defId == null) throw new MissingArgumentException("Null definition ID")
    if (rptDesign == null) Definition(None, None, Some(new MissingArgumentException("Design input stream null")))
    else if (definition == null) Definition(None, None, Some(new MissingArgumentException("Definition null")))
    updateDefinition(defId, Definition(definition.defEnt, Some(IOUtils.toByteArray(rptDesign)), None))
  } catch {
    case e: Exception => Definition(None, None, Some(e))
  }

  def getDefinition(defId: String): Definition = try {
    if (defId == null) throw new MissingArgumentException("Null definition ID")
    ServiceRegistry.defsResource.getDefinition(defId)
  } catch {
    case e: Exception => Definition(None, None, Some(e))
  }

  def getDefinitionEntity(defId: String): Definition = try {
    if (defId == null) throw new MissingArgumentException("Null definition ID")
    ServiceRegistry.defsResource.getDefinitionEntity(defId)
  } catch {
    case e: Exception => Definition(None, None, Some(e))
  }

  def getDefinitionDesign(defId: String): Definition = try {
    if (defId == null) throw new MissingArgumentException("Null definition ID")
    ServiceRegistry.defsResource.getDefinitionDesign(defId)
  } catch {
    case e: Exception => Definition(None, None, Some(e))
  }

  def deleteDefinition(defId: String): (model.DeleteStatus, Option[Exception]) = try {
    if (defId == null) throw new MissingArgumentException("Null definition ID")
    (ServiceRegistry.defsResource.deleteDefinition(defId), None)
  } catch {
    case e: Exception => (model.DeleteStatus.FAILED, Some(e))
  }

  def createJob(defId: String, rptType: model.ReportType, retentionPeriod: Option[Int], reportParameters: immutable.Map[String, String], userName: String): JobStatus = try {
    ServiceRegistry.jobsResource.createJob(defId, rptType, retentionPeriod, reportParameters, userName)
  } catch {
    case e: Exception => JobStatus(-1L, Some(com.ksmpartners.ernie.model.JobStatus.FAILED), Some(e))
  }

  def getJobStatus(jobId: Long): JobStatus = try {
    if (jobId == null) throw new MissingArgumentException("Null job ID")
    ServiceRegistry.jobStatusResource.get(jobId)
  } catch {
    case e: Exception => JobStatus(jobId, None, Some(e))
  }

  def getJobCatalog(catalog: Option[model.JobCatalog]): (List[com.ksmpartners.ernie.model.JobEntity], Option[Exception]) = try {
    (ServiceRegistry.jobsResource.getCatalog(catalog), None)
  } catch {
    case e: Exception => (Nil, Some(e))
  }

  def getJobList(): (List[String], Option[Exception]) = try {
    (ServiceRegistry.jobsResource.getList, None)
  } catch {
    case e: Exception => (Nil, Some(e))
  }

  def getDefinitionList(): (List[String], Option[Exception]) = try {
    (ServiceRegistry.defsResource.getList, None)
  } catch {
    case e: Exception => (Nil, Some(e))
  }

  def getJobEntity(jobId: Long): JobEntity = try {
    if (jobId == null) throw new MissingArgumentException("Null job ID")
    ServiceRegistry.jobsResource.getJobEntity(jobId)
  } catch {
    case e: Exception => JobEntity(None, Some(e))
  }

  def getReportEntity(jobId: Long): ReportEntity = try {
    if (jobId == null) throw new MissingArgumentException("Null job ID")
    ServiceRegistry.jobResultsResource.getReportEntity(jobId)
  } catch {
    case e: Exception => ReportEntity(None, Some(e))
  }

  def getReportEntity(rptId: String): ReportEntity = try {
    if (rptId == null) throw new MissingArgumentException("Null report ID")
    ServiceRegistry.jobResultsResource.getReportEntity(com.ksmpartners.ernie.util.Utility.rptToJobId(rptId))
  } catch {
    case e: Exception => ReportEntity(None, Some(e))
  }

  def getReportOutputStream(jobId: Long): ReportOutput = try {
    if (jobId == null) throw new MissingArgumentException("Null job ID")
    ServiceRegistry.jobResultsResource.get(jobId, false, true)
  } catch {
    case e: Exception => ReportOutput(None, None, null, Some(e))
  }

  def getReportOutputFile(jobId: Long): ReportOutput = try {
    if (jobId == null) throw new MissingArgumentException("Null job ID")
    else if (!fileReportManager) ReportOutput(None, None, null, Some(new NothingToReturnException("Memory report manager does not provide files")))
    else {
      ServiceRegistry.jobResultsResource.get(jobId, true, false)
    }
  } catch {
    case e: Exception => ReportOutput(None, None, null, Some(e))
  }

  def getReportOutput(jobId: Long): ReportOutput = try {
    if (jobId == null) throw new MissingArgumentException("Null job ID")
    ServiceRegistry.jobResultsResource.get(jobId, fileReportManager, true)
  } catch {
    case e: Exception => ReportOutput(None, None, null, Some(e))
  }

  def deleteReportOutput(jobId: Long): (model.DeleteStatus, Option[Exception]) = try {
    if (jobId == null) throw new MissingArgumentException("Null job ID")
    (ServiceRegistry.jobResultsResource.del(jobId), None)
  } catch {
    case e: Exception => (model.DeleteStatus.FAILED, Some(e))
  }

  def purgeExpiredReports(): PurgeResult = try {
    ServiceRegistry.jobsResource.purge
  } catch {
    case e: Exception => PurgeResult(model.DeleteStatus.FAILED, Nil, Some(e))
  }

  protected def init() {
    ServiceRegistry.init(ErnieConfig(fileReportManager, jobsDir, defDir, outputDir, timeout, defaultRetentionDays, maxRetentionDays))
  }

}

object ErnieAPI {
  def apply(jobsDir: String, defDir: String, outputDir: String, timeout: Long, defaultRetentionDays: Int, maxRetentionDays: Int): ErnieAPI = {
    val api = new ErnieAPI
    api.jobsDir = jobsDir
    api.fileReportManager = true
    api.defDir = defDir
    api.outputDir = outputDir
    api.timeout = timeout
    api.defaultRetentionDays = defaultRetentionDays
    api.maxRetentionDays = maxRetentionDays
    api.init
    api
  }
  def apply(timeout: Long, defaultRetentionDays: Int, maxRetentionDays: Int): ErnieAPI = {
    val api = new ErnieAPI
    api.timeout = timeout
    api.defaultRetentionDays = defaultRetentionDays
    api.maxRetentionDays = maxRetentionDays
    api.fileReportManager = false
    api.init
    api
  }
}