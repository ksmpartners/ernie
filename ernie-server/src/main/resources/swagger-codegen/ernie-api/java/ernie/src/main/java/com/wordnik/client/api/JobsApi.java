package com.wordnik.client.api;

import com.wordnik.client.common.ApiException;
import com.wordnik.client.common.ApiInvoker;
import com.wordnik.client.model.Byte;
import com.wordnik.client.model.JobStatusMap;
import com.wordnik.client.model.JobsCatalogResponse;
import com.wordnik.client.model.DeleteResponse;
import com.wordnik.client.model.ReportEntity;
import com.wordnik.client.model.JobEntity;
import com.wordnik.client.model.StatusResponse;
import java.util.*;

public class JobsApi {
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

  public jobStatusMap getJobsMap (String Authorization, String Accept) throws ApiException {
    // create path and map variables
    String path = "/jobs".replaceAll("\\{format\\}","json");

    // query params
    Map<String, String> queryParams = new HashMap<String, String>();
    Map<String, String> headerParams = new HashMap<String, String>();

    headerParams.put("Authorization", Authorization);
    headerParams.put("Accept", Accept);
    try {
      String response = apiInvoker.invokeAPI(basePath, path, "GET", queryParams, null, headerParams);
      if(response != null){
        return (jobStatusMap) ApiInvoker.deserialize(response, "", jobStatusMap.class);
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
  public jobStatusMap getJobsMapHead (String Authorization, String Accept) throws ApiException {
    // create path and map variables
    String path = "/jobs".replaceAll("\\{format\\}","json");

    // query params
    Map<String, String> queryParams = new HashMap<String, String>();
    Map<String, String> headerParams = new HashMap<String, String>();

    headerParams.put("Authorization", Authorization);
    headerParams.put("Accept", Accept);
    try {
      String response = apiInvoker.invokeAPI(basePath, path, "HEAD", queryParams, null, headerParams);
      if(response != null){
        return (jobStatusMap) ApiInvoker.deserialize(response, "", jobStatusMap.class);
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
  public void postJob (String Authorization, String Accept, String body) throws ApiException {
    // create path and map variables
    String path = "/jobs".replaceAll("\\{format\\}","json");

    // query params
    Map<String, String> queryParams = new HashMap<String, String>();
    Map<String, String> headerParams = new HashMap<String, String>();

    headerParams.put("Authorization", Authorization);
    headerParams.put("Accept", Accept);
    try {
      String response = apiInvoker.invokeAPI(basePath, path, "POST", queryParams, body, headerParams);
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
  public JobsCatalogResponse getDeletedJobsCatalog (String Authorization, String Accept) throws ApiException {
    // create path and map variables
    String path = "/jobs/deleted".replaceAll("\\{format\\}","json");

    // query params
    Map<String, String> queryParams = new HashMap<String, String>();
    Map<String, String> headerParams = new HashMap<String, String>();

    headerParams.put("Authorization", Authorization);
    headerParams.put("Accept", Accept);
    try {
      String response = apiInvoker.invokeAPI(basePath, path, "GET", queryParams, null, headerParams);
      if(response != null){
        return (JobsCatalogResponse) ApiInvoker.deserialize(response, "", JobsCatalogResponse.class);
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
  public JobsCatalogResponse getDeletedJobsCatalogHead (String Authorization, String Accept) throws ApiException {
    // create path and map variables
    String path = "/jobs/deleted".replaceAll("\\{format\\}","json");

    // query params
    Map<String, String> queryParams = new HashMap<String, String>();
    Map<String, String> headerParams = new HashMap<String, String>();

    headerParams.put("Authorization", Authorization);
    headerParams.put("Accept", Accept);
    try {
      String response = apiInvoker.invokeAPI(basePath, path, "HEAD", queryParams, null, headerParams);
      if(response != null){
        return (JobsCatalogResponse) ApiInvoker.deserialize(response, "", JobsCatalogResponse.class);
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
  public JobsCatalogResponse getFailedJobsCatalog (String Authorization, String Accept) throws ApiException {
    // create path and map variables
    String path = "/jobs/failed".replaceAll("\\{format\\}","json");

    // query params
    Map<String, String> queryParams = new HashMap<String, String>();
    Map<String, String> headerParams = new HashMap<String, String>();

    headerParams.put("Authorization", Authorization);
    headerParams.put("Accept", Accept);
    try {
      String response = apiInvoker.invokeAPI(basePath, path, "GET", queryParams, null, headerParams);
      if(response != null){
        return (JobsCatalogResponse) ApiInvoker.deserialize(response, "", JobsCatalogResponse.class);
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
  public JobsCatalogResponse getFailedJobsCatalogHead (String Authorization, String Accept) throws ApiException {
    // create path and map variables
    String path = "/jobs/failed".replaceAll("\\{format\\}","json");

    // query params
    Map<String, String> queryParams = new HashMap<String, String>();
    Map<String, String> headerParams = new HashMap<String, String>();

    headerParams.put("Authorization", Authorization);
    headerParams.put("Accept", Accept);
    try {
      String response = apiInvoker.invokeAPI(basePath, path, "HEAD", queryParams, null, headerParams);
      if(response != null){
        return (JobsCatalogResponse) ApiInvoker.deserialize(response, "", JobsCatalogResponse.class);
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
  public void purgeExpired (String Authorization, String Accept) throws ApiException {
    // create path and map variables
    String path = "/jobs/expired".replaceAll("\\{format\\}","json");

    // query params
    Map<String, String> queryParams = new HashMap<String, String>();
    Map<String, String> headerParams = new HashMap<String, String>();

    headerParams.put("Authorization", Authorization);
    headerParams.put("Accept", Accept);
    try {
      String response = apiInvoker.invokeAPI(basePath, path, "DELETE", queryParams, null, headerParams);
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
  public JobsCatalogResponse getExpiredJobsCatalog (String Authorization, String Accept) throws ApiException {
    // create path and map variables
    String path = "/jobs/expired".replaceAll("\\{format\\}","json");

    // query params
    Map<String, String> queryParams = new HashMap<String, String>();
    Map<String, String> headerParams = new HashMap<String, String>();

    headerParams.put("Authorization", Authorization);
    headerParams.put("Accept", Accept);
    try {
      String response = apiInvoker.invokeAPI(basePath, path, "GET", queryParams, null, headerParams);
      if(response != null){
        return (JobsCatalogResponse) ApiInvoker.deserialize(response, "", JobsCatalogResponse.class);
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
  public JobsCatalogResponse getExpiredJobsCatalogHead (String Authorization, String Accept) throws ApiException {
    // create path and map variables
    String path = "/jobs/expired".replaceAll("\\{format\\}","json");

    // query params
    Map<String, String> queryParams = new HashMap<String, String>();
    Map<String, String> headerParams = new HashMap<String, String>();

    headerParams.put("Authorization", Authorization);
    headerParams.put("Accept", Accept);
    try {
      String response = apiInvoker.invokeAPI(basePath, path, "HEAD", queryParams, null, headerParams);
      if(response != null){
        return (JobsCatalogResponse) ApiInvoker.deserialize(response, "", JobsCatalogResponse.class);
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
  public JobsCatalogResponse getCompleteJobsCatalog (String Authorization, String Accept) throws ApiException {
    // create path and map variables
    String path = "/jobs/complete".replaceAll("\\{format\\}","json");

    // query params
    Map<String, String> queryParams = new HashMap<String, String>();
    Map<String, String> headerParams = new HashMap<String, String>();

    headerParams.put("Authorization", Authorization);
    headerParams.put("Accept", Accept);
    try {
      String response = apiInvoker.invokeAPI(basePath, path, "GET", queryParams, null, headerParams);
      if(response != null){
        return (JobsCatalogResponse) ApiInvoker.deserialize(response, "", JobsCatalogResponse.class);
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
  public JobsCatalogResponse getCompleteJobsCatalogHead (String Authorization, String Accept) throws ApiException {
    // create path and map variables
    String path = "/jobs/complete".replaceAll("\\{format\\}","json");

    // query params
    Map<String, String> queryParams = new HashMap<String, String>();
    Map<String, String> headerParams = new HashMap<String, String>();

    headerParams.put("Authorization", Authorization);
    headerParams.put("Accept", Accept);
    try {
      String response = apiInvoker.invokeAPI(basePath, path, "HEAD", queryParams, null, headerParams);
      if(response != null){
        return (JobsCatalogResponse) ApiInvoker.deserialize(response, "", JobsCatalogResponse.class);
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
  public JobsCatalogResponse getJobsCatalog (String Authorization, String Accept) throws ApiException {
    // create path and map variables
    String path = "/jobs/catalog".replaceAll("\\{format\\}","json");

    // query params
    Map<String, String> queryParams = new HashMap<String, String>();
    Map<String, String> headerParams = new HashMap<String, String>();

    headerParams.put("Authorization", Authorization);
    headerParams.put("Accept", Accept);
    try {
      String response = apiInvoker.invokeAPI(basePath, path, "GET", queryParams, null, headerParams);
      if(response != null){
        return (JobsCatalogResponse) ApiInvoker.deserialize(response, "", JobsCatalogResponse.class);
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
  public JobsCatalogResponse getJobsCatalogHead (String Authorization, String Accept) throws ApiException {
    // create path and map variables
    String path = "/jobs/catalog".replaceAll("\\{format\\}","json");

    // query params
    Map<String, String> queryParams = new HashMap<String, String>();
    Map<String, String> headerParams = new HashMap<String, String>();

    headerParams.put("Authorization", Authorization);
    headerParams.put("Accept", Accept);
    try {
      String response = apiInvoker.invokeAPI(basePath, path, "HEAD", queryParams, null, headerParams);
      if(response != null){
        return (JobsCatalogResponse) ApiInvoker.deserialize(response, "", JobsCatalogResponse.class);
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
  public JobEntity getJobEntity (String job_id, String Authorization, String Accept) throws ApiException {
    // create path and map variables
    String path = "/jobs/{job_id}".replaceAll("\\{format\\}","json").replaceAll("\\{" + "job_id" + "\\}", apiInvoker.escapeString(job_id.toString()));

    // query params
    Map<String, String> queryParams = new HashMap<String, String>();
    Map<String, String> headerParams = new HashMap<String, String>();

    // verify required params are set
    if(job_id == null ) {
       throw new ApiException(400, "missing required params");
    }
    headerParams.put("Authorization", Authorization);
    headerParams.put("Accept", Accept);
    try {
      String response = apiInvoker.invokeAPI(basePath, path, "GET", queryParams, null, headerParams);
      if(response != null){
        return (JobEntity) ApiInvoker.deserialize(response, "", JobEntity.class);
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
  public JobEntity getJobEntityHead (String job_id, String Authorization, String Accept) throws ApiException {
    // create path and map variables
    String path = "/jobs/{job_id}".replaceAll("\\{format\\}","json").replaceAll("\\{" + "job_id" + "\\}", apiInvoker.escapeString(job_id.toString()));

    // query params
    Map<String, String> queryParams = new HashMap<String, String>();
    Map<String, String> headerParams = new HashMap<String, String>();

    // verify required params are set
    if(job_id == null ) {
       throw new ApiException(400, "missing required params");
    }
    headerParams.put("Authorization", Authorization);
    headerParams.put("Accept", Accept);
    try {
      String response = apiInvoker.invokeAPI(basePath, path, "HEAD", queryParams, null, headerParams);
      if(response != null){
        return (JobEntity) ApiInvoker.deserialize(response, "", JobEntity.class);
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
  public byte getJobResult (String job_id, String Authorization) throws ApiException {
    // create path and map variables
    String path = "/jobs/{job_id}/result".replaceAll("\\{format\\}","json").replaceAll("\\{" + "job_id" + "\\}", apiInvoker.escapeString(job_id.toString()));

    // query params
    Map<String, String> queryParams = new HashMap<String, String>();
    Map<String, String> headerParams = new HashMap<String, String>();

    // verify required params are set
    if(job_id == null ) {
       throw new ApiException(400, "missing required params");
    }
    headerParams.put("Authorization", Authorization);
    try {
      String response = apiInvoker.invokeAPI(basePath, path, "GET", queryParams, null, headerParams);
      if(response != null){
        return (byte) ApiInvoker.deserialize(response, "", byte.class);
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
  public byte getJobResultHead (String job_id, String Authorization) throws ApiException {
    // create path and map variables
    String path = "/jobs/{job_id}/result".replaceAll("\\{format\\}","json").replaceAll("\\{" + "job_id" + "\\}", apiInvoker.escapeString(job_id.toString()));

    // query params
    Map<String, String> queryParams = new HashMap<String, String>();
    Map<String, String> headerParams = new HashMap<String, String>();

    // verify required params are set
    if(job_id == null ) {
       throw new ApiException(400, "missing required params");
    }
    headerParams.put("Authorization", Authorization);
    try {
      String response = apiInvoker.invokeAPI(basePath, path, "HEAD", queryParams, null, headerParams);
      if(response != null){
        return (byte) ApiInvoker.deserialize(response, "", byte.class);
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
  public DeleteResponse deleteReport (String job_id, String Authorization, String Accept) throws ApiException {
    // create path and map variables
    String path = "/jobs/{job_id}/result".replaceAll("\\{format\\}","json").replaceAll("\\{" + "job_id" + "\\}", apiInvoker.escapeString(job_id.toString()));

    // query params
    Map<String, String> queryParams = new HashMap<String, String>();
    Map<String, String> headerParams = new HashMap<String, String>();

    // verify required params are set
    if(job_id == null ) {
       throw new ApiException(400, "missing required params");
    }
    headerParams.put("Authorization", Authorization);
    headerParams.put("Accept", Accept);
    try {
      String response = apiInvoker.invokeAPI(basePath, path, "DELETE", queryParams, null, headerParams);
      if(response != null){
        return (DeleteResponse) ApiInvoker.deserialize(response, "", DeleteResponse.class);
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
  public ReportEntity getResultDetail (String job_id, String Authorization, String Accept) throws ApiException {
    // create path and map variables
    String path = "/jobs/{job_id}/result/detail".replaceAll("\\{format\\}","json").replaceAll("\\{" + "job_id" + "\\}", apiInvoker.escapeString(job_id.toString()));

    // query params
    Map<String, String> queryParams = new HashMap<String, String>();
    Map<String, String> headerParams = new HashMap<String, String>();

    // verify required params are set
    if(job_id == null ) {
       throw new ApiException(400, "missing required params");
    }
    headerParams.put("Authorization", Authorization);
    headerParams.put("Accept", Accept);
    try {
      String response = apiInvoker.invokeAPI(basePath, path, "GET", queryParams, null, headerParams);
      if(response != null){
        return (ReportEntity) ApiInvoker.deserialize(response, "", ReportEntity.class);
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
  public ReportEntity getResultDetailHead (String job_id, String Authorization, String Accept) throws ApiException {
    // create path and map variables
    String path = "/jobs/{job_id}/result/detail".replaceAll("\\{format\\}","json").replaceAll("\\{" + "job_id" + "\\}", apiInvoker.escapeString(job_id.toString()));

    // query params
    Map<String, String> queryParams = new HashMap<String, String>();
    Map<String, String> headerParams = new HashMap<String, String>();

    // verify required params are set
    if(job_id == null ) {
       throw new ApiException(400, "missing required params");
    }
    headerParams.put("Authorization", Authorization);
    headerParams.put("Accept", Accept);
    try {
      String response = apiInvoker.invokeAPI(basePath, path, "HEAD", queryParams, null, headerParams);
      if(response != null){
        return (ReportEntity) ApiInvoker.deserialize(response, "", ReportEntity.class);
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
  public StatusResponse getJobStatus (String job_id, String Authorization, String Accept) throws ApiException {
    // create path and map variables
    String path = "/jobs/{job_id}/status".replaceAll("\\{format\\}","json").replaceAll("\\{" + "job_id" + "\\}", apiInvoker.escapeString(job_id.toString()));

    // query params
    Map<String, String> queryParams = new HashMap<String, String>();
    Map<String, String> headerParams = new HashMap<String, String>();

    // verify required params are set
    if(job_id == null ) {
       throw new ApiException(400, "missing required params");
    }
    headerParams.put("Authorization", Authorization);
    headerParams.put("Accept", Accept);
    try {
      String response = apiInvoker.invokeAPI(basePath, path, "GET", queryParams, null, headerParams);
      if(response != null){
        return (StatusResponse) ApiInvoker.deserialize(response, "", StatusResponse.class);
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
  public StatusResponse getJobStatusHead (String job_id, String Authorization, String Accept) throws ApiException {
    // create path and map variables
    String path = "/jobs/{job_id}/status".replaceAll("\\{format\\}","json").replaceAll("\\{" + "job_id" + "\\}", apiInvoker.escapeString(job_id.toString()));

    // query params
    Map<String, String> queryParams = new HashMap<String, String>();
    Map<String, String> headerParams = new HashMap<String, String>();

    // verify required params are set
    if(job_id == null ) {
       throw new ApiException(400, "missing required params");
    }
    headerParams.put("Authorization", Authorization);
    headerParams.put("Accept", Accept);
    try {
      String response = apiInvoker.invokeAPI(basePath, path, "HEAD", queryParams, null, headerParams);
      if(response != null){
        return (StatusResponse) ApiInvoker.deserialize(response, "", StatusResponse.class);
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

