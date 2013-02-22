package com.ksmpartners.ernie.engine

import actors.Actor
import com.ksmpartners.ernie.model.JobStatus

case class Request(rtpDefId: Int, self: Actor)
case class Notify(jobId: Int, jobStatus: JobStatus, self: Actor)
