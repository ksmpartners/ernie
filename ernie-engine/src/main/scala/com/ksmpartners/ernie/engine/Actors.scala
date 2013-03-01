package com.ksmpartners.ernie.engine

import actors.Actor
import scala.math.abs
import collection.mutable.HashMap
import com.ksmpartners.ernie.model.{Notification, JobStatus}
import util.Random
import org.slf4j.LoggerFactory
import org.eclipse.birt.report.engine.api.EngineException

class Coordinator(rptGenerator: ReportGenerator) extends Actor {

  val LOG = LoggerFactory.getLogger(this.getClass)

  private var worker: Worker = null
  private val jobIdToStatusMap = new HashMap[Int, JobStatus]()
  private val rnd: Random = new Random()


  override def start(): Actor = {
    LOG.debug("Starting %s".format(this))
    super.start()
    this.worker = new Worker(rptGenerator)
    worker.start
    this
  }

  def act {
    LOG.info("%s: in act()".format(this))
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
          LOG.info("%s: got notify for jobId %s with status %s".format(this, jobId, jobStatus))
        case msg => LOG.info("%s: Received message: %s".format(this, msg))
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

  val LOG = LoggerFactory.getLogger(this.getClass)

  def act {
    loop {
      react {
        case JobRequest(rptId, jobId, requester) =>
          requester ! Notify(jobId, JobStatus.IN_PROGRESS, this)
          val result = (runPdfReport(rptId, jobId) match {
            case true => JobStatus.COMPLETE
            case false => JobStatus.FAILED
          })
          requester ! Notify(jobId, result, this)
        case msg => LOG.info("%s: received message: %s".format(this, msg))
      }
    }
  }

  override def start(): Actor = {
    super.start()
    startRptGenerator
    this
  }

  def runPdfReport(rptId: String, jobId: Int): Boolean = {
    // TODO: Run report...
    LOG.info("%s: running report %s...".format(this, rptId))
    var success: Boolean = true
    try {
      rptGenerator.runPdfReport(rptId + ".rptdesign", "REPORT_" + jobId + ".pdf")
    } catch {
      case ex: EngineException =>
        LOG.error("Caught exception %s".format(ex))
        success = false
    }
    LOG.info("%s: done report %s...".format(this, rptId))
    success
  }

  def startRptGenerator {
    rptGenerator.startup
  }

  def stopRptGenerator {
    rptGenerator.shutdown
  }

}