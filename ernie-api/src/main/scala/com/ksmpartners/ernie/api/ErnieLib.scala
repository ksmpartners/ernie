/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
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

