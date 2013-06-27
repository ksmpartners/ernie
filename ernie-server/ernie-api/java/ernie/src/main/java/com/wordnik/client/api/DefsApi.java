package com.wordnik.client.api;

import com.wordnik.client.common.ApiException;
import com.wordnik.client.common.ApiInvoker;
import com.wordnik.client.model.DefinitionEntity;
import com.wordnik.client.model.ReportDefinitionMapResponse;
import com.wordnik.client.model.DefinitionDeleteResponse;
import java.util.*;

public class DefsApi {
  String basePath = "http://localhost:8080";
  ApiInvoker apiInvoker = ApiInvoker.getInstance();

  public ApiInvoker getInvoker() {
    return apiInvoker;
  }
  
  public void setBasePath(String basePath) {
    this.basePath = basePath;
  }
  
  public String getBasePath() {
    return basePath;
  }

  public ReportDefinitionMapResponse getDefinition (String Authorization, String Accept) throws ApiException {
    // create path and map variables
    String path = "/defs".replaceAll("\\{format\\}","json");

    // query params
    Map<String, String> queryParams = new HashMap<String, String>();
    Map<String, String> headerParams = new HashMap<String, String>();

    headerParams.put("Authorization", Authorization);
    headerParams.put("Accept", Accept);
    try {
      String response = apiInvoker.invokeAPI(basePath, path, "GET", queryParams, null, headerParams);
      if(response != null){
        return (ReportDefinitionMapResponse) ApiInvoker.deserialize(response, "", ReportDefinitionMapResponse.class);
      }
      else {
        return null;
      }
    } catch (ApiException ex) {
      if(ex.getCode() == 404) {
      	return null;
      }
      else {
        throw ex;
      }
    }
  }
  public void getDefinitionHead (String Authorization, String Accept) throws ApiException {
    // create path and map variables
    String path = "/defs".replaceAll("\\{format\\}","json");

    // query params
    Map<String, String> queryParams = new HashMap<String, String>();
    Map<String, String> headerParams = new HashMap<String, String>();

    headerParams.put("Authorization", Authorization);
    headerParams.put("Accept", Accept);
    try {
      String response = apiInvoker.invokeAPI(basePath, path, "HEAD", queryParams, null, headerParams);
      if(response != null){
        return ;
      }
      else {
        return ;
      }
    } catch (ApiException ex) {
      if(ex.getCode() == 404) {
      	return ;
      }
      else {
        throw ex;
      }
    }
  }
  public DefinitionEntity postDefinition (String Authorization, String Accept, String body) throws ApiException {
    // create path and map variables
    String path = "/defs".replaceAll("\\{format\\}","json");

    // query params
    Map<String, String> queryParams = new HashMap<String, String>();
    Map<String, String> headerParams = new HashMap<String, String>();

    headerParams.put("Authorization", Authorization);
    headerParams.put("Accept", Accept);
    try {
      String response = apiInvoker.invokeAPI(basePath, path, "POST", queryParams, body, headerParams);
      if(response != null){
        return (DefinitionEntity) ApiInvoker.deserialize(response, "", DefinitionEntity.class);
      }
      else {
        return null;
      }
    } catch (ApiException ex) {
      if(ex.getCode() == 404) {
      	return null;
      }
      else {
        throw ex;
      }
    }
  }
  public DefinitionEntity getDefinitionDetail (String def_id, String Authorization, String Accept) throws ApiException {
    // create path and map variables
    String path = "/defs/{def_id}".replaceAll("\\{format\\}","json").replaceAll("\\{" + "def_id" + "\\}", apiInvoker.escapeString(def_id.toString()));

    // query params
    Map<String, String> queryParams = new HashMap<String, String>();
    Map<String, String> headerParams = new HashMap<String, String>();

    // verify required params are set
    if(def_id == null ) {
       throw new ApiException(400, "missing required params");
    }
    headerParams.put("Authorization", Authorization);
    headerParams.put("Accept", Accept);
    try {
      String response = apiInvoker.invokeAPI(basePath, path, "GET", queryParams, null, headerParams);
      if(response != null){
        return (DefinitionEntity) ApiInvoker.deserialize(response, "", DefinitionEntity.class);
      }
      else {
        return null;
      }
    } catch (ApiException ex) {
      if(ex.getCode() == 404) {
      	return null;
      }
      else {
        throw ex;
      }
    }
  }
  public void getDefinitionDetailHead (String def_id, String Authorization, String Accept) throws ApiException {
    // create path and map variables
    String path = "/defs/{def_id}".replaceAll("\\{format\\}","json").replaceAll("\\{" + "def_id" + "\\}", apiInvoker.escapeString(def_id.toString()));

    // query params
    Map<String, String> queryParams = new HashMap<String, String>();
    Map<String, String> headerParams = new HashMap<String, String>();

    // verify required params are set
    if(def_id == null ) {
       throw new ApiException(400, "missing required params");
    }
    headerParams.put("Authorization", Authorization);
    headerParams.put("Accept", Accept);
    try {
      String response = apiInvoker.invokeAPI(basePath, path, "HEAD", queryParams, null, headerParams);
      if(response != null){
        return ;
      }
      else {
        return ;
      }
    } catch (ApiException ex) {
      if(ex.getCode() == 404) {
      	return ;
      }
      else {
        throw ex;
      }
    }
  }
  public DefinitionDeleteResponse deleteDefinition (String def_id, String Authorization, String Accept) throws ApiException {
    // create path and map variables
    String path = "/defs/{def_id}".replaceAll("\\{format\\}","json").replaceAll("\\{" + "def_id" + "\\}", apiInvoker.escapeString(def_id.toString()));

    // query params
    Map<String, String> queryParams = new HashMap<String, String>();
    Map<String, String> headerParams = new HashMap<String, String>();

    // verify required params are set
    if(def_id == null ) {
       throw new ApiException(400, "missing required params");
    }
    headerParams.put("Authorization", Authorization);
    headerParams.put("Accept", Accept);
    try {
      String response = apiInvoker.invokeAPI(basePath, path, "DELETE", queryParams, null, headerParams);
      if(response != null){
        return (DefinitionDeleteResponse) ApiInvoker.deserialize(response, "", DefinitionDeleteResponse.class);
      }
      else {
        return null;
      }
    } catch (ApiException ex) {
      if(ex.getCode() == 404) {
      	return null;
      }
      else {
        throw ex;
      }
    }
  }
  public DefinitionEntity putDefinition (String def_id, String Authorization, String Accept, String body) throws ApiException {
    // create path and map variables
    String path = "/defs/{def_id}/rptdesign".replaceAll("\\{format\\}","json").replaceAll("\\{" + "def_id" + "\\}", apiInvoker.escapeString(def_id.toString()));

    // query params
    Map<String, String> queryParams = new HashMap<String, String>();
    Map<String, String> headerParams = new HashMap<String, String>();

    // verify required params are set
    if(def_id == null ) {
       throw new ApiException(400, "missing required params");
    }
    headerParams.put("Authorization", Authorization);
    headerParams.put("Accept", Accept);
    try {
      String response = apiInvoker.invokeAPI(basePath, path, "PUT", queryParams, body, headerParams);
      if(response != null){
        return (DefinitionEntity) ApiInvoker.deserialize(response, "", DefinitionEntity.class);
      }
      else {
        return null;
      }
    } catch (ApiException ex) {
      if(ex.getCode() == 404) {
      	return null;
      }
      else {
        throw ex;
      }
    }
  }
  }

