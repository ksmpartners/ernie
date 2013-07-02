/*
	Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/

package com.ksmpartners.ernie.api.service

import com.ksmpartners.ernie.model
import com.ksmpartners.ernie.engine
import java.io._
import java.util
import org.slf4j.{ LoggerFactory, Logger }
import com.ksmpartners.ernie.model._
import scala.Some
import com.ksmpartners.ernie.engine.{ ReportResponse, PurgeResponse, PurgeRequest }
import scala.collection.immutable
import com.ksmpartners.ernie.api
import akka.actor._
import ActorDSL._
import akka.pattern.ask
import scala.concurrent.Await
import com.ksmpartners.ernie.engine.PurgeRequest
import scala.Some
import com.ksmpartners.ernie.api.NothingToReturnException
import com.ksmpartners.ernie.engine.PurgeResponse

/**
 * Dependencies for starting and interacting with jobs for the creation of reports
 */
trait JobDependencies extends RequiresCoordinator
    with RequiresReportManager {

  class JobsResource {
    private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.api.service.JobDependencies")

    def createJob(defId: String, rptType: ReportType, retentionPeriod: Option[Int], reportParameters: immutable.Map[String, String], userName: String): (Long, model.JobStatus) = {
      val respOpt = Await.result((coordinator ? (engine.ReportRequest(defId, rptType, retentionPeriod,
        reportParameters, userName))).mapTo[engine.ReportResponse], timeoutDuration)
      (respOpt.jobId, respOpt.jobStatus)
    }

    def getList(): List[String] = {
      Await.result((coordinator ? (engine.JobsListRequest())).mapTo[engine.JobsListResponse], timeoutDuration).jobsList.toList
    }

  }

  class JobCatalogResource {
    def getCatalog(catalog: Option[JobCatalog]): List[JobEntity] = {
      Await.result((coordinator ? (engine.JobsCatalogRequest(catalog))).mapTo[engine.JobsCatalogResponse], timeoutDuration).catalog
    }

    def purge(): (model.DeleteStatus, List[String]) = {
      val respOpt = Await.result((coordinator ? (PurgeRequest())).mapTo[PurgeResponse], timeoutDuration)
      (respOpt.deleteStatus, respOpt.purgedRptIds)
    }
  }

  class JobStatusResource {
    private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.api.service.JobDependencies")

    def get(jobId: Long): model.JobStatus = {
      Await.result((coordinator ? (engine.StatusRequest(jobId))).mapTo[engine.StatusResponse], timeoutDuration).jobStatus
    }
  }

  class JobEntityResource {
    def getJobEntity(jobId: Long): Option[model.JobEntity] =
      Await.result((coordinator ? (engine.JobDetailRequest(jobId))).mapTo[engine.JobDetailResponse], timeoutDuration).jobEntity
  }

  class JobResultsResource {

    def get(jobId: Long, file: Boolean, stream: Boolean): Option[InputStream] = {
      if (!file && !stream) throw new NothingToReturnException("Must request a file and/or stream of output")
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

    def getReportEntity(jobId: Long): Option[model.ReportEntity] = {
      Await.result((coordinator ? (engine.ReportDetailRequest(jobId))).mapTo[engine.ReportDetailResponse], timeoutDuration).rptEntity
    }

    def del(jobId: Long): DeleteStatus = {
      Await.result((coordinator ? (engine.DeleteRequest(jobId))).mapTo[engine.DeleteResponse], timeoutDuration).deleteStatus
    }
  }

}

object JobDependencies {
  private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.api.service.JobDependencies")
}

