package com.ksmpartners.ernie.engine

import scala.actors.Actor
import scala.actors.Actor._
import scala.math.abs
import collection.mutable.HashMap
import com.ksmpartners.ernie.model.JobStatus
import util.Random

object Coordinator extends Actor {

  // TODO: Replace println with proper logging.

  private var jobIdToStatusMap = new HashMap[Int, JobStatus]()
  private val rnd: Random = new Random()

  def act {
    println("Coord: in act()")
    while (true) {
      receive {
        case ReportRequest(rptId) =>
          val jobId = getJobId
          jobIdToStatusMap += (jobId -> JobStatus.PENDING)
          sender ! Notify(jobId, jobIdToStatusMap.get(jobId).get, this)
          Worker ! JobRequest(rptId, jobId, this)
        case StatusRequest(jobId) =>
          if (jobIdToStatusMap.contains(jobId))
            sender ! Notify(jobId, jobIdToStatusMap.get(jobId).get, this)
        case Notify(jobId, jobStatus, worker) =>
          jobIdToStatusMap += (jobId -> jobStatus)
          println("Coord: got notify for id: " + jobId + ", status: " + jobStatus)
        case msg => System.out.println("Received message: " + msg.toString)
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

object Worker extends Actor {

  def act {
    loop {
      react {
        case JobRequest(rptId, jobId, requester) =>
          requester ! Notify(jobId, JobStatus.IN_PROGRESS, this)
          runReport(rptId, jobId)
          requester ! Notify(jobId, JobStatus.COMPLETE, this)
        case msg => println("Worker: received message: " + msg.toString)
      }
    }
  }

  def runReport(rptId: Int, jobId: Int) {
    // TODO: Run report...
    println("Worker" + jobId + ": running report " + rptId + "...")
    Thread.sleep(1000)
    println("Worker" + jobId + ": done report " + rptId + "...")
  }

}