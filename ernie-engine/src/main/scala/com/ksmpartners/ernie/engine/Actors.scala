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

/**
 * Actor for coordinating report generation.
 */
class Coordinator(reportManager: ReportManager) extends Actor {

  private val log = LoggerFactory.getLogger(classOf[Coordinator])

  private lazy val worker: Worker = new Worker(reportManager)
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
        case req@ReportRequest(rptId, rptType) => {
          val jobId = generateJobId()
          jobIdToResultMap += (jobId -> (JobStatus.PENDING, None))
          sender ! ReportResponse(jobId, req)
          worker ! JobRequest(rptId, rptType, jobId)
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

  private var currJobId = System.currentTimeMillis()

  private def generateJobId(): Long = {
    currJobId += 1
    currJobId
  }

}

/**
 * Actor that is paired with a Coordinator, and executes report requests.
 */
class Worker(reportManager: ReportManager) extends Actor {

  private val log = LoggerFactory.getLogger(classOf[Worker])
  private lazy val rptGenerator = new ReportGenerator(reportManager)

  def act() {
    log.debug("in act()")
    loop {
      react {
        case req@JobRequest(defId, rptType, jobId) => {
          sender ! JobResponse(JobStatus.IN_PROGRESS, None, req)
          var resultStatus = JobStatus.COMPLETE
          var rptId: Option[String] = None
          try {
            rptId = Some(runReport(defId, jobId, rptType))
          } catch {
            case ex: Exception => {
              log.error("Caught exception while generating report: {}", ex.getMessage)
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

  private def runReport(defId: String, jobId: Long, rptType: ReportType): String = {
    log.debug("Running report {} for jobId {}...", defId, jobId)
    val rptId = "REPORT_" + jobId
    rptGenerator.runReport(defId, rptId, rptType)
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
