/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.api

import com.ksmpartners.ernie.api.service.{ JobDependencies, ServiceRegistry }
import com.ksmpartners.ernie.model._
import scala.xml.NodeSeq
import java.io.{ ByteArrayOutputStream, ByteArrayInputStream }
import scala.collection.immutable
import org.apache.commons.io.IOUtils

case class ErnieResponse(errorOpt: Option[Exception])

case class ErnieConfig(fileMgr: Boolean, jobsDir: String, defDir: String, outputDir: String, timeout: Long, defaultRetentionDays: Int, maxRetentionDays: Int)

case class Definition(defEnt: Option[DefinitionEntity], rptDesign: Option[Array[Byte]], error: Option[Exception]) extends ErnieResponse(error)
case class DefinitionCatalog(catalog: List[DefinitionEntity], error: Option[Exception]) extends ErnieResponse(error)
case class JobStatus(jobId: Long, jobStatus: Option[com.ksmpartners.ernie.model.JobStatus], error: Option[Exception]) extends ErnieResponse(error)
case class JobEntity(jobEntity: Option[com.ksmpartners.ernie.model.JobEntity], error: Option[Exception]) extends ErnieResponse(error)
case class ReportEntity(rptEntity: Option[com.ksmpartners.ernie.model.ReportEntity], error: Option[Exception]) extends ErnieResponse(error)
case class ReportOutput(stream: Option[java.io.InputStream], file: Option[java.io.File], error: Option[Exception]) extends ErnieResponse(error)
case class PurgeResult(deleteStatus: DeleteStatus, purgedIds: List[String], error: Option[Exception]) extends ErnieResponse(error)
class MissingArgumentException(msg: String) extends Exception(msg)
class InvalidDefinitionException(msg: String) extends Exception(msg)
class NotFoundException(msg: String) extends Exception(msg)
class NothingToReturnException(msg: String) extends Exception(msg)
class TimeoutException(msg: String) extends Exception(msg)
class ReportOutputException(status: Option[com.ksmpartners.ernie.model.JobStatus], msg: String) extends Exception(msg)

