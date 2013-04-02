/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.engine

import actors.Actor
import collection._
import com.ksmpartners.ernie.model.JobStatus
import com.ksmpartners.ernie.engine.report._
import org.slf4j.LoggerFactory
import java.io.File
import java.util

/**
 * Actor for coordinating report generation.
 */
class Coordinator(reportManager: ReportManager) extends Actor {

  private val log = LoggerFactory.getLogger(this.getClass)

  private lazy val worker: Worker = new Worker(reportManager)
  private val jobIdToResultMap = new mutable.HashMap[Long, (JobStatus, Option[String] /* fileName */ )]()

  override def start(): Actor = {
    log.debug("in start()")
    super.start()
    worker.start
    this
  }

  def act {
    log.debug("in act()")
    loop {
      react {
        case req@ReportRequest(rptId) => {
          val jobId = getJobId()
          jobIdToResultMap += (jobId -> (JobStatus.PENDING, None))
          sender ! ReportResponse(jobId, req)
          worker ! JobRequest(rptId, jobId)
        }
        case req@StatusRequest(jobId) => {
          sender ! StatusResponse(jobIdToResultMap.getOrElse(jobId, (JobStatus.NO_SUCH_JOB, None))._1, req)
        }
        case req@ResultRequest(jobId) => {
          sender ! ResultResponse(jobIdToResultMap.getOrElse(jobId, (JobStatus.NO_SUCH_JOB, None))._2, req)
        }
        case req@JobsMapRequest(uriPrefix) => {
          val jobStatusMap: util.Map[String, String] = new util.HashMap()
          jobIdToResultMap foreach { entry =>
            val jobIdString = entry._1.toString
            jobStatusMap.put(jobIdString, uriPrefix + "/" + jobIdString)
          }
          sender ! JobsMapResponse(jobStatusMap, req)
        }
        case req@ReportDefinitionMapRequest(uriPrefix) => {
          val response = (worker !? req).asInstanceOf[ReportDefinitionMapResponse]
          sender ! response
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

  private def getJobId(): Long = {
    currJobId += 1
    currJobId
  }

}

/**
 * Actor that is paired with a Coordinator, and executes report requests.
 */
class Worker(reportManager: ReportManager) extends Actor {

  private val log = LoggerFactory.getLogger(this.getClass)
  private lazy val rptGenerator = new ReportGenerator(reportManager)

  def act {
    log.debug("in act()")
    loop {
      react {
        case req@JobRequest(rptId, jobId) => {
          sender ! JobResponse(JobStatus.IN_PROGRESS, null, req)
          var resultStatus = JobStatus.COMPLETE
          var resultFileName: Option[String] = None
          try {
            resultFileName = Some(runPdfReport(rptId, jobId))
          } catch {
            case ex: Exception => {
              log.error("Caught exception while generating report: {}", ex.getMessage)
              resultStatus = JobStatus.FAILED
            }
          }
          sender ! JobResponse(resultStatus, resultFileName, req)
        }
        case req@ReportDefinitionMapRequest(uriPrefix) => {
          val rptDefMap: util.Map[String, String] = new util.HashMap()
          rptGenerator.getAvailableRptDefs map { rptDefId =>
            rptDefMap.put(rptDefId, uriPrefix + "/" + rptDefId)
          }
          sender ! ReportDefinitionMapResponse(rptDefMap, req)
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

  private def runPdfReport(rptId: String, jobId: Long): String = {
    log.debug("Running report {}...", rptId)
    val rptDefName = rptId
    val rptOutputName = "REPORT_" + jobId
    rptGenerator.runPdfReport(rptDefName, rptOutputName)
    log.debug("Done report {}...", rptId)
    rptOutputName
  }

  private def startRptGenerator() {
    rptGenerator.startup
  }

  private def stopRptGenerator() {
    rptGenerator.shutdown
  }

}
