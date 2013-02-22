package com.ksmpartners.ernie.engine

import scala.actors.Actor
import scala.actors.Actor._
import scala.math.abs
import collection.mutable.HashMap
import com.ksmpartners.ernie.model.JobStatus
import util.Random

object Coordinator extends Actor {

  var jobIdToStatusMap = new HashMap[Int, JobStatus]()

  val rnd: Random = new Random()

  def act() {
    println("Coord: in act()")
    while (true) {
      receive {
        case Request(id, _) =>
          val jobId = getJobId
          jobIdToStatusMap += (jobId -> JobStatus.PENDING)
          Worker ! Request(jobId, this)
        case Notify(jobId, jobStatus, worker) =>
          jobIdToStatusMap += (jobId -> jobStatus)
          println("Coord: got notify for id: " + jobId + ", status: " + jobStatus)
        case msg => System.out.println("Received message: " + msg.toString)
      }
    }
  }

  def getJobId(): Int = {
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

  def act() {
    loop {
      react {
        case Request(id, requester) =>
          requester ! Notify(id, JobStatus.IN_PROGRESS, this)
          runReport()
          requester ! Notify(id, JobStatus.COMPLETE, this)
        case msg => println("Worker: received message: " + msg.toString)
      }
    }
  }

  def runReport() {
    // TODO: Run report...
    println("Worker: running report...")
    Thread.sleep(1000)
    println("Worker: done report...")
  }

}