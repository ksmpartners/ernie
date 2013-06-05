/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.engine

import com.ksmpartners.ernie.model._
import scala.collection.immutable

/** Request that the report defId be generated resulting in an output of type rptType */
case class ReportRequest(defId: String, rptType: ReportType, retentionPeriod: Option[Int], reportParameters: immutable.Map[String, String])
/** The response to the given ReportRequest */
case class ReportResponse(jobId: Long, jobStatus: JobStatus, req: ReportRequest)
/** Request the resulting file for the given jobId */
case class ResultRequest(jobId: Long)
/** The response to the given ResultRequest */
case class ResultResponse(rptId: Option[String], req: ResultRequest)
/** Request the status for the given jobId */
case class StatusRequest(jobId: Long)
/** The response to the given StatusRequest */
case class StatusResponse(jobStatus: JobStatus, req: StatusRequest)
/** Request job output deletion for the given jobId */
case class DeleteRequest(jobId: Long)
/** The response to the given DeleteRequest */
case class DeleteResponse(deleteStatus: DeleteStatus, req: DeleteRequest)
/** Request report definition deletion for the given jobId */
case class DeleteDefinitionRequest(defId: String)
/** The response to the given DeleteDefinitionRequest */
case class DeleteDefinitionResponse(deleteStatus: DeleteStatus, req: DeleteDefinitionRequest)
/** Request purge of all expired reports */
case class PurgeRequest()
/** The response to the given PurgeRequest */
case class PurgeResponse(deleteStatus: DeleteStatus, purgedRptIds: List[String], req: PurgeRequest)
/** Request a list of the currently known jobIds */
case class JobsListRequest()
/** The response to the given JobsListRequest */
case class JobsListResponse(jobsList: Array[String], req: JobsListRequest)
/** Request that the definition defId be generated into a rptType document */
case class JobRequest(defId: String, rptType: ReportType, jobId: Long, retentionPeriod: Option[Int], reportParameters: immutable.Map[String, String])
/** The response(s) associated with the given JobRequest */
case class JobResponse(jobStatus: JobStatus, rptId: Option[String], req: JobRequest)
/** Request that the Actor be shut down */
case class ShutDownRequest()
/** The response that indicates that the Actor's facilities are shut down */
case class ShutDownResponse()
/**Request details for a given report */
case class ReportDetailRequest(jobId: Long)
/**The response associated with the given ReportDetailRequest*/
case class ReportDetailResponse(rptEntity: Option[ReportEntity], req: ReportDetailRequest)
/**Request details for a given job */
case class JobDetailRequest(jobId: Long)
/**The response associated with the given ReportDetailRequest*/
case class JobDetailResponse(jobEntity: Option[JobEntity], req: ReportDetailRequest)
