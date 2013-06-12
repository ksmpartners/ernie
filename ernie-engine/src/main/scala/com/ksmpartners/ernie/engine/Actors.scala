/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.engine

import actors.Actor
import collection._
import com.ksmpartners.ernie.model._
import com.ksmpartners.ernie.engine.report._
import org.slf4j.LoggerFactory
import org.joda.time.{ Days, DateTime }
import com.ksmpartners.ernie.util.Utility._
import org.eclipse.birt.report.engine.api.UnsupportedFormatException
import com.ksmpartners.ernie.util.MapperUtility._
import scala.Some
import java.io.{ File, FileOutputStream }

/**
 * Actor for coordinating report generation.
 */
class Coordinator(pathToJobEntities: String, reportManager: ReportManager) extends Actor {
  this: ReportGeneratorFactory =>

  private val log = LoggerFactory.getLogger(classOf[Coordinator])

  private lazy val worker: Worker = new Worker(getReportGenerator(reportManager))
  private val jobIdToResultMap = new mutable.HashMap[Long, JobEntity]() /* rptId */
  private var timeout: Long = 1000L
  private var noRestartingJobs = true
  override def start(): Actor = {
    log.debug("in start()")
    super.start()
    worker.start()
    val files = (new java.io.File(pathToJobEntities)).listFiles()
    if (files != null)
      files.filter({ _.isFile }).filter({ _.getName.endsWith("entity") }).foreach({ file =>
        try {
          val jobEnt = mapper.readValue(file, classOf[JobEntity])
          val jobId = file.getName.replaceFirst("[.][^.]+$", "").toLong
          jobIdToResultMap += (jobId -> jobEnt)
          if (((jobEnt.getJobStatus == JobStatus.IN_PROGRESS) || (jobEnt.getJobStatus == JobStatus.PENDING)) && (jobEnt.getRptEntity != null)) {
            jobEnt.setJobStatus(JobStatus.RESTARTING)
            noRestartingJobs = false
            updateJob(jobId, jobEnt)
          }
        } catch {
          case e: Exception => log.error("Caught exception while loading job entities: {}", e.getMessage)
        }
      })
    this
  }
  private def handleRestartingJobs() = if (!noRestartingJobs) {
    log.info("Checking for restarting jobs...")
    val restJobs = jobIdToResultMap.filter(p => p._2.getJobStatus == JobStatus.RESTARTING)
    if (restJobs.isEmpty) noRestartingJobs = true
    else restJobs.foreach(f => {
      val jobEnt = f._2
      val jobId = f._1
      import JavaConversions._
      worker ! JobRequest(jobEnt.getRptEntity.getSourceDefId, jobEnt.getRptEntity.getReportType, jobId, Some(Days.daysBetween(DateTime.now, jobEnt.getRptEntity.getRetentionDate).getDays),
        if (jobEnt.getRptEntity.getParams != null) immutable.Map(jobEnt.getRptEntity.getParams.toList: _*) else immutable.Map.empty[String, String], jobEnt.getRptEntity.getCreatedUser)
    })
  }
  private def checkExpired(jobId: Long) {
    reportManager.getReport(jobToRptId(jobId)).map(f => {
      val jobEnt = jobIdToResultMap.get(jobId).get
      if (f.getRetentionDate.isBeforeNow && (jobEnt.getJobStatus == JobStatus.COMPLETE)) {
        updateJob(jobId, {
          jobEnt.setJobStatus(JobStatus.EXPIRED)
          jobEnt
        })
      }
    })
  }
  def act() {
    log.debug("in act()")
    loop {
      handleRestartingJobs()
      react {
        case req@ReportRequest(defId, rptType, retentionOption, reportParameters, userName) => {
          val jobId = generateJobId()
          if (reportManager.getDefinition(defId).isDefined) {
            val rptEntity = new ReportEntity()
            rptEntity.setSourceDefId(defId)
            rptEntity.setReportType(rptType)
            rptEntity.setRetentionDate(DateTime.now.plusDays(retentionOption.getOrElse(reportManager.getDefaultRetentionDays)))
            rptEntity.setParams(JavaConversions.asJavaMap(reportParameters))
            rptEntity.setCreatedDate(DateTime.now)
            rptEntity.setCreatedUser(userName)
            updateJob(jobId, new JobEntity(jobId, JobStatus.IN_PROGRESS, DateTime.now, null, rptEntity))
            reportManager.getDefinition(defId).map(m => if ((m.getEntity.getUnsupportedReportTypes != null) && m.getEntity.getUnsupportedReportTypes.contains(rptType)) {
              try {
                updateJob(jobId, jobIdToResultMap.get(jobId).map(je => { je.setJobStatus(JobStatus.FAILED_UNSUPPORTED_FORMAT); je }).get)
              } catch {
                case e: NoSuchElementException => {
                  log.error("Caught exception while running report: {}", e.getMessage)
                }
              }
              sender ! ReportResponse(jobId, JobStatus.FAILED_UNSUPPORTED_FORMAT, req)
            } else {
              val retentionDate = DateTime.now().plusDays(retentionOption getOrElse reportManager.getDefaultRetentionDays)
              if (retentionDate.isBefore(DateTime.now()) || retentionDate.isEqual(DateTime.now())) sender ! ReportResponse(jobId, JobStatus.FAILED_RETENTION_DATE_PAST, req)
              else if (retentionDate.isAfter(DateTime.now().plusDays(reportManager.getMaximumRetentionDays))) sender ! ReportResponse(jobId, JobStatus.FAILED_RETENTION_DATE_EXCEEDS_MAXIMUM, req)
              else {
                sender ! ReportResponse(jobId, JobStatus.IN_PROGRESS, req)
                worker ! JobRequest(defId, rptType, jobId, retentionOption, reportParameters, rptEntity.getCreatedUser)
              }
            })
          } else {
            updateJob(jobId, new JobEntity(jobId, JobStatus.FAILED_NO_SUCH_DEFINITION, DateTime.now, null, null))
            sender ! ReportResponse(jobId, JobStatus.FAILED_NO_SUCH_DEFINITION, req)
          }
        }
        case req@DeleteRequest(jobId) => {
          if (jobIdToResultMap.contains(jobId)) {
            if ((jobIdToResultMap.get(jobId).map(je => je.getJobStatus).getOrElse(null) == JobStatus.COMPLETE) &&
              (jobIdToResultMap.get(jobId).map(je => je.getRptId).map(f => f != "").getOrElse(false))) {
              try {
                reportManager.deleteReport(jobIdToResultMap.get(jobId).map(je => je.getRptId).get)
                updateJob(jobId, jobIdToResultMap.get(jobId).map(je => { je.setJobStatus(JobStatus.DELETED); je }).get)
                sender ! DeleteResponse(jobIdToResultMap.get(jobId).map(je => je.getJobStatus match {
                  case JobStatus.DELETED => DeleteStatus.SUCCESS
                  case _ => DeleteStatus.FAILED
                }).get, req)
              } catch {
                case e: Exception => sender ! DeleteResponse(jobIdToResultMap.get(jobId).map(je => je.getJobStatus match {
                  case JobStatus.DELETED => DeleteStatus.SUCCESS
                  case _ => DeleteStatus.FAILED
                }).getOrElse(DeleteStatus.FAILED), req)
              }
            } else sender ! DeleteResponse(jobIdToResultMap.get(jobId).map(je => je.getJobStatus match {
              case JobStatus.DELETED => DeleteStatus.SUCCESS
              case _ => DeleteStatus.FAILED
            }).getOrElse(DeleteStatus.FAILED), req) // TODO: Send back "jobIdToResultMap.get(jobId).get._1" because the status could be PENDING or FAILED
          } else sender ! DeleteResponse(DeleteStatus.NOT_FOUND, req) //no such job
        } case req@DeleteDefinitionRequest(defId) => {
          if (jobIdToResultMap.find(p => {
            val defIdOpt = if (p._2.getRptEntity != null) Some(p._2.getRptEntity.getSourceDefId)
            else reportManager.getReport(p._2.getRptId).map(r => r.getSourceDefId)
            if (defIdOpt.isDefined) (defIdOpt.get == defId) && ((p._2.getJobStatus == JobStatus.IN_PROGRESS) || (p._2.getJobStatus == JobStatus.PENDING))
            else false
          }).isDefined) {
            sender ! DeleteDefinitionResponse(DeleteStatus.FAILED_IN_USE, req)
          } else try {
            reportManager.deleteDefinition(defId)
            sender ! DeleteDefinitionResponse(DeleteStatus.SUCCESS, req)
          } catch { case _ => sender ! DeleteDefinitionResponse(DeleteStatus.FAILED, req) }
        }
        case req@PurgeRequest() => {
          var purgedReports: List[String] = Nil
          var deleteStatus = DeleteStatus.SUCCESS
          jobIdToResultMap.foreach(f => if ((f._2 != null)) {
            val rptOpt = reportManager.getReport(jobToRptId(f._1))
            if (((f._2.getJobStatus == JobStatus.COMPLETE) || (f._2.getJobStatus == JobStatus.EXPIRED)) && (rptOpt isDefined)) {
              val rptId = rptOpt.get.getRptId

              if (reportManager.getReport(rptId).isDefined) {
                if (reportManager.getReport(rptId).get.getRetentionDate.isBeforeNow) {
                  purgedReports ::= rptId
                  try {
                    reportManager.deleteReport(rptId)
                    updateJob(f._1, jobIdToResultMap.get(f._1).map(je => { je.setJobStatus(JobStatus.DELETED); je }).get)
                  } catch {
                    case e: NoSuchElementException => {
                      log.error("Caught exception while purging reports: {}", e.getMessage)
                      deleteStatus = DeleteStatus.FAILED
                    }
                    case e: Exception => {
                      log.error("Caught exception while purging reports: {}", e.getMessage)
                      deleteStatus = DeleteStatus.FAILED
                    }
                  }
                }
              }
            }
          })
          sender ! PurgeResponse(deleteStatus, purgedReports, req)
        }
        case req@StatusRequest(jobId) => {
          checkExpired(jobId)
          sender ! StatusResponse(jobIdToResultMap.get(jobId).map(je => je.getJobStatus) getOrElse (JobStatus.NO_SUCH_JOB), req)
        }
        case req@ReportDetailRequest(jobId) => {
          sender ! ReportDetailResponse(jobIdToResultMap.get(jobId).map(je => reportManager.getReport(jobToRptId(je.getJobId)).map(f => f.getEntity)) getOrElse None, req)
        }
        case req@JobDetailRequest(jobId) => {
          sender ! JobDetailResponse(jobIdToResultMap.get(jobId), req)
        }
        case req@ResultRequest(jobId) => {
          val rptId = jobIdToResultMap.get(jobId).map(je => je.getRptId) orElse (None)
          sender ! ResultResponse(rptId match {
            case Some("") => None
            case Some(null) => None
            case None => None
            case Some(string) => Some(string)
            case _ => None
          }, req)
        }
        case req@JobsListRequest() => {
          val jobsList: Array[String] = jobIdToResultMap.keySet.map({ _.toString }).toArray
          sender ! JobsListResponse(jobsList, req)
        }
        case req@JobsCatalogRequest(jobCatalog) => {
          val jobsList: List[JobEntity] = if (jobCatalog.isDefined) jobCatalog.getOrElse(null) match {
            case JobCatalog.FAILED => jobIdToResultMap.filter(f => f._2.getJobStatus match {
              case JobStatus.FAILED => true
              case JobStatus.FAILED_INVALID_PARAMETER_VALUES => true
              case JobStatus.FAILED_NO_SUCH_DEFINITION => true
              case JobStatus.FAILED_PARAMETER_NULL => true
              case JobStatus.FAILED_RETENTION_DATE_EXCEEDS_MAXIMUM => true
              case JobStatus.FAILED_RETENTION_DATE_PAST => true
              case JobStatus.FAILED_UNSUPPORTED_FORMAT => true
              case JobStatus.FAILED_UNSUPPORTED_PARAMETER_TYPE => true
              case _ => false
            }).map(f => f._2) toList
            case JobCatalog.COMPLETE => jobIdToResultMap.filter(f => f._2.getJobStatus == JobStatus.COMPLETE).map(f => f._2) toList
            case JobCatalog.DELETED => jobIdToResultMap.filter(f => f._2.getJobStatus == JobStatus.DELETED).map(f => f._2) toList
            case JobCatalog.IN_PROGRESS => jobIdToResultMap.filter(f => f._2.getJobStatus == JobStatus.IN_PROGRESS).map(f => f._2) toList
            case JobCatalog.EXPIRED => jobIdToResultMap.filter(f => {
              val rptOpt = reportManager.getReport(f._2.getRptId)
              rptOpt.map(rpt => DateTime.now.isAfter(rpt.getRetentionDate)).getOrElse(false)
            }).map(f => f._2) toList
            case _ => jobIdToResultMap.map(f => f._2).toList
          }
          else jobIdToResultMap.map(f => f._2).toList
          sender ! JobsCatalogResponse(jobsList, req)
        }
        case JobResponse(jobStatus, rptId, req) => {
          log.info("Got notify for jobId {} with status {}", req.jobId, jobStatus)
          try {
            updateJob(req.jobId, jobIdToResultMap.get(req.jobId).map(je => { je.setJobStatus(jobStatus); je.setRptId(rptId.get); je.setRptEntity(null); je }).get)
          } catch {
            case e: NoSuchElementException => {
              log.error("Caught exception while running report: {}", e.getMessage)
            }
          }
          if (jobStatus == JobStatus.FAILED_UNSUPPORTED_FORMAT) reportManager.getDefinition(rptId getOrElse "").map(defn =>
            {
              val entity = defn.getEntity
              val unsupportedRptTypes = entity.getUnsupportedReportTypes
              unsupportedRptTypes.add(req.rptType)
              entity.setUnsupportedReportTypes(unsupportedRptTypes)
              reportManager.updateDefinitionEntity(rptId.getOrElse(""), entity)
            })
        }
        case ShutDownRequest() => {
          worker !? (timeout, ShutDownRequest())
          sender ! ShutDownResponse()
          exit()
        }
        case msg => log.info("Received unexpected message: {}", msg)
      }

    }
  }

