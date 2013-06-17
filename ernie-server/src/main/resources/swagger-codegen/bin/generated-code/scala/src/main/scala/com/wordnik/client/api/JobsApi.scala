package com.wordnik.client.api

import com.wordnik.client.model.Byte
import com.wordnik.client.model.JobsMapResponse
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

  def getJobsMap(): Option[JobsMapResponse] = {
    // create path and map variables
    val path = "/jobs".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

    try {
      apiInvoker.invokeApi(basePath, path, "GET", queryParams.toMap, None, headerParams.toMap, contentType) match {
        case s: String =>
          Some(ApiInvoker.deserialize(s, "", classOf[JobsMapResponse]).asInstanceOf[JobsMapResponse])
        case _ => None
      }
    } catch {
      case ex: ApiException if ex.code == 404 => None
      case ex: ApiException => throw ex
    }
  }
  def getJobsMap(): Option[JobsMapResponse] = {
    // create path and map variables
    val path = "/jobs".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

    try {
      apiInvoker.invokeApi(basePath, path, "HEAD", queryParams.toMap, None, headerParams.toMap, contentType) match {
        case s: String =>
          Some(ApiInvoker.deserialize(s, "", classOf[JobsMapResponse]).asInstanceOf[JobsMapResponse])
        case _ => None
      }
    } catch {
      case ex: ApiException if ex.code == 404 => None
      case ex: ApiException => throw ex
    }
  }
  def postJob() = {
    // create path and map variables
    val path = "/jobs".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

    try {
      apiInvoker.invokeApi(basePath, path, "POST", queryParams.toMap, None, headerParams.toMap, contentType) match {
        case s: String =>
        case _ => None
      }
    } catch {
      case ex: ApiException if ex.code == 404 => None
      case ex: ApiException => throw ex
    }
  }
  def getDeletedJobsCatalog(): Option[JobsCatalogResponse] = {
    // create path and map variables
    val path = "/jobs/deleted".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

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
  def getDeletedJobsCatalog(): Option[JobsCatalogResponse] = {
    // create path and map variables
    val path = "/jobs/deleted".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

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
  def getFailedJobsCatalog(): Option[JobsCatalogResponse] = {
    // create path and map variables
    val path = "/jobs/failed".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

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
  def getFailedJobsCatalog(): Option[JobsCatalogResponse] = {
    // create path and map variables
    val path = "/jobs/failed".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

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
  def purgeExpired() = {
    // create path and map variables
    val path = "/jobs/expired".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

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
  def getExpiredJobsCatalog(): Option[JobsCatalogResponse] = {
    // create path and map variables
    val path = "/jobs/expired".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

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
  def getExpiredJobsCatalog(): Option[JobsCatalogResponse] = {
    // create path and map variables
    val path = "/jobs/expired".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

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
  def getCompleteJobsCatalog(): Option[JobsCatalogResponse] = {
    // create path and map variables
    val path = "/jobs/complete".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

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
  def getCompleteJobsCatalog(): Option[JobsCatalogResponse] = {
    // create path and map variables
    val path = "/jobs/complete".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

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
  def getJobsCatalog(): Option[JobsCatalogResponse] = {
    // create path and map variables
    val path = "/jobs/catalog".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

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
  def getJobsCatalog(): Option[JobsCatalogResponse] = {
    // create path and map variables
    val path = "/jobs/catalog".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

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
  def getJobEntity(): Option[JobEntity] = {
    // create path and map variables
    val path = "/jobs/{job}".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

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
  def getJobEntity(): Option[JobEntity] = {
    // create path and map variables
    val path = "/jobs/{job}".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

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
  def getJobResult(): Option[byte] = {
    // create path and map variables
    val path = "/jobs/{job}/result".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

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
  def getJobResult(): Option[byte] = {
    // create path and map variables
    val path = "/jobs/{job}/result".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

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
  def deleteReport(): Option[DeleteResponse] = {
    // create path and map variables
    val path = "/jobs/{job}/result".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

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
  def getResultDetail(): Option[ReportEntity] = {
    // create path and map variables
    val path = "/jobs/{job}/result/detail".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

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
  def getResultDetail(): Option[ReportEntity] = {
    // create path and map variables
    val path = "/jobs/{job}/result/detail".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

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
  def getJobStatus(): Option[StatusResponse] = {
    // create path and map variables
    val path = "/jobs/{job}/status".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

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
  def getJobStatus(): Option[StatusResponse] = {
    // create path and map variables
    val path = "/jobs/{job}/status".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

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

