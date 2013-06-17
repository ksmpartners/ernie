package com.wordnik.client.api

import com.wordnik.client.model.Byte
import com.wordnik.client.model.ReportDefinitionMapResponse
import com.wordnik.client.model.DefinitionEntity
import com.wordnik.client.model.DefinitionDeleteResponse
import com.wordnik.client.common.ApiInvoker
import com.wordnik.client.common.ApiException

import java.io.File

import scala.collection.mutable.HashMap

class DefsApi {
  var basePath: String = "http://localhost:8080"
  var apiInvoker = ApiInvoker

  def addHeader(key: String, value: String) = apiInvoker.defaultHeaders += key -> value

  def getDefinition(): Option[ReportDefinitionMapResponse] = {
    // create path and map variables
    val path = "/defs".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

    try {
      apiInvoker.invokeApi(basePath, path, "GET", queryParams.toMap, None, headerParams.toMap, contentType) match {
        case s: String =>
          Some(ApiInvoker.deserialize(s, "", classOf[ReportDefinitionMapResponse]).asInstanceOf[ReportDefinitionMapResponse])
        case _ => None
      }
    } catch {
      case ex: ApiException if ex.code == 404 => None
      case ex: ApiException => throw ex
    }
  }
  def getDefinition(): Option[ReportDefinitionMapResponse] = {
    // create path and map variables
    val path = "/defs".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

    try {
      apiInvoker.invokeApi(basePath, path, "HEAD", queryParams.toMap, None, headerParams.toMap, contentType) match {
        case s: String =>
          Some(ApiInvoker.deserialize(s, "", classOf[ReportDefinitionMapResponse]).asInstanceOf[ReportDefinitionMapResponse])
        case _ => None
      }
    } catch {
      case ex: ApiException if ex.code == 404 => None
      case ex: ApiException => throw ex
    }
  }
  def postDefinition(): Option[byte] = {
    // create path and map variables
    val path = "/defs".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

    try {
      apiInvoker.invokeApi(basePath, path, "POST", queryParams.toMap, None, headerParams.toMap, contentType) match {
        case s: String =>
          Some(ApiInvoker.deserialize(s, "", classOf[byte]).asInstanceOf[byte])
        case _ => None
      }
    } catch {
      case ex: ApiException if ex.code == 404 => None
      case ex: ApiException => throw ex
    }
  }
  def getDefinitionDetail(): Option[DefinitionEntity] = {
    // create path and map variables
    val path = "/defs/{def_id}".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

    try {
      apiInvoker.invokeApi(basePath, path, "GET", queryParams.toMap, None, headerParams.toMap, contentType) match {
        case s: String =>
          Some(ApiInvoker.deserialize(s, "", classOf[DefinitionEntity]).asInstanceOf[DefinitionEntity])
        case _ => None
      }
    } catch {
      case ex: ApiException if ex.code == 404 => None
      case ex: ApiException => throw ex
    }
  }
  def getDefinitionDetail(): Option[DefinitionEntity] = {
    // create path and map variables
    val path = "/defs/{def_id}".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

    try {
      apiInvoker.invokeApi(basePath, path, "HEAD", queryParams.toMap, None, headerParams.toMap, contentType) match {
        case s: String =>
          Some(ApiInvoker.deserialize(s, "", classOf[DefinitionEntity]).asInstanceOf[DefinitionEntity])
        case _ => None
      }
    } catch {
      case ex: ApiException if ex.code == 404 => None
      case ex: ApiException => throw ex
    }
  }
  def deleteDefinition(): Option[DefinitionDeleteResponse] = {
    // create path and map variables
    val path = "/defs/{def_id}".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

    try {
      apiInvoker.invokeApi(basePath, path, "DELETE", queryParams.toMap, None, headerParams.toMap, contentType) match {
        case s: String =>
          Some(ApiInvoker.deserialize(s, "", classOf[DefinitionDeleteResponse]).asInstanceOf[DefinitionDeleteResponse])
        case _ => None
      }
    } catch {
      case ex: ApiException if ex.code == 404 => None
      case ex: ApiException => throw ex
    }
  }
  def putDefinition(): Option[DefinitionEntity] = {
    // create path and map variables
    val path = "/defs/{def_id}/rptdesign".replaceAll("\\{format\\}", "json")
    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

    try {
      apiInvoker.invokeApi(basePath, path, "PUT", queryParams.toMap, None, headerParams.toMap, contentType) match {
        case s: String =>
          Some(ApiInvoker.deserialize(s, "", classOf[DefinitionEntity]).asInstanceOf[DefinitionEntity])
        case _ => None
      }
    } catch {
      case ex: ApiException if ex.code == 404 => None
      case ex: ApiException => throw ex
    }
  }
}

