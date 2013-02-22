package com.ksmpartners.ernie.engine

import actors.Actor
import com.ksmpartners.ernie.model.JobStatus

case class ReportRequest(rtpDefId: Int)
case class JobRequest(rtpDefId: Int, jobId: Int, self: Actor)
case class StatusRequest(jobId: Int)
case class Notify(jobId: Int, jobStatus: JobStatus, self: Actor)
