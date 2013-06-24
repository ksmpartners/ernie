/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.api.service

import com.ksmpartners.ernie.model
import com.ksmpartners.ernie.engine
import java.io.{ File, ByteArrayOutputStream, IOException }
import java.util
import org.slf4j.{ LoggerFactory, Logger }
import com.ksmpartners.ernie.model._
import scala.Some
import com.ksmpartners.ernie.engine.{ ReportResponse, PurgeResponse, PurgeRequest }
import scala.collection.immutable
import com.ksmpartners.ernie.api
import com.ksmpartners.ernie.api.{ NothingToReturnException, JobStatus }
import akka.actor._
import ActorDSL._
import akka.pattern.ask
import scala.concurrent.Await

/**
 * Dependencies for starting and interacting with jobs for the creation of reports
 */
trait JobDependencies extends RequiresCoordinator
    with RequiresReportManager {

  class JobsResource {
    private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.api.service.JobDependencies")

    def createJob(defId: String, rptType: ReportType, retentionPeriod: Option[Int], reportParameters: immutable.Map[String, String], userName: String): JobStatus = {
      val respOpt = Await.result((coordinator ? (engine.ReportRequest(defId, rptType, retentionPeriod,
        reportParameters, userName))).mapTo[engine.ReportResponse], timeoutDuration)
      JobStatus(respOpt.jobId, Some(respOpt.jobStatus), None)
    }

    def getList(): List[String] = {
      Await.result((coordinator ? (engine.JobsListRequest())).mapTo[engine.JobsListResponse], timeoutDuration).jobsList.toList
    }

  }

  class JobCatalogResource {
    def getCatalog(catalog: Option[JobCatalog]): List[JobEntity] = {
      Await.result((coordinator ? (engine.JobsCatalogRequest(catalog))).mapTo[engine.JobsCatalogResponse], timeoutDuration).catalog
    }

    def purge(): api.PurgeResult = {
      val respOpt = Await.result((coordinator ? (PurgeRequest())).mapTo[PurgeResponse], timeoutDuration)
      api.PurgeResult(respOpt.deleteStatus, respOpt.purgedRptIds, None)
    }
  }

  class JobStatusResource {
    private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.api.service.JobDependencies")

    def get(jobId: Long): api.JobStatus = {
      JobStatus(jobId,
        Some(Await.result((coordinator ? (engine.StatusRequest(jobId))).mapTo[engine.StatusResponse], timeoutDuration).jobStatus), None)
    }
  }

  class JobEntityResource {
    def getJobEntity(jobId: Long): api.JobEntity = {
      val respOpt = Await.result((coordinator ? (engine.JobDetailRequest(jobId))).mapTo[engine.JobDetailResponse], timeoutDuration)
      if (respOpt.jobEntity.isEmpty) api.JobEntity(None, Some(new api.NotFoundException("Job ID not found")))
      else api.JobEntity(respOpt.jobEntity, None)
    }
  }

  class JobResultsResource {

    def get(jobId: Long, file: Boolean, stream: Boolean): api.ReportOutput = {
      if (!file && !stream) throw new NothingToReturnException("Must request a file and/or stream of output")
      val statusResponse = Await.result((coordinator ? (engine.StatusRequest(jobId))).mapTo[engine.StatusResponse], timeoutDuration)
      if (statusResponse.jobStatus != model.JobStatus.COMPLETE) {
        if (statusResponse.jobStatus == model.JobStatus.NO_SUCH_JOB) throw new api.NotFoundException("Job " + jobId + " not found")
        throw new api.ReportOutputException(Some(statusResponse.jobStatus), "Failure to retrieve job output")
      } else {
        val response = Await.result((coordinator ? (engine.ResultRequest(jobId.toLong))).mapTo[engine.ResultResponse], timeoutDuration)
        if (response.rptId.isDefined) {
          val rptId = response.rptId.get
          var error: Option[Exception] = None
          var bAOS: Option[java.io.InputStream] = None
          if (stream) bAOS = try {
            reportManager.getReportContent(rptId)
          } catch {
            case e: Exception => { error = Some(e); None }
          }
          api.ReportOutput(bAOS, if (file) Some(new java.io.File(outputDir, rptId + ".entity")) else None, reportManager.getReport(rptId).get.getEntity, error)
        } else throw new api.NotFoundException("Report output not found")
      }

    }

    def getReportEntity(jobId: Long): api.ReportEntity = {
      api.ReportEntity(Await.result((coordinator ? (engine.ReportDetailRequest(jobId))).mapTo[engine.ReportDetailResponse], timeoutDuration).rptEntity, None)
    }

    def del(jobId: Long): DeleteStatus = {
      Await.result((coordinator ? (engine.DeleteRequest(jobId))).mapTo[engine.DeleteResponse], timeoutDuration).deleteStatus
    }
  }

}

object JobDependencies {
  private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.api.service.JobDependencies")
}

