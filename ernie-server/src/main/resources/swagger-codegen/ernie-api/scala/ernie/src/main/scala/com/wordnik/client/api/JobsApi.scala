package com.wordnik.client.api

import com.wordnik.client.model.Byte
import com.wordnik.client.model.JobStatusMap
import com.wordnik.client.model.JobsCatalogResponse
import com.wordnik.client.model.DeleteResponse
import com.wordnik.client.model.ReportEntity
import com.wordnik.client.model.JobEntity
import com.wordnik.client.model.StatusResponse
import com.wordnik.client.common.ApiInvoker
import com.wordnik.client.common.ApiException

import java.io.File

import scala.collection.mutable.HashMap

class JobsApi {
  var basePath: String = "http://localhost:8080"
  var apiInvoker = ApiInvoker

  def addHeader(key: String, value: String) = apiInvoker.defaultHeaders += key -> value

  def getJobsMap(Authorization: String, Accept: String): Option[jobStatusMap] = {
    // create path and map variables
    val path = "/jobs".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

    headerParams += "Authorization" -> Authorization
    headerParams += "Accept" -> Accept
    try {
      apiInvoker.invokeApi(basePath, path, "GET", queryParams.toMap, None, headerParams.toMap, contentType) match {
        case s: String =>
          Some(ApiInvoker.deserialize(s, "", classOf[jobStatusMap]).asInstanceOf[jobStatusMap])
        case _ => None
      }
    } catch {
      case ex: ApiException if ex.code == 404 => None
      case ex: ApiException => throw ex
    }
  }
  def getJobsMapHead(Authorization: String, Accept: String): Option[jobStatusMap] = {
    // create path and map variables
    val path = "/jobs".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

    headerParams += "Authorization" -> Authorization
    headerParams += "Accept" -> Accept
    try {
      apiInvoker.invokeApi(basePath, path, "HEAD", queryParams.toMap, None, headerParams.toMap, contentType) match {
        case s: String =>
          Some(ApiInvoker.deserialize(s, "", classOf[jobStatusMap]).asInstanceOf[jobStatusMap])
        case _ => None
      }
    } catch {
      case ex: ApiException if ex.code == 404 => None
      case ex: ApiException => throw ex
    }
  }
  def postJob(Authorization: String, Accept: String, body: String) = {
    // create path and map variables
    val path = "/jobs".replaceAll("\\{format\\}", "json")
    val contentType = {
      if (body != null && body.isInstanceOf[File])
        "multipart/form-data"
      else "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

    headerParams += "Authorization" -> Authorization
    headerParams += "Accept" -> Accept
    try {
      apiInvoker.invokeApi(basePath, path, "POST", queryParams.toMap, body, headerParams.toMap, contentType) match {
        case s: String =>
        case _ => None
      }
    } catch {
      case ex: ApiException if ex.code == 404 => None
      case ex: ApiException => throw ex
    }
  }
  def getDeletedJobsCatalog(Authorization: String, Accept: String): Option[JobsCatalogResponse] = {
    // create path and map variables
    val path = "/jobs/deleted".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

    headerParams += "Authorization" -> Authorization
    headerParams += "Accept" -> Accept
    try {
      apiInvoker.invokeApi(basePath, path, "GET", queryParams.toMap, None, headerParams.toMap, contentType) match {
        case s: String =>
          Some(ApiInvoker.deserialize(s, "", classOf[JobsCatalogResponse]).asInstanceOf[JobsCatalogResponse])
        case _ => None
      }
    } catch {
      case ex: ApiException if ex.code == 404 => None
      case ex: ApiException => throw ex
    }
  }
  def getDeletedJobsCatalogHead(Authorization: String, Accept: String): Option[JobsCatalogResponse] = {
    // create path and map variables
    val path = "/jobs/deleted".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

    headerParams += "Authorization" -> Authorization
    headerParams += "Accept" -> Accept
    try {
      apiInvoker.invokeApi(basePath, path, "HEAD", queryParams.toMap, None, headerParams.toMap, contentType) match {
        case s: String =>
          Some(ApiInvoker.deserialize(s, "", classOf[JobsCatalogResponse]).asInstanceOf[JobsCatalogResponse])
        case _ => None
      }
    } catch {
      case ex: ApiException if ex.code == 404 => None
      case ex: ApiException => throw ex
    }
  }
  def getFailedJobsCatalog(Authorization: String, Accept: String): Option[JobsCatalogResponse] = {
    // create path and map variables
    val path = "/jobs/failed".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

    headerParams += "Authorization" -> Authorization
    headerParams += "Accept" -> Accept
    try {
      apiInvoker.invokeApi(basePath, path, "GET", queryParams.toMap, None, headerParams.toMap, contentType) match {
        case s: String =>
          Some(ApiInvoker.deserialize(s, "", classOf[JobsCatalogResponse]).asInstanceOf[JobsCatalogResponse])
        case _ => None
      }
    } catch {
      case ex: ApiException if ex.code == 404 => None
      case ex: ApiException => throw ex
    }
  }
  def getFailedJobsCatalogHead(Authorization: String, Accept: String): Option[JobsCatalogResponse] = {
    // create path and map variables
    val path = "/jobs/failed".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

    headerParams += "Authorization" -> Authorization
    headerParams += "Accept" -> Accept
    try {
      apiInvoker.invokeApi(basePath, path, "HEAD", queryParams.toMap, None, headerParams.toMap, contentType) match {
        case s: String =>
          Some(ApiInvoker.deserialize(s, "", classOf[JobsCatalogResponse]).asInstanceOf[JobsCatalogResponse])
        case _ => None
      }
    } catch {
      case ex: ApiException if ex.code == 404 => None
      case ex: ApiException => throw ex
    }
  }
  def purgeExpired(Authorization: String, Accept: String) = {
    // create path and map variables
    val path = "/jobs/expired".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

    headerParams += "Authorization" -> Authorization
    headerParams += "Accept" -> Accept
    try {
      apiInvoker.invokeApi(basePath, path, "DELETE", queryParams.toMap, None, headerParams.toMap, contentType) match {
        case s: String =>
        case _ => None
      }
    } catch {
      case ex: ApiException if ex.code == 404 => None
      case ex: ApiException => throw ex
    }
  }
  def getExpiredJobsCatalog(Authorization: String, Accept: String): Option[JobsCatalogResponse] = {
    // create path and map variables
    val path = "/jobs/expired".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

    headerParams += "Authorization" -> Authorization
    headerParams += "Accept" -> Accept
    try {
      apiInvoker.invokeApi(basePath, path, "GET", queryParams.toMap, None, headerParams.toMap, contentType) match {
        case s: String =>
          Some(ApiInvoker.deserialize(s, "", classOf[JobsCatalogResponse]).asInstanceOf[JobsCatalogResponse])
        case _ => None
      }
    } catch {
      case ex: ApiException if ex.code == 404 => None
      case ex: ApiException => throw ex
    }
  }
  def getExpiredJobsCatalogHead(Authorization: String, Accept: String): Option[JobsCatalogResponse] = {
    // create path and map variables
    val path = "/jobs/expired".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

    headerParams += "Authorization" -> Authorization
    headerParams += "Accept" -> Accept
    try {
      apiInvoker.invokeApi(basePath, path, "HEAD", queryParams.toMap, None, headerParams.toMap, contentType) match {
        case s: String =>
          Some(ApiInvoker.deserialize(s, "", classOf[JobsCatalogResponse]).asInstanceOf[JobsCatalogResponse])
        case _ => None
      }
    } catch {
      case ex: ApiException if ex.code == 404 => None
      case ex: ApiException => throw ex
    }
  }
  def getCompleteJobsCatalog(Authorization: String, Accept: String): Option[JobsCatalogResponse] = {
    // create path and map variables
    val path = "/jobs/complete".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

    headerParams += "Authorization" -> Authorization
    headerParams += "Accept" -> Accept
    try {
      apiInvoker.invokeApi(basePath, path, "GET", queryParams.toMap, None, headerParams.toMap, contentType) match {
        case s: String =>
          Some(ApiInvoker.deserialize(s, "", classOf[JobsCatalogResponse]).asInstanceOf[JobsCatalogResponse])
        case _ => None
      }
    } catch {
      case ex: ApiException if ex.code == 404 => None
      case ex: ApiException => throw ex
    }
  }
  def getCompleteJobsCatalogHead(Authorization: String, Accept: String): Option[JobsCatalogResponse] = {
    // create path and map variables
    val path = "/jobs/complete".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

    headerParams += "Authorization" -> Authorization
    headerParams += "Accept" -> Accept
    try {
      apiInvoker.invokeApi(basePath, path, "HEAD", queryParams.toMap, None, headerParams.toMap, contentType) match {
        case s: String =>
          Some(ApiInvoker.deserialize(s, "", classOf[JobsCatalogResponse]).asInstanceOf[JobsCatalogResponse])
        case _ => None
      }
    } catch {
      case ex: ApiException if ex.code == 404 => None
      case ex: ApiException => throw ex
    }
  }
  def getJobsCatalog(Authorization: String, Accept: String): Option[JobsCatalogResponse] = {
    // create path and map variables
    val path = "/jobs/catalog".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

    headerParams += "Authorization" -> Authorization
    headerParams += "Accept" -> Accept
    try {
      apiInvoker.invokeApi(basePath, path, "GET", queryParams.toMap, None, headerParams.toMap, contentType) match {
        case s: String =>
          Some(ApiInvoker.deserialize(s, "", classOf[JobsCatalogResponse]).asInstanceOf[JobsCatalogResponse])
        case _ => None
      }
    } catch {
      case ex: ApiException if ex.code == 404 => None
      case ex: ApiException => throw ex
    }
  }
  def getJobsCatalogHead(Authorization: String, Accept: String): Option[JobsCatalogResponse] = {
    // create path and map variables
    val path = "/jobs/catalog".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

    headerParams += "Authorization" -> Authorization
    headerParams += "Accept" -> Accept
    try {
      apiInvoker.invokeApi(basePath, path, "HEAD", queryParams.toMap, None, headerParams.toMap, contentType) match {
        case s: String =>
          Some(ApiInvoker.deserialize(s, "", classOf[JobsCatalogResponse]).asInstanceOf[JobsCatalogResponse])
        case _ => None
      }
    } catch {
      case ex: ApiException if ex.code == 404 => None
      case ex: ApiException => throw ex
    }
  }
  def getJobEntity(job_id: String, Authorization: String, Accept: String): Option[JobEntity] = {
    // create path and map variables
    val path = "/jobs/{job_id}".replaceAll("\\{format\\}", "json").replaceAll("\\{" + "job_id" + "\\}", apiInvoker.escapeString(job_id))

    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

    // verify required params are set
    (Set(job_id) - null).size match {
      case 1 => // all required values set
      case _ => throw new Exception("missing required params")
    }
    headerParams += "Authorization" -> Authorization
    headerParams += "Accept" -> Accept
    try {
      apiInvoker.invokeApi(basePath, path, "GET", queryParams.toMap, None, headerParams.toMap, contentType) match {
        case s: String =>
          Some(ApiInvoker.deserialize(s, "", classOf[JobEntity]).asInstanceOf[JobEntity])
        case _ => None
      }
    } catch {
      case ex: ApiException if ex.code == 404 => None
      case ex: ApiException => throw ex
    }
  }
  def getJobEntityHead(job_id: String, Authorization: String, Accept: String): Option[JobEntity] = {
    // create path and map variables
    val path = "/jobs/{job_id}".replaceAll("\\{format\\}", "json").replaceAll("\\{" + "job_id" + "\\}", apiInvoker.escapeString(job_id))

    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

    // verify required params are set
    (Set(job_id) - null).size match {
      case 1 => // all required values set
      case _ => throw new Exception("missing required params")
    }
    headerParams += "Authorization" -> Authorization
    headerParams += "Accept" -> Accept
    try {
      apiInvoker.invokeApi(basePath, path, "HEAD", queryParams.toMap, None, headerParams.toMap, contentType) match {
        case s: String =>
          Some(ApiInvoker.deserialize(s, "", classOf[JobEntity]).asInstanceOf[JobEntity])
        case _ => None
      }
    } catch {
      case ex: ApiException if ex.code == 404 => None
      case ex: ApiException => throw ex
    }
  }
  def getJobResult(job_id: String, Authorization: String, Accept: String): Option[byte] = {
    // create path and map variables
    val path = "/jobs/{job_id}/result".replaceAll("\\{format\\}", "json").replaceAll("\\{" + "job_id" + "\\}", apiInvoker.escapeString(job_id))

    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

    // verify required params are set
    (Set(job_id) - null).size match {
      case 1 => // all required values set
      case _ => throw new Exception("missing required params")
    }
    headerParams += "Authorization" -> Authorization
    headerParams += "Accept" -> Accept
    try {
      apiInvoker.invokeApi(basePath, path, "GET", queryParams.toMap, None, headerParams.toMap, contentType) match {
        case s: String =>
          Some(ApiInvoker.deserialize(s, "", classOf[byte]).asInstanceOf[byte])
        case _ => None
      }
    } catch {
      case ex: ApiException if ex.code == 404 => None
      case ex: ApiException => throw ex
    }
  }
  def getJobResultHead(job_id: String, Authorization: String): Option[byte] = {
    // create path and map variables
    val path = "/jobs/{job_id}/result".replaceAll("\\{format\\}", "json").replaceAll("\\{" + "job_id" + "\\}", apiInvoker.escapeString(job_id))

    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

    // verify required params are set
    (Set(job_id) - null).size match {
      case 1 => // all required values set
      case _ => throw new Exception("missing required params")
    }
    headerParams += "Authorization" -> Authorization
    try {
      apiInvoker.invokeApi(basePath, path, "HEAD", queryParams.toMap, None, headerParams.toMap, contentType) match {
        case s: String =>
          Some(ApiInvoker.deserialize(s, "", classOf[byte]).asInstanceOf[byte])
        case _ => None
      }
    } catch {
      case ex: ApiException if ex.code == 404 => None
      case ex: ApiException => throw ex
    }
  }
  def deleteReport(job_id: String, Authorization: String, Accept: String): Option[DeleteResponse] = {
    // create path and map variables
    val path = "/jobs/{job_id}/result".replaceAll("\\{format\\}", "json").replaceAll("\\{" + "job_id" + "\\}", apiInvoker.escapeString(job_id))

    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

    // verify required params are set
    (Set(job_id) - null).size match {
      case 1 => // all required values set
      case _ => throw new Exception("missing required params")
    }
    headerParams += "Authorization" -> Authorization
    headerParams += "Accept" -> Accept
    try {
      apiInvoker.invokeApi(basePath, path, "DELETE", queryParams.toMap, None, headerParams.toMap, contentType) match {
        case s: String =>
          Some(ApiInvoker.deserialize(s, "", classOf[DeleteResponse]).asInstanceOf[DeleteResponse])
        case _ => None
      }
    } catch {
      case ex: ApiException if ex.code == 404 => None
      case ex: ApiException => throw ex
    }
  }
  def getResultDetail(job_id: String, Authorization: String, Accept: String): Option[ReportEntity] = {
    // create path and map variables
    val path = "/jobs/{job_id}/result/detail".replaceAll("\\{format\\}", "json").replaceAll("\\{" + "job_id" + "\\}", apiInvoker.escapeString(job_id))

    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

    // verify required params are set
    (Set(job_id) - null).size match {
      case 1 => // all required values set
      case _ => throw new Exception("missing required params")
    }
    headerParams += "Authorization" -> Authorization
    headerParams += "Accept" -> Accept
    try {
      apiInvoker.invokeApi(basePath, path, "GET", queryParams.toMap, None, headerParams.toMap, contentType) match {
        case s: String =>
          Some(ApiInvoker.deserialize(s, "", classOf[ReportEntity]).asInstanceOf[ReportEntity])
        case _ => None
      }
    } catch {
      case ex: ApiException if ex.code == 404 => None
      case ex: ApiException => throw ex
    }
  }
  def getResultDetailHead(job_id: String, Authorization: String, Accept: String): Option[ReportEntity] = {
    // create path and map variables
    val path = "/jobs/{job_id}/result/detail".replaceAll("\\{format\\}", "json").replaceAll("\\{" + "job_id" + "\\}", apiInvoker.escapeString(job_id))

    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

    // verify required params are set
    (Set(job_id) - null).size match {
      case 1 => // all required values set
      case _ => throw new Exception("missing required params")
    }
    headerParams += "Authorization" -> Authorization
    headerParams += "Accept" -> Accept
    try {
      apiInvoker.invokeApi(basePath, path, "HEAD", queryParams.toMap, None, headerParams.toMap, contentType) match {
        case s: String =>
          Some(ApiInvoker.deserialize(s, "", classOf[ReportEntity]).asInstanceOf[ReportEntity])
        case _ => None
      }
    } catch {
      case ex: ApiException if ex.code == 404 => None
      case ex: ApiException => throw ex
    }
  }
  def getJobStatus(job_id: String, Authorization: String, Accept: String): Option[StatusResponse] = {
    // create path and map variables
    val path = "/jobs/{job_id}/status".replaceAll("\\{format\\}", "json").replaceAll("\\{" + "job_id" + "\\}", apiInvoker.escapeString(job_id))

    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

    // verify required params are set
    (Set(job_id) - null).size match {
      case 1 => // all required values set
      case _ => throw new Exception("missing required params")
    }
    headerParams += "Authorization" -> Authorization
    headerParams += "Accept" -> Accept
    try {
      apiInvoker.invokeApi(basePath, path, "GET", queryParams.toMap, None, headerParams.toMap, contentType) match {
        case s: String =>
          Some(ApiInvoker.deserialize(s, "", classOf[StatusResponse]).asInstanceOf[StatusResponse])
        case _ => None
      }
    } catch {
      case ex: ApiException if ex.code == 404 => None
      case ex: ApiException => throw ex
    }
  }
  def getJobStatusHead(job_id: String, Authorization: String, Accept: String): Option[StatusResponse] = {
    // create path and map variables
    val path = "/jobs/{job_id}/status".replaceAll("\\{format\\}", "json").replaceAll("\\{" + "job_id" + "\\}", apiInvoker.escapeString(job_id))

    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

    // verify required params are set
    (Set(job_id) - null).size match {
      case 1 => // all required values set
      case _ => throw new Exception("missing required params")
    }
    headerParams += "Authorization" -> Authorization
    headerParams += "Accept" -> Accept
    try {
      apiInvoker.invokeApi(basePath, path, "HEAD", queryParams.toMap, None, headerParams.toMap, contentType) match {
        case s: String =>
          Some(ApiInvoker.deserialize(s, "", classOf[StatusResponse]).asInstanceOf[StatusResponse])
        case _ => None
      }
    } catch {
      case ex: ApiException if ex.code == 404 => None
      case ex: ApiException => throw ex
    }
  }
}

