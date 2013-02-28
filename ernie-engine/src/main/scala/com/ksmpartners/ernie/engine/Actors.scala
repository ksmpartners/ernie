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
    super.start()
    this.worker = new Worker(rptGenerator)
    worker.start
    this
  }

  def act {
    LOG.info("Coord: in act()")
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
          sender ! new Notification(jobId, jobStatus)
        case Notify(jobId, jobStatus, worker) =>
          jobIdToStatusMap += (jobId -> jobStatus)
          LOG.info("Coord: got notify for id: " + jobId + ", status: " + jobStatus)
        case msg => LOG.info("Coord: Received message: " + msg.toString)
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
        case msg => LOG.info("Worker: received message: " + msg.toString)
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
    LOG.info("Worker" + jobId + ": running report " + rptId + "...")
    var success: Boolean = true
    try {
      rptGenerator.runPdfReport(rptId + ".rptdesign", "REPORT_" + jobId + ".pdf")
    } catch {
      case ex: EngineException =>
        LOG.error("Caught exception {}", ex)
        success = false
    }
    LOG.info("Worker" + jobId + ": done report " + rptId + "...")
    success
  }

  def startRptGenerator {
    rptGenerator.startup
  }

  def stopRptGenerator {
    rptGenerator.shutdown
  }

}