/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.api.service

import com.ksmpartners.ernie.model
import com.ksmpartners.ernie.engine
import java.io.{ ByteArrayOutputStream, IOException }
import java.util
import org.slf4j.{ LoggerFactory, Logger }
import com.ksmpartners.ernie.model._
import scala.Some
import com.ksmpartners.ernie.engine.{ ReportResponse, PurgeResponse, PurgeRequest }
import scala.collection.immutable
import com.ksmpartners.ernie.api
import com.ksmpartners.ernie.api.{ NothingToReturnException, JobStatus, TimeoutException }

/**
 * Dependencies for starting and interacting with jobs for the creation of reports
 */
trait JobDependencies extends RequiresCoordinator
    with RequiresReportManager {

  /**
   * Resource for handling HTTP requests at /jobs
   */
  class JobsResource {

    def createJob(defId: String, rptType: ReportType, retentionPeriod: Option[Int], reportParameters: immutable.Map[String, String], userName: String): JobStatus = {
      val respOpt = (coordinator !? (timeout, engine.ReportRequest(defId, rptType, retentionPeriod,
        reportParameters, userName))).asInstanceOf[Option[engine.ReportResponse]]
      if (respOpt.isEmpty) throw new TimeoutException("Job request timed out")
      else JobStatus(respOpt.get.jobId, Some(respOpt.get.jobStatus), None)
    }

    def getCatalog(catalog: Option[JobCatalog]): List[JobEntity] = {
      val respOpt: Option[engine.JobsCatalogResponse] = (coordinator !? (timeout, engine.JobsCatalogRequest(catalog))).asInstanceOf[Option[engine.JobsCatalogResponse]]
      if (respOpt.isEmpty)
        throw new TimeoutException("Catalog request timed out")
      else respOpt.get.catalog
    }

    def getJobEntity(jobId: Long): api.JobEntity = {
      val respOpt = (coordinator !? (timeout, engine.JobDetailRequest(jobId))).asInstanceOf[Option[engine.JobDetailResponse]]
      if (respOpt.isEmpty) {
        throw new TimeoutException("Job entity request tiemd out")
      } else {
        if (respOpt.get.jobEntity.isEmpty) api.JobEntity(None, Some(new api.NotFoundException("Job ID not found")))
        else api.JobEntity(respOpt.get.jobEntity, None)
      }
    }

    def purge(): api.PurgeResult = {
      val respOpt = (coordinator !? (timeout, PurgeRequest())).asInstanceOf[Option[PurgeResponse]]
      if (respOpt.isEmpty) throw new TimeoutException("Purge request timed out")
      else api.PurgeResult(respOpt.get.deleteStatus, respOpt.get.purgedRptIds, None)
    }
  }

  /**
   * Resource for handling HTTP requests at /jobs/<JOB_ID>/status
   */
  class JobStatusResource {
    /**
     * Return a Box[ListResponse] containing status for the given jobId
     */
    def get(jobId: Long): api.JobStatus = {
      val respOpt = (coordinator !? (timeout, engine.StatusRequest(jobId))).asInstanceOf[Option[engine.StatusResponse]]
      if (respOpt.isEmpty) throw new TimeoutException("Job status request timed out")
      else {
        JobStatus(jobId, Some(respOpt.get.jobStatus), None)
      }
    }
  }

  /**
   * Resource for handling HTTP requests at /jobs/<JOB_ID>/result
   */
  class JobResultsResource {

    def get(jobId: Long, file: Boolean, stream: Boolean): api.ReportOutput = {
      if (!file && !stream) throw new NothingToReturnException("Must request a file and/or stream of output")
      val statusRespOpt = (coordinator !? (timeout, engine.StatusRequest(jobId))).asInstanceOf[Option[engine.StatusResponse]]
      if (statusRespOpt.isEmpty) throw new TimeoutException("Status request timed out")
      else {
        val statusResponse = statusRespOpt.get
        if (statusResponse.jobStatus != model.JobStatus.COMPLETE) {
          throw new api.ReportOutputException(Some(statusResponse.jobStatus), "Failure to retrieve job output")
        } else {
          val respOpt = (coordinator !? (timeout, engine.ResultRequest(jobId.toLong))).asInstanceOf[Option[engine.ResultResponse]]
          if (respOpt.isEmpty) throw new TimeoutException("Output request timed out")
          else {
            val response = respOpt.get
            if (response.rptId.isDefined) {
              val rptId = response.rptId.get
              var error: Option[Exception] = None
              var bAOS: Option[java.io.InputStream] = None
              if (stream) bAOS = try {
                reportManager.getReportContent(rptId)
              } catch {
                case e: Exception => { error = Some(e); None }
              }
              api.ReportOutput(bAOS, if (file) Some(new java.io.File(outputDir, rptId + ".entity")) else None, error)
            } else throw new api.NotFoundException("Report output not found")
          }
        }
      }
    }

    /**
     * Retrieves details for output from a given jobId
     */
    def getReportEntity(jobId: Long): api.ReportEntity = {
      val respOpt = (coordinator !? (timeout, engine.ReportDetailRequest(jobId))).asInstanceOf[Option[engine.ReportDetailResponse]]
      if (respOpt.isEmpty) throw new TimeoutException("Report entity request timed out")
      else api.ReportEntity(respOpt.get.rptEntity, None)
    }

    /**
     * Purges the report output for a given jobId
     */
    def del(jobId: Long): DeleteStatus = {
      val respOpt = (coordinator !? (timeout, engine.DeleteRequest(jobId))).asInstanceOf[Option[engine.DeleteResponse]]
      if (respOpt.isEmpty) throw new TimeoutException("Delete request timed out")
      else respOpt.get.deleteStatus
    }
  }

}

object JobDependencies {
  private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.api.service.JobDependencies")
}

