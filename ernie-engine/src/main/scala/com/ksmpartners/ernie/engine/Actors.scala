/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.engine

import actors.Actor
import scala.math.abs
import collection._
import com.ksmpartners.ernie.model.JobStatus
import util.Random
import org.slf4j.LoggerFactory

/**
 * Actor for coordinating report generation.
 */
class Coordinator(pathToRptDefs: String, pathToOutputs: String) extends Actor {

  private val log = LoggerFactory.getLogger(this.getClass)

  private lazy val worker: Worker = new Worker(pathToRptDefs, pathToOutputs)
  private val jobIdToStatusMap = new mutable.HashMap[Int, JobStatus]()
  private val rnd: Random = new Random()

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
        case ReportRequest(rptId) => {
          val jobId = getJobId
          jobIdToStatusMap += (jobId -> JobStatus.PENDING)
          sender ! Notify(jobId, jobIdToStatusMap.get(jobId).get, this)
          worker ! JobRequest(rptId, jobId, this)
        }
        case StatusRequest(jobId) => {
          sender ! Notify(jobId, jobIdToStatusMap.getOrElse(jobId, JobStatus.NO_SUCH_JOB), this)
        }
        case Notify(jobId, jobStatus, worker) => {
          jobIdToStatusMap += (jobId -> jobStatus)
          log.info("Got notify for jobId {} with status {}", jobId, jobStatus)
        }
        case ShutDownRequest => {
          worker !? ShutDownRequest
          exit()
        }
        case msg => log.info("Received unexpected message: {}", msg)
      }
    }
  }

  var currJobId = System.currentTimeMillis()

  // TODO: Rework logic for getting jobId
  private def getJobId: Int = {
    var rndId = 0
    var found = false
    while (!found) {
      rndId = abs(rnd.nextInt())
      if (!jobIdToStatusMap.contains(rndId))
        found = true
    }
    rndId
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
        case JobRequest(rptId, jobId, requester) => {
          requester ! Notify(jobId, JobStatus.IN_PROGRESS, this)
          var result = JobStatus.COMPLETE
          try {
            runPdfReport(rptId, jobId)
          } catch {
            case ex: Exception => {
              log.error("Caught exception while generating report: {}", ex.getMessage)
              result = JobStatus.FAILED
            }
          }
          requester ! Notify(jobId, result, this)
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

  private def runPdfReport(rptId: String, jobId: Int) {
    log.debug("Running report {}...", rptId)
    val rptDefName = rptId + ".rptdesign"
    val rptOutputName = "REPORT_" + jobId + ".pdf"
    rptGenerator.runPdfReport(rptDefName, rptOutputName)
    log.debug("Done report {}...", rptId)
  }

  private def startRptGenerator() {
    rptGenerator.startup
  }

  private def stopRptGenerator() {
    rptGenerator.shutdown
  }

}