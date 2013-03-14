/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.engine

import com.ksmpartners.ernie.model.JobStatus

case class ReportRequest(rtpDefId: String)
case class ReportResponse(jobId: Long, req: ReportRequest)
case class ResultRequest(jobId: Long)
case class ResultResponse(filePath: Option[String], req: ResultRequest)
case class StatusRequest(jobId: Long)
case class StatusResponse(jobStatus: JobStatus, req: StatusRequest)

case class JobRequest(rtpDefId: String, jobId: Long)
case class JobResponse(jobStatus: JobStatus, filePath: Option[String], req: JobRequest)
case class ShutDownRequest()
case class ShutDownResponse()
