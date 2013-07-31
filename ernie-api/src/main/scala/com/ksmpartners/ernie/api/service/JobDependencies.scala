/*
	Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/

package com.ksmpartners.ernie.api.service

import com.ksmpartners.ernie.model
import com.ksmpartners.ernie.engine
import java.io._
import com.ksmpartners.ernie.model._
import scala.collection.immutable
import com.ksmpartners.ernie.api
import akka.pattern.ask
import scala.concurrent.{ Future, Await }
import com.ksmpartners.ernie.engine.{ JobNotificationResponse, PurgeRequest, PurgeResponse }
import scala.Some

/**
 * Dependencies for starting and interacting with jobs for the creation of reports.
 */
trait JobDependencies extends RequiresCoordinator
    with RequiresReportManager {

  /**
   * Provides job creation and retrieval of jobs list.
   */
  class JobsResource {

    /**
     * Create and start a report generation job.
     * @param defId an existing report definition/design
     * @param rptType the report output format
     * @param retentionPeriod optional override for default number of days to retain report output
     * @param reportParameters a set of BIRT Report Parameters corresponding to the parameters specified in the report definition.
     * @param userName username of the user creating the job
     * @return the generated job ID and a [[com.ksmpartners.ernie.model.JobStatus]]
     */
    def createJob(defId: String, rptType: ReportType, retentionPeriod: Option[Int], reportParameters: immutable.Map[String, String], userName: String): (Long, model.JobStatus) = {
      val respOpt = Await.result((coordinator ? (engine.ReportRequest(defId, rptType, retentionPeriod,
        reportParameters, userName))).mapTo[engine.ReportResponse], timeoutDuration)
      (respOpt.jobId, respOpt.jobStatus)
    }

    /**
     * Get a list of all job IDs as strings.
     */
    def getList(): List[String] = {
      Await.result((coordinator ? (engine.JobsListRequest())).mapTo[engine.JobsListResponse], timeoutDuration).jobsList.toList
    }

  }

  /**
   * Provides job catalog operations.
   */
  class JobCatalogResource {
    /**
     * Get a catalog of jobs.
     * @param catalog optionally specify a subset of jobs to retrieve
     * @return a list of [[com.ksmpartners.ernie.model.JobEntity]] constituting the catalog.
     */
    def getCatalog(catalog: Option[JobCatalog]): List[JobEntity] = {
      Await.result((coordinator ? (engine.JobsCatalogRequest(catalog))).mapTo[engine.JobsCatalogResponse], timeoutDuration).catalog
    }

    /**
     * Purge jobs in expired catalog.
     * @return the status of the batch deletion and a list of purged report IDs.
     */
    def purge(): (model.DeleteStatus, List[String]) = {
      val respOpt = Await.result((coordinator ? (PurgeRequest())).mapTo[PurgeResponse], timeoutDuration)
      (respOpt.deleteStatus, respOpt.purgedRptIds)
    }
  }

  /**
   * Provides job status interrogation.
   */
  class JobStatusResource {
    /**
     * Get the status of a given job ID.
     */
    def get(jobId: Long): model.JobStatus = {
      Await.result((coordinator ? (engine.StatusRequest(jobId))).mapTo[engine.StatusResponse], timeoutDuration).jobStatus
    }

    /**
     * Get a Future to notify caller on job status change
     */
    def getFuture(jobId: Long, status: Option[JobStatus]): Future[JobNotificationResponse] = {
      (coordinator ? engine.JobNotificationRequest(jobId, status)).mapTo[JobNotificationResponse]
    }
  }

  /**
   * Provides job metadata interrogation.
   */
  class JobEntityResource {
    /**
     * Retrieve job metadata.
     * @param jobId the ID of the job to interrogate.
     * @return a JobEntity if the jobId is found; otherwise, [[scala.None]]
     */
    def getJobEntity(jobId: Long): Option[model.JobEntity] =
      Await.result((coordinator ? (engine.JobDetailRequest(jobId))).mapTo[engine.JobDetailResponse], timeoutDuration).jobEntity
  }

  /**
   * Provides job results operations.
   */
  class JobResultsResource {

    /**
     * Retrieve job output.
     * @param jobId the jobId whose output is to be retrieved
     * @return a [[java.io.InputStream]] if the report output is available; otherwise, [[scala.None]]
     */
    def get(jobId: Long): Option[InputStream] = {
      val statusResponse = Await.result((coordinator ? (engine.StatusRequest(jobId))).mapTo[engine.StatusResponse], timeoutDuration)
      if (statusResponse.jobStatus == model.JobStatus.NO_SUCH_JOB) None
      else if (statusResponse.jobStatus != model.JobStatus.COMPLETE) {
        throw new api.ReportOutputException(Some(statusResponse.jobStatus), "Failure to retrieve job output")
      } else {
        val response = Await.result((coordinator ? (engine.ResultRequest(jobId.toLong))).mapTo[engine.ResultResponse], timeoutDuration)
        if (response.rptId.isDefined) {
          reportManager.getReportContent(response.rptId.get)
        } else None
      }

    }

    /**
     * Retrieve report output metadata.
     * @param jobId the job whose report output metadata is to be interrogated
     * @return a [[com.ksmpartners.ernie.model.ReportEntity]] if the job ID is found.
     */
    def getReportEntity(jobId: Long): Option[model.ReportEntity] = {
      Await.result((coordinator ? (engine.ReportDetailRequest(jobId))).mapTo[engine.ReportDetailResponse], timeoutDuration).rptEntity
    }

    /**
     * Delete a job's output and any associated metadata
     * @param jobId the job whose output and metadata is to be deleted
     * @return the status of the deletion
     */
    def del(jobId: Long): DeleteStatus = {
      Await.result((coordinator ? (engine.DeleteRequest(jobId))).mapTo[engine.DeleteResponse], timeoutDuration).deleteStatus
    }
  }

}

