<?php
/**
 *  Copyright 2011 Wordnik, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 *
 * NOTE: This class is auto generated by the swagger code generator program. Do not edit the class manually.
 */
class DefsApi {

	function __construct($apiClient) {
	  $this->apiClient = $apiClient;
	}

  /**
	 * getDefinition
	 * Retrieve a mapping of definition IDs to URIs
   * Authorization, string: Authorization (optional)
   * Accept, string: Accept (optional)
   * @return reportDefMap
	 */

   public function getDefinition($Authorization=null, $Accept=null) {

  		//parse inputs
  		$resourcePath = "/defs";
  		$resourcePath = str_replace("{format}", "json", $resourcePath);
  		$method = "GET";
      $queryParams = array();
      $headerParams = array();

      if($Authorization != null) {
                $headerParams['Authorization'] = $this->apiClient->toHeaderValue($Authorization);
            }
        if($Accept != null) {
                $headerParams['Accept'] = $this->apiClient->toHeaderValue($Accept);
            }
        //make the API Call
      if (! isset($body)) {
        $body = null;
      }
  		$response = $this->apiClient->callAPI($resourcePath, $method,
  		                                      $queryParams, $body,
  		                                      $headerParams);


      if(! $response){
          return null;
        }

  		$responseObject = $this->apiClient->deserialize($response,
  		                                                'reportDefMap');
  		return $responseObject;

      }
  /**
	 * getDefinitionHead
	 * Retrieve a mapping of definition IDs to URIs
   * Authorization, string: Authorization (optional)
   * Accept, string: Accept (optional)
   * @return 
	 */

   public function getDefinitionHead($Authorization=null, $Accept=null) {

  		//parse inputs
  		$resourcePath = "/defs";
  		$resourcePath = str_replace("{format}", "json", $resourcePath);
  		$method = "HEAD";
      $queryParams = array();
      $headerParams = array();

      if($Authorization != null) {
                $headerParams['Authorization'] = $this->apiClient->toHeaderValue($Authorization);
            }
        if($Accept != null) {
                $headerParams['Accept'] = $this->apiClient->toHeaderValue($Accept);
            }
        //make the API Call
      if (! isset($body)) {
        $body = null;
      }
  		$response = $this->apiClient->callAPI($resourcePath, $method,
  		                                      $queryParams, $body,
  		                                      $headerParams);


      }
  /**
	 * postDefinition
	 * Post a DefinitionEntity
   * Authorization, string: Authorization (optional)
   * Accept, string: Accept (optional)
   * body, string: DefinitionEntity (optional)
   * @return DefinitionEntity
	 */

   public function postDefinition($Authorization=null, $Accept=null, $body=null) {

  		//parse inputs
  		$resourcePath = "/defs";
  		$resourcePath = str_replace("{format}", "json", $resourcePath);
  		$method = "POST";
      $queryParams = array();
      $headerParams = array();

      if($Authorization != null) {
                $headerParams['Authorization'] = $this->apiClient->toHeaderValue($Authorization);
            }
        if($Accept != null) {
                $headerParams['Accept'] = $this->apiClient->toHeaderValue($Accept);
            }
        //make the API Call
      if (! isset($body)) {
        $body = null;
      }
  		$response = $this->apiClient->callAPI($resourcePath, $method,
  		                                      $queryParams, $body,
  		                                      $headerParams);


      if(! $response){
          return null;
        }

  		$responseObject = $this->apiClient->deserialize($response,
  		                                                'DefinitionEntity');
  		return $responseObject;

      }
  /**
	 * getDefinitionDetail
	 * Retrieve the DefinitionEntity for a specific Definition ID
   * def_id, string: def_id (optional)
   * Authorization, string: Authorization (optional)
   * Accept, string: Accept (optional)
   * @return DefinitionEntity
	 */

   public function getDefinitionDetail($def_id=null, $Authorization=null, $Accept=null) {

  		//parse inputs
  		$resourcePath = "/defs/{def_id}";
  		$resourcePath = str_replace("{format}", "json", $resourcePath);
  		$method = "GET";
      $queryParams = array();
      $headerParams = array();

      if($Authorization != null) {
                $headerParams['Authorization'] = $this->apiClient->toHeaderValue($Authorization);
            }
        if($Accept != null) {
                $headerParams['Accept'] = $this->apiClient->toHeaderValue($Accept);
            }
        if($def_id != null) {
  			$resourcePath = str_replace("{" . "def_id" . "}",
  			                            $this->apiClient->toPathValue($def_id), $resourcePath);
  		}
  		//make the API Call
      if (! isset($body)) {
        $body = null;
      }
  		$response = $this->apiClient->callAPI($resourcePath, $method,
  		                                      $queryParams, $body,
  		                                      $headerParams);


      if(! $response){
          return null;
        }

  		$responseObject = $this->apiClient->deserialize($response,
  		                                                'DefinitionEntity');
  		return $responseObject;

      }
  /**
	 * getDefinitionDetailHead
	 * Retrieve the DefinitionEntity for a specific Definition ID
   * def_id, string: def_id (optional)
   * Authorization, string: Authorization (optional)
   * Accept, string: Accept (optional)
   * @return 
	 */

