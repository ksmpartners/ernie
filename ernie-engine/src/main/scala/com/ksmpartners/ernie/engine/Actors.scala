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
import org.slf4j.LoggerFactory
import java.io.File
import java.util

/**
 * Actor for coordinating report generation.
 */
class Coordinator(pathToRptDefs: String, pathToOutputs: String) extends Actor {

  private val log = LoggerFactory.getLogger(this.getClass)

  private lazy val worker: Worker = new Worker(pathToRptDefs, pathToOutputs)
  private val jobIdToResultMap = new mutable.HashMap[Long, (JobStatus, Option[String] /* filePath */ )]()

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
          jobIdToResultMap += (jobId -> (JobStatus.PENDING, null))
          sender ! ReportResponse(jobId, req)
          worker ! JobRequest(rptId, jobId)
        }
        case req@StatusRequest(jobId) => {
          sender ! StatusResponse(jobIdToResultMap.getOrElse(jobId, (JobStatus.NO_SUCH_JOB, null))._1, req)
        }
        case req@ResultRequest(jobId) => {
          sender ! ResultResponse(jobIdToResultMap.getOrElse(jobId, (JobStatus.NO_SUCH_JOB, None))._2, req)
        }
        case req@JobStatusMapRequest() => {
          val jobStatusMap: util.Map[java.lang.Long, JobStatus] = new util.HashMap[java.lang.Long, JobStatus]()
          jobIdToResultMap foreach { entry => jobStatusMap.put(entry._1, entry._2._1) }
          sender ! JobStatusMapResponse(jobStatusMap, req)
        }
        case req@ReportDefinitionMapRequest() => {
          val response = (worker !? req).asInstanceOf[ReportDefinitionMapResponse]
          sender ! response
        }
        case JobResponse(jobStatus, filePath, req) => {
          log.info("Got notify for jobId {} with status {}", req.jobId, jobStatus)
          jobIdToResultMap += (req.jobId -> (jobStatus, filePath))
        }
        case ShutDownRequest => {
          worker !? ShutDownRequest
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
class Worker(pathToRptDefs: String, pathToOutputs: String) extends Actor {

  private val log = LoggerFactory.getLogger(this.getClass)
  private lazy val rptGenerator = new ReportGenerator(pathToRptDefs, pathToOutputs)

  def act {
    log.debug("in act()")
    loop {
      react {
        case req@JobRequest(rptId, jobId) => {
          sender ! JobResponse(JobStatus.IN_PROGRESS, null, req)
          var resultStatus = JobStatus.COMPLETE
          var resultFile: Option[String] = None
          try {
            resultFile = Some(runPdfReport(rptId, jobId).getAbsolutePath)
          } catch {
            case ex: Exception => {
              log.error("Caught exception while generating report: {}", ex.getMessage)
              resultStatus = JobStatus.FAILED
            }
          }
          sender ! JobResponse(resultStatus, resultFile, req)
        }
        case req@ReportDefinitionMapRequest() => {
          val rptDefMap: util.Map[String, String] = new util.HashMap()
          rptGenerator.getAvailableRptDefs map { file =>
            rptDefMap.put(if (file.contains(".")) file.substring(0, file.indexOf('.')) else file, file)
          }
          sender ! ReportDefinitionMapResponse(rptDefMap, req)
        }
        case ShutDownRequest => {
          stopRptGenerator()
          sender ! ShutDownResponse
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

  private def runPdfReport(rptId: String, jobId: Long): File = {
    log.debug("Running report {}...", rptId)
    val rptDefName = rptId + ".rptdesign"
    val rptOutputName = "REPORT_" + jobId + ".pdf"
    val outputFile = rptGenerator.runPdfReport(rptDefName, rptOutputName)
    log.debug("Done report {}...", rptId)
    outputFile
  }

  private def startRptGenerator() {
    rptGenerator.startup
  }

  private def stopRptGenerator() {
    rptGenerator.shutdown
  }

}