  private def updateJob(jobId: Long, jobEnt: JobEntity) {
    jobIdToResultMap += (jobId -> jobEnt)
    val jobEntFile = new File(pathToJobEntities, jobId + ".entity")
    jobEntFile.delete
    jobEntFile.createNewFile
    try_(new FileOutputStream(jobEntFile, false)) { fos =>
      mapper.writeValue(fos, jobEnt)
    }
  }

  private var currJobId = System.currentTimeMillis

  def setTimeout(t: Long) {
    timeout = t
  }

  private def generateJobId(): Long = {
    currJobId += 1
    currJobId
  }

}

/**
 * Actor that is paired with a Coordinator, and executes report requests.
 */
class Worker(rptGenerator: ReportGenerator) extends Actor {

  private val log = LoggerFactory.getLogger(classOf[Worker])

  def act() {
    log.debug("in act()")
    loop {
      react {
        case req@JobRequest(defId, rptType, jobId, retentionOption, reportParameters, userName) => {
          sender ! JobResponse(JobStatus.IN_PROGRESS, None, req)
          var resultStatus = JobStatus.COMPLETE
          var rptId: Option[String] = None
          try {
            rptId = Some(runReport(defId, jobId, rptType, retentionOption, reportParameters, userName))
          } catch {
            case ex: ParameterNullException => {
              log.error("Caught exception while generating report: {}", ex.getMessage)
              resultStatus = JobStatus.FAILED_PARAMETER_NULL
            }
            case ex: InvalidParameterValuesException => {
              log.error("Caught exception while generating report: {}", ex.getMessage)
              resultStatus = JobStatus.FAILED_INVALID_PARAMETER_VALUES
            }
            case ex: UnsupportedDataTypeException => {
              log.error("Caught exception while generating report: {}", ex.getMessage)
              resultStatus = JobStatus.FAILED_UNSUPPORTED_PARAMETER_TYPE
            }
            case ex: ReportManager.RetentionDateAfterMaximumException => {
              log.error("Caught exception while generating report: {}", ex.getMessage)
              resultStatus = JobStatus.FAILED_RETENTION_DATE_EXCEEDS_MAXIMUM
            }
            case ex: ReportManager.RetentionDateInThePastException => {
              log.error("Caught exception while generating report: {}", ex.getMessage)
              resultStatus = JobStatus.FAILED_RETENTION_DATE_PAST
            }
            case ex: UnsupportedFormatException => {
              log.error("Caught exception while generating report: {}", ex.getMessage)
              resultStatus = JobStatus.FAILED_UNSUPPORTED_FORMAT
            }
            case ex: ClassCastException => {
              log.error("Caught exception while generating report: {}", ex.getMessage)
              resultStatus = JobStatus.FAILED_INVALID_PARAMETER_VALUES
            }
            case ex: Exception => {
              log.error("Caught exception while generating report: {}", ex.getMessage)
              log.error(ex.getStackTraceString)
              resultStatus = JobStatus.FAILED
            }
          }
          sender ! JobResponse(resultStatus, rptId, req)
        }
        case ShutDownRequest() => {
          stopRptGenerator()
          sender ! ShutDownResponse()
          exit()
        }
        case msg => log.info("Received unexpected message: {}", msg)
      }
    }
  }

  override def start(): Actor = {
    log.debug("in start()")
    super.start()
    startRptGenerator()
    this
  }

  private def runReport(defId: String, jobId: Long, rptType: ReportType, retentionOption: Option[Int], reportParameters: immutable.Map[String, String], userName: String): String = {
    log.debug("Running report {} for jobId {}...", defId, jobId)
    val rptId = jobToRptId(jobId)
    rptGenerator.runReport(defId, rptId, rptType, retentionOption, reportParameters, userName)
    log.debug("Done running report {} for jobId {}...", defId, jobId)
    rptId
  }

  private def startRptGenerator() {
    rptGenerator.startup()
  }

  private def stopRptGenerator() {
    rptGenerator.shutdown()
  }

}