   public function getDefinitionDetailHead($def_id=null, $Authorization=null, $Accept=null) {

  		//parse inputs
  		$resourcePath = "/defs/{def_id}";
  		$resourcePath = str_replace("{format}", "json", $resourcePath);
  		$method = "HEAD";
      $queryParams = array();
      $headerParams = array();

      if($Authorization != null) {
                $headerParams['Authorization'] = $this->apiClient->toHeaderValue($Authorization);
            }
        if($Accept != null) {
                $headerParams['Accept'] = $this->apiClient->toHeaderValue($Accept);
            }
        if($def_id != null) {
  			$resourcePath = str_replace("{" . "def_id" . "}",
  			                            $this->apiClient->toPathValue($def_id), $resourcePath);
  		}
  		//make the API Call
      if (! isset($body)) {
        $body = null;
      }
  		$response = $this->apiClient->callAPI($resourcePath, $method,
  		                                      $queryParams, $body,
  		                                      $headerParams);


      }
  /**
	 * deleteDefinition
	 * Deletes a specific definition
   * def_id, string: def_id (optional)
   * Authorization, string: Authorization (optional)
   * Accept, string: Accept (optional)
   * @return DefinitionDeleteResponse
	 */

   public function deleteDefinition($def_id=null, $Authorization=null, $Accept=null) {

  		//parse inputs
  		$resourcePath = "/defs/{def_id}";
  		$resourcePath = str_replace("{format}", "json", $resourcePath);
  		$method = "DELETE";
      $queryParams = array();
      $headerParams = array();

      if($Authorization != null) {
                $headerParams['Authorization'] = $this->apiClient->toHeaderValue($Authorization);
            }
        if($Accept != null) {
                $headerParams['Accept'] = $this->apiClient->toHeaderValue($Accept);
            }
        if($def_id != null) {
  			$resourcePath = str_replace("{" . "def_id" . "}",
  			                            $this->apiClient->toPathValue($def_id), $resourcePath);
  		}
  		//make the API Call
      if (! isset($body)) {
        $body = null;
      }
  		$response = $this->apiClient->callAPI($resourcePath, $method,
  		                                      $queryParams, $body,
  		                                      $headerParams);


      if(! $response){
          return null;
        }

  		$responseObject = $this->apiClient->deserialize($response,
  		                                                'DefinitionDeleteResponse');
  		return $responseObject;

      }
  /**
	 * putDefinition
	 * Put definition rptdesign
   * def_id, string: def_id (optional)
   * Authorization, string: Authorization (optional)
   * Accept, string: Accept (optional)
   * body, string: Rptdesign (optional)
   * @return DefinitionEntity
	 */

   public function putDefinition($def_id=null, $Authorization=null, $Accept=null, $body=null) {

  		//parse inputs
  		$resourcePath = "/defs/{def_id}/rptdesign";
  		$resourcePath = str_replace("{format}", "json", $resourcePath);
  		$method = "PUT";
      $queryParams = array();
      $headerParams = array();

      if($Authorization != null) {
                $headerParams['Authorization'] = $this->apiClient->toHeaderValue($Authorization);
            }
        if($Accept != null) {
                $headerParams['Accept'] = $this->apiClient->toHeaderValue($Accept);
            }
        if($def_id != null) {
  			$resourcePath = str_replace("{" . "def_id" . "}",
  			                            $this->apiClient->toPathValue($def_id), $resourcePath);
  		}
  		//make the API Call
      if (! isset($body)) {
        $body = null;
      }
  		$response = $this->apiClient->callAPI($resourcePath, $method,
  		                                      $queryParams, $body,
  		                                      $headerParams);


      if(! $response){
          return null;
        }

  		$responseObject = $this->apiClient->deserialize($response,
  		                                                'DefinitionEntity');
  		return $responseObject;

      }
  
}

