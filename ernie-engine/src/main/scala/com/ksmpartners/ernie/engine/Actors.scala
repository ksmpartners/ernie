package com.ksmpartners.ernie.engine

import actors.Actor
import scala.math.abs
import collection.mutable.HashMap
import com.ksmpartners.ernie.model.{Notification, JobStatus}
import util.Random
import org.slf4j.LoggerFactory
import org.eclipse.birt.report.engine.api.EngineException

class Coordinator(rptGenerator: ReportGenerator) extends Actor {

  private val LOG = LoggerFactory.getLogger(this.getClass)

  private var worker: Worker = null
  private val jobIdToStatusMap = new HashMap[Int, JobStatus]()
  private val rnd: Random = new Random()


  override def start(): Actor = {
    LOG.debug("in start()")
    super.start()
    this.worker = new Worker(rptGenerator)
    worker.start
    this
  }

  def act {
    LOG.debug("in act()")
    while (true) {
      receive {
        case ReportRequest(rptId) =>
          val jobId = getJobId
          jobIdToStatusMap += (jobId -> JobStatus.PENDING)
          sender ! new Notification(jobId, jobIdToStatusMap.get(jobId).get)
          worker ! JobRequest(rptId, jobId, this)
        case StatusRequest(jobId) =>
          val jobStatus = (jobIdToStatusMap.contains(jobId) match {
            case true => jobIdToStatusMap.get(jobId).get
            case false => JobStatus.NO_SUCH_JOB
          })
          sender ! new Notification(jobId, jobStatus)
        case Notify(jobId, jobStatus, worker) =>
          jobIdToStatusMap += (jobId -> jobStatus)
          LOG.info("Got notify for jobId %s with status %s".format(jobId, jobStatus))
        case msg => LOG.info("Received unexpected message: %s".format(msg))
      }
    }
  }

  // TODO: Rework logic for getting jobId
  def getJobId: Int = {
    var rndId = 0
    var found = false
    while(!found) {
      rndId = abs(rnd.nextInt())
      if (!jobIdToStatusMap.contains(rndId))
        found = true
    }
    rndId
  }

}

class Worker(rptGenerator: ReportGenerator) extends Actor {

  private val LOG = LoggerFactory.getLogger(this.getClass)

  def act {
    LOG.debug("in act()")
    loop {
      react {
        case JobRequest(rptId, jobId, requester) =>
          requester ! Notify(jobId, JobStatus.IN_PROGRESS, this)
          val result = (runPdfReport(rptId, jobId) match {
            case true => JobStatus.COMPLETE
            case false => JobStatus.FAILED
          })
          requester ! Notify(jobId, result, this)
        case msg => LOG.info("Received unexpected message: %s".format(msg))
      }
    }
  }

  override def start(): Actor = {
    LOG.debug("in start()")
    super.start()
    startRptGenerator
    this
  }

  def runPdfReport(rptId: String, jobId: Int): Boolean = {
    LOG.debug("Running report %s...".format(rptId))
    val rptDefName = rptId + ".rptdesign"
    val rptOutputName = "REPORT_" + jobId + ".pdf"
    var success: Boolean = true
    try {
      rptGenerator.runPdfReport(rptDefName, rptOutputName)
    } catch {
      case ex: EngineException =>
        LOG.error("Caught exception %s".format(ex))
        success = false
    }
    LOG.debug("Done report %s...".format(rptId))
    success
  }

  def startRptGenerator {
    rptGenerator.startup
  }

  def stopRptGenerator {
    rptGenerator.shutdown
  }

}