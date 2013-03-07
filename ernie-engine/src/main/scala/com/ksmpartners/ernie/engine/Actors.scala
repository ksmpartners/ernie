package com.ksmpartners.ernie.engine

import actors.Actor
import scala.math.abs
import collection._
import com.ksmpartners.ernie.model.JobStatus
import util.Random
import org.slf4j.LoggerFactory
import org.eclipse.birt.report.engine.api.EngineException

class Coordinator(rptGenerator: ReportGenerator) extends Actor {

  private val log = LoggerFactory.getLogger(this.getClass)

  private var worker: Worker = null
  private val jobIdToStatusMap = new mutable.HashMap[Int, JobStatus]()
  private val rnd: Random = new Random()

  override def start(): Actor = {
    log.debug("in start()")
    super.start()
    this.worker = new Worker(rptGenerator)
    worker.start
    this
  }

  def act {
    log.debug("in act()")
    while (true) {
      receive {
        case ReportRequest(rptId) =>
          val jobId = getJobId
          jobIdToStatusMap += (jobId -> JobStatus.PENDING)
          sender ! Notify(jobId, jobIdToStatusMap.get(jobId).get, this)
          worker ! JobRequest(rptId, jobId, this)
        case StatusRequest(jobId) =>
          val jobStatus = (jobIdToStatusMap.contains(jobId) match {
            case true => jobIdToStatusMap.get(jobId).get
            case false => JobStatus.NO_SUCH_JOB
          })
          sender ! Notify(jobId, jobStatus, this)
        case Notify(jobId, jobStatus, worker) =>
          jobIdToStatusMap += (jobId -> jobStatus)
          log.info("Got notify for jobId {} with status {}", jobId, jobStatus)
        case msg => log.info("Received unexpected message: {}", msg)
      }
    }
  }

  // TODO: Rework logic for getting jobId
  def getJobId: Int = {
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

class Worker(rptGenerator: ReportGenerator) extends Actor {

  private val log = LoggerFactory.getLogger(this.getClass)

  def act {
    log.debug("in act()")
    loop {
      react {
        case JobRequest(rptId, jobId, requester) =>
          requester ! Notify(jobId, JobStatus.IN_PROGRESS, this)
          val result = (runPdfReport(rptId, jobId) match {
            case true => JobStatus.COMPLETE
            case false => JobStatus.FAILED
          })
          requester ! Notify(jobId, result, this)
        case msg => log.info("Received unexpected message: {}", msg)
      }
    }
  }

  override def start(): Actor = {
    log.debug("in start()")
    super.start()
    startRptGenerator
    this
  }

  def runPdfReport(rptId: String, jobId: Int): Boolean = {
    log.debug("Running report {}...", rptId)
    val rptDefName = rptId + ".rptdesign"
    val rptOutputName = "REPORT_" + jobId + ".pdf"
    var success: Boolean = true
    try {
      rptGenerator.runPdfReport(rptDefName, rptOutputName)
    } catch {
      case ex: EngineException =>
        log.error("Caught exception while generating report: {}", ex.getMessage)
        success = false
    }
    log.debug("Done report {}...", rptId)
    success
  }

  def startRptGenerator {
    rptGenerator.startup
  }

  def stopRptGenerator {
    rptGenerator.shutdown
  }

}