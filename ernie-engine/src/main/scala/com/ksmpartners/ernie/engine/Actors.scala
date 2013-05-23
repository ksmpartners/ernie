/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.engine

import actors.Actor
import collection._
import com.ksmpartners.ernie.model.{ ReportType, JobStatus }
import com.ksmpartners.ernie.engine.report._
import org.slf4j.LoggerFactory
import org.joda.time.DateTime

/**
 * Actor for coordinating report generation.
 */
class Coordinator(reportManager: ReportManager) extends Actor {
  this: ReportGeneratorFactory =>

  private val log = LoggerFactory.getLogger(classOf[Coordinator])

  private lazy val worker: Worker = new Worker(getReportGenerator(reportManager))
  private val jobIdToResultMap = new mutable.HashMap[Long, (JobStatus, Option[String] /* rptId */ )]()

  override def start(): Actor = {
    log.debug("in start()")
    super.start()
    worker.start()
    this
  }

  def act() {
    log.debug("in act()")
    loop {
      react {
        case req@ReportRequest(rptId, rptType, retentionOption) => {
          val jobId = generateJobId()
          jobIdToResultMap += (jobId -> (JobStatus.PENDING, None))
          val retentionDate = DateTime.now().plusDays(retentionOption getOrElse reportManager.getDefaultRetentionDays)
          if (retentionDate.isBefore(DateTime.now()) || retentionDate.isEqual(DateTime.now())) sender ! ReportResponse(jobId, JobStatus.FAILED_RETENTION_DATE_PAST, req)
          else if (retentionDate.isAfter(DateTime.now().plusDays(reportManager.getMaximumRetentionDays))) sender ! ReportResponse(jobId, JobStatus.FAILED_RETENTION_DATE_EXCEEDS_MAXIMUM, req)
          else {
            sender ! ReportResponse(jobId, JobStatus.IN_PROGRESS, req)
            worker ! JobRequest(rptId, rptType, jobId, retentionOption)
          }
        }
        case req@DeleteRequest(jobId) => {
          if (jobIdToResultMap.contains(jobId)) {
            if ((jobIdToResultMap.get(jobId).get._1 == JobStatus.COMPLETE) && (jobIdToResultMap.get(jobId).get._2.isDefined)) {
              // TODO: Consider wrapping in a try/catch to handle any exceptions that might get thrown.
              // Update DeleteResponse to contain the result of the deletion
              try {
                reportManager.deleteReport(jobIdToResultMap.get(jobId).get._2.get)
                jobIdToResultMap.update(jobId, (JobStatus.DELETED, Some(jobIdToResultMap.get(jobId).get._2.get)))
                sender ! DeleteResponse(jobIdToResultMap.get(jobId).get._1, req)
              } catch {
                case e: Exception => sender ! DeleteResponse(jobIdToResultMap.get(jobId).get._1, req)
              }
            } else sender ! DeleteResponse(jobIdToResultMap.get(jobId).get._1, req) // TODO: Send back "jobIdToResultMap.get(jobId).get._1" because the status could be PENDING or FAILED
          } else sender ! DeleteResponse(JobStatus.NO_SUCH_JOB, req) //no such job
        }
        case req@PurgeRequest() => {
          var purgedReports: List[String] = Nil

          jobIdToResultMap.foreach(f => if (f._2._2.isDefined) {
            val rptId = f._2._2.get
            if (reportManager.getReport(rptId).isDefined)
              if (reportManager.getReport(rptId).get.getRetentionDate.isBeforeNow) {
                purgedReports ::= rptId
                reportManager.deleteReport(rptId)
                jobIdToResultMap.update(f._1, (JobStatus.DELETED, Some(rptId)))
              }
          })

          sender ! PurgeResponse(purgedReports, req)
        }
        case req@StatusRequest(jobId) => {
          sender ! StatusResponse(jobIdToResultMap.getOrElse(jobId, (JobStatus.NO_SUCH_JOB, None))._1, req)
        }
        case req@ResultRequest(jobId) => {
          sender ! ResultResponse(jobIdToResultMap.getOrElse(jobId, (JobStatus.NO_SUCH_JOB, None))._2, req)
        }
        case req@JobsListRequest() => {
          val jobsList: Array[String] = jobIdToResultMap.keySet.map({ _.toString }).toArray
          sender ! JobsListResponse(jobsList, req)
        }
        case JobResponse(jobStatus, rptId, req) => {
          log.info("Got notify for jobId {} with status {}", req.jobId, jobStatus)
          jobIdToResultMap += (req.jobId -> (jobStatus, rptId))
        }
        case ShutDownRequest() => {
          worker !? ShutDownRequest()
          sender ! ShutDownResponse()
          exit()
        }
        case msg => log.info("Received unexpected message: {}", msg)
      }
    }
  }

  private var currJobId = System.currentTimeMillis

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
        case req@JobRequest(defId, rptType, jobId, retentionOption) => {
          sender ! JobResponse(JobStatus.IN_PROGRESS, None, req)
          var resultStatus = JobStatus.COMPLETE
          var rptId: Option[String] = None
          try {
            rptId = Some(runReport(defId, jobId, rptType, retentionOption))
          } catch {
            case ex: ReportManager.RetentionDateAfterMaximumException => {
              log.error("Caught exception while generating report: {}", ex.getMessage)
              resultStatus = JobStatus.FAILED
            }
            case ex: ReportManager.RetentionDateInThePastException => {
              log.error("Caught exception while generating report: {}", ex.getMessage)
              resultStatus = JobStatus.FAILED_RETENTION_DATE_PAST
            }
            case ex: Exception => {
              log.error("Caught exception while generating report: {}", ex.getMessage)
              resultStatus = JobStatus.FAILED_RETENTION_DATE_EXCEEDS_MAXIMUM
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

  private def runReport(defId: String, jobId: Long, rptType: ReportType, retentionOption: Option[Int]): String = {
    log.debug("Running report {} for jobId {}...", defId, jobId)
    val rptId = "REPORT_" + jobId
    rptGenerator.runReport(defId, rptId, rptType, retentionOption)
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
