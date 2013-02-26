package com.ksmpartners.ernie.engine

import actors.Actor
import com.ksmpartners.ernie.model.JobStatus

case class ReportRequest(rtpDefId: String)
case class JobRequest(rtpDefId: String, jobId: Int, self: Actor)
case class StatusRequest(jobId: Int)
case class Notify(jobId: Int, jobStatus: JobStatus, self: Actor)
