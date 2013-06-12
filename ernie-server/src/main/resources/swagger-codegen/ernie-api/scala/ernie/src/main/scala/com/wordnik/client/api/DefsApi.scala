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

  def getDefinition(Authorization: String, Accept: String): Option[ReportDefinitionMapResponse] = {
    // create path and map variables
    val path = "/defs".replaceAll("\\{format\\}", "json")
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
          Some(ApiInvoker.deserialize(s, "", classOf[ReportDefinitionMapResponse]).asInstanceOf[ReportDefinitionMapResponse])
        case _ => None
      }
    } catch {
      case ex: ApiException if ex.code == 404 => None
      case ex: ApiException => throw ex
    }
  }
  def getDefinitionHead(Authorization: String, Accept: String): Option[ReportDefinitionMapResponse] = {
    // create path and map variables
    val path = "/defs".replaceAll("\\{format\\}", "json")
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
          Some(ApiInvoker.deserialize(s, "", classOf[ReportDefinitionMapResponse]).asInstanceOf[ReportDefinitionMapResponse])
        case _ => None
      }
    } catch {
      case ex: ApiException if ex.code == 404 => None
      case ex: ApiException => throw ex
    }
  }
  def postDefinition(Authorization: String, Accept: String, body: String): Option[byte] = {
    // create path and map variables
    val path = "/defs".replaceAll("\\{format\\}", "json")
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
          Some(ApiInvoker.deserialize(s, "", classOf[byte]).asInstanceOf[byte])
        case _ => None
      }
    } catch {
      case ex: ApiException if ex.code == 404 => None
      case ex: ApiException => throw ex
    }
  }
  def getDefinitionDetail(def_id: String, Authorization: String, Accept: String): Option[DefinitionEntity] = {
    // create path and map variables
    val path = "/defs/{def_id}".replaceAll("\\{format\\}", "json").replaceAll("\\{" + "def_id" + "\\}", apiInvoker.escapeString(def_id))

    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

    // verify required params are set
    (Set(def_id) - null).size match {
      case 1 => // all required values set
      case _ => throw new Exception("missing required params")
    }
    headerParams += "Authorization" -> Authorization
    headerParams += "Accept" -> Accept
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
  def getDefinitionDetailHead(def_id: String, Authorization: String, Accept: String): Option[DefinitionEntity] = {
    // create path and map variables
    val path = "/defs/{def_id}".replaceAll("\\{format\\}", "json").replaceAll("\\{" + "def_id" + "\\}", apiInvoker.escapeString(def_id))

    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

    // verify required params are set
    (Set(def_id) - null).size match {
      case 1 => // all required values set
      case _ => throw new Exception("missing required params")
    }
    headerParams += "Authorization" -> Authorization
    headerParams += "Accept" -> Accept
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
  def deleteDefinition(def_id: String, Authorization: String, Accept: String): Option[DefinitionDeleteResponse] = {
    // create path and map variables
    val path = "/defs/{def_id}".replaceAll("\\{format\\}", "json").replaceAll("\\{" + "def_id" + "\\}", apiInvoker.escapeString(def_id))

    val contentType = {
      "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

    // verify required params are set
    (Set(def_id) - null).size match {
      case 1 => // all required values set
      case _ => throw new Exception("missing required params")
    }
    headerParams += "Authorization" -> Authorization
    headerParams += "Accept" -> Accept
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
  def putDefinition(def_id: String, Authorization: String, Accept: String, body: String): Option[DefinitionEntity] = {
    // create path and map variables
    val path = "/defs/{def_id}/rptdesign".replaceAll("\\{format\\}", "json").replaceAll("\\{" + "def_id" + "\\}", apiInvoker.escapeString(def_id))

    val contentType = {
      if (body != null && body.isInstanceOf[File])
        "multipart/form-data"
      else "application/json"
    }

    // query params
    val queryParams = new HashMap[String, String]
    val headerParams = new HashMap[String, String]

    // verify required params are set
    (Set(def_id) - null).size match {
      case 1 => // all required values set
      case _ => throw new Exception("missing required params")
    }
    headerParams += "Authorization" -> Authorization
    headerParams += "Accept" -> Accept
    try {
      apiInvoker.invokeApi(basePath, path, "PUT", queryParams.toMap, body, headerParams.toMap, contentType) match {
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

