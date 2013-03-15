/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.engine

import com.ksmpartners.ernie.model.{ JobStatusMap, JobStatus }

/** Request that a report be generated */
case class ReportRequest(rtpDefId: String)
/** The response to the given ReportRequest */
case class ReportResponse(jobId: Long, req: ReportRequest)
/** Request the resulting file for the given jobId */
case class ResultRequest(jobId: Long)
/** The response to the given ResultRequest */
case class ResultResponse(filePath: Option[String], req: ResultRequest)
/** Request the status for the given jobId */
case class StatusRequest(jobId: Long)
/** The response to the given StatusRequest */
case class StatusResponse(jobStatus: JobStatus, req: StatusRequest)
/** Request a map of jobs to their statuses */
case class JobStatusMapRequest()
/** The response to the given JobStatusMapRequest */
case class JobStatusMapResponse(jobStatusMap: JobStatusMap, req: JobStatusMapRequest)

/** Request that the given rptDefId be processed */
case class JobRequest(rtpDefId: String, jobId: Long)
/** The response(s) associated with the given JobRequest */
case class JobResponse(jobStatus: JobStatus, filePath: Option[String], req: JobRequest)
/** Request that the Actor be shut down */
case class ShutDownRequest()
/** The response that indicates that the Actor's facilities are shut down */
case class ShutDownResponse()
