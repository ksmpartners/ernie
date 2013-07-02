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

package com.ksmpartners.ernie.api

import com.ksmpartners.ernie.model._

/*class ErnieResponse(e: Option[Exception]) {
  def errorOpt: Option[Exception] = e
}
object ErnieResponse {
  def apply(errorOpt: Option[Exception]) = new ErnieResponse(errorOpt)
}    */

/*
case class Definition(defEnt: Option[DefinitionEntity], rptDesign: Option[Array[Byte]], error: Option[Exception]) extends ErnieResponse(error)
case class DefinitionCatalog(catalog: List[DefinitionEntity], error: Option[Exception]) extends ErnieResponse(error)
case class JobStatus(jobId: Long, jobStatus: Option[com.ksmpartners.ernie.model.JobStatus], error: Option[Exception]) extends ErnieResponse(error)
case class JobEntity(jobEntity: Option[com.ksmpartners.ernie.model.JobEntity], error: Option[Exception]) extends ErnieResponse(error)
case class ReportEntity(rptEntity: Option[com.ksmpartners.ernie.model.ReportEntity], error: Option[Exception]) extends ErnieResponse(error)
case class ReportOutput(stream: Option[java.io.InputStream], file: Option[java.io.File], rptEnt: com.ksmpartners.ernie.model.ReportEntity, error: Option[Exception]) extends ErnieResponse(error)
case class PurgeResult(deleteStatus: DeleteStatus, purgedIds: List[String], error: Option[Exception]) extends ErnieResponse(error)        */
case class MissingArgumentException(msg: String) extends Exception(msg)
case class InvalidDefinitionException(msg: String) extends Exception(msg)
case class NotFoundException(msg: String) extends Exception(msg)
case class NothingToReturnException(msg: String) extends Exception(msg)
case class ReportOutputException(status: Option[com.ksmpartners.ernie.model.JobStatus], msg: String) extends Exception(msg) {
  def compare(e: Exception): Boolean = {
    if (e.isInstanceOf[ReportOutputException]) {
      if (status.isEmpty || e.asInstanceOf[ReportOutputException].status.isEmpty) true
      else e.asInstanceOf[ReportOutputException].status.equals(status)
    } else false
  }
}

