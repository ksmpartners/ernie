/*
	Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/

package com.ksmpartners.ernie.api

import com.ksmpartners.ernie.api.service._
import com.ksmpartners.ernie.model
import java.io.{ InputStream, ByteArrayInputStream }
import scala.collection.immutable
import java.util.concurrent.TimeoutException
import akka.pattern.AskTimeoutException

import scala.Some
import com.ksmpartners.ernie.api.ErnieBuilder._
import scala.concurrent.duration.FiniteDuration

/**
 * API for interacting with Ernie.
 * Retrieve an instance of ErnieControl by starting an ErnieEngine. For example:
 * {{{
 *   val config = ErnieBuilder() withMemoryReportManager build()
 *   val engine = ErnieEngine(config)
 *   val control = engine.start
 * }}}
 * Or simply:
 * {{{
 *   val control = ErnieEngine(ErnieBuilder() withMemoryReportManager build()).start
 * }}}
 */
protected[api] class ErnieControl extends ErnieDependencies {

  private def wrapper[B](func: () => B): B = try {
    func()
  } catch {
    case a: AskTimeoutException => {
      val t = new TimeoutException(a.getMessage)
      t.initCause(a.getCause())
      t.setStackTrace(a.getStackTrace)
      throw t
    }
    case t: Throwable => throw t
  }

  /**
   * Create a report definition.
   * @param rptDesign BIRT report design XML as byte array input stream
   * @param description a plain text description or identifier for the definition
   * @param createdUser the username of the definition creator
   * @throws InvalidDefinitionException if rptDesign is null or contains malformed XML
   * @throws AskTimeoutException if request times out
   * @return the resultant definition metadata.
   */
  def createDefinition(rptDesign: Option[ByteArrayInputStream], description: String, createdUser: String): model.DefinitionEntity =
    wrapper(() => {
      defsResource.putDefinition(None,
        rptDesign,
        {
          val defEnt = new model.DefinitionEntity
          defEnt.setDefDescription(description)
          defEnt.setCreatedUser(createdUser)
          Some(defEnt)
        })
    })

  /**
   * Return DefinitionEntities for all definitions.
   */
  def getDefinitionsCatalog(): List[model.DefinitionEntity] =
    wrapper(() => defsResource.getCatalog())

  /**
   *  Update a definition.
   *  @param defId existing definition to update
   * @param rptDesign BIRT report design XML as byte array input stream
   * @param defEnt definition metadata
   * @throws NotFoundException if defId is not found
   * @throws MissingArgumentException if neither report design nor DefinitionEntity is provided
   * @throws InvalidDefinitionException if rptDesign is null or contains malformed XML
   * @throws AskTimeoutException if request times out
   * @return updated definition metadata.
   */
  def updateDefinition(defId: String, defEnt: Option[model.DefinitionEntity], rptDesign: Option[ByteArrayInputStream]): model.DefinitionEntity =
    wrapper(() => {
      if (defId == null) throw new MissingArgumentException("Null definition ID")
      defsResource.putDefinition(Some(defId), rptDesign, defEnt)
    })

  /**
   * Get definition metadata.
   * @param defId definition to interrogate
   * @throws AskTimeoutException if request times out
   * @return DefinitionEntity if defId is found; otherwise, [[scala.None]].
   */
  def getDefinitionEntityOpt(defId: String): Option[model.DefinitionEntity] = wrapper(() => {
    if (defId == null) throw new MissingArgumentException("Null definition ID")
    defsResource.getDefinitionEntity(defId)
  })

  /**
   * Get definition metadata.
   * @param defId definition to interrogate
   * @throws NotFoundException if defId is not found or invalid
   * @throws AskTimeoutException if request times out
   * @return DefinitionEntity containing metadata for given defId.
   */
  def getDefinitionEntity(defId: String): model.DefinitionEntity = getDefinitionEntityOpt(defId).getOrElse(throw new NotFoundException(defId + " not found"))

  /**
   * Get definition design as input stream.
   * @param defId definition to interrogate
   * @throws AskTimeoutException if request times out
   * @return InputStream if defId is found; otherwise, [[scala.None]].
   */
  def getDefinitionDesignOpt(defId: String)(b: (Option[InputStream]) => Unit): Unit = wrapper(() => {
    if (defId == null) throw new MissingArgumentException("Null definition ID")
    b(defsResource.getDefinitionDesign(defId))
  })

  /**
   * Get definition design as input stream.
   * @param defId definition to interrogate
   * @throws NotFoundException if defId is not found or invalid
   * @throws AskTimeoutException if request times out
   * @return InputStream if defId is found; otherwise, [[scala.None]].
   */
  def getDefinitionDesign(defId: String)(b: (InputStream) => Unit): Unit = {
    if (defId == null) throw new MissingArgumentException("Null definition ID")
    wrapper(() => defsResource.getDefinitionDesign(defId)).map(is => b(is)).getOrElse(throw new NotFoundException(defId + " design not found"))
  }

  /**
   * Delete a definition. Completely remove the report design and DefinitionEntity from the report manager and filesystem (if applicable).
   * @param defId definition to delete
   * @throws MissingArgumentException if defId is null
   * @throws AskTimeoutException if request times out
   * @return a DeleteStatus indicating the result of deletion.
   */
  def deleteDefinition(defId: String): model.DeleteStatus = wrapper(() => {
    if (defId == null) throw new MissingArgumentException("Null definition ID")
    defsResource.deleteDefinition(defId)
  })

  /**
   * Create and start a report generation job.
   * @param defId an existing report definition/design
   * @param rptType the report output format
   * @param retentionPeriod optional override for default number of days to retain report output
   * @param reportParameters a set of BIRT Report Parameters corresponding to the parameters specified in the report definition.
   * @param userName username of the user creating the job
   * @throws AskTimeoutException if request times out
   * @return the generated job ID and a [[com.ksmpartners.ernie.model.JobStatus]]
   */
  def createJob(defId: String, rptType: model.ReportType, retentionPeriod: Option[Int], reportParameters: immutable.Map[String, String], userName: String): (Long, model.JobStatus) = wrapper(() => {
    jobsResource.createJob(defId, rptType, retentionPeriod, reportParameters, userName)
  })

  /**
   * Get the status of a given job ID
   * @param jobId to interrogate
   * @throws AskTimeoutException if request times out
   * @return [[com.ksmpartners.ernie.model.JobStatus]] of given jobId.
   */
  def getJobStatus(jobId: Long): model.JobStatus = wrapper(() =>
    if (jobId <= 0) throw new MissingArgumentException("Null job ID")
    else jobStatusResource.get(jobId))

  /**
   * Get a catalog of jobs.
   * @param catalog optionally specify a subset of jobs to retrieve
   * @throws AskTimeoutException if request times out
   * @return a list of [[com.ksmpartners.ernie.model.JobEntity]] constituting the catalog.
   */
  def getJobCatalog(catalog: Option[model.JobCatalog]): List[model.JobEntity] = wrapper(() => jobCatalogResource.getCatalog(catalog))

  /**
   * Get a list of all job IDs as strings.
   * @throws AskTimeoutException if request times out
   */
  def getJobList(): List[String] = wrapper(() => jobsResource.getList)

  /**
   * Return all existing definition IDs.
   */
  def getDefinitionList(): List[String] = wrapper(() => defsResource.getList)

  /**
   * Retrieve job metadata.
   * @param jobId the ID of the job to interrogate
   * @throws MissingArgumentException if jobId is null or invalid
   * @throws AskTimeoutException if request times out
   * @return a JobEntity if the jobId is found; otherwise, [[scala.None]].
   */
  def getJobEntity(jobId: Long): Option[model.JobEntity] = wrapper(() =>
    if (jobId <= 0) throw new MissingArgumentException("Null job ID")
    else jobEntityResource.getJobEntity(jobId))

  /**
   * Retrieve report output metadata.
   * @param jobId the job whose report output metadata is to be interrogated
   * @throws AskTimeoutException if request times out
   * @return a [[com.ksmpartners.ernie.model.ReportEntity]] if the job ID is found.
   */
  def getReportEntity(jobId: Long): Option[model.ReportEntity] = wrapper(() =>
    if (jobId <= 0) throw new MissingArgumentException("Null job ID")
    else jobResultsResource.getReportEntity(jobId))

  /**
   * Retrieve report output metadata.
   * @param rptId the report output whose metadata is to be interrogated
   * @throws AskTimeoutException if request times out
   * @return a [[com.ksmpartners.ernie.model.ReportEntity]] if the report ID is found.
   */
  def getReportEntity(rptId: String): Option[model.ReportEntity] = wrapper(() =>
    if (rptId == null) throw new MissingArgumentException("Null report ID")
    else jobResultsResource.getReportEntity(com.ksmpartners.ernie.util.Utility.rptToJobId(rptId)))

  /**
   * Retrieve job output.
   * @param jobId the jobId whose output is to be retrieved
   * @throws AskTimeoutException if request times out
   * @return a [[java.io.InputStream]] if the report output is available; otherwise, [[scala.None]]
   */
  def getReportOutput(jobId: Long): Option[InputStream] = wrapper(() =>
    if (jobId <= 0) throw new MissingArgumentException("Null job ID")
    else jobResultsResource.get(jobId))

  /**
   * Delete a job's output and any associated metadata
   * @param jobId the job whose output and metadata is to be deleted
   * @throws AskTimeoutException if request times out
   * @return the status of the deletion
   */
  def deleteReportOutput(jobId: Long): model.DeleteStatus = wrapper(() =>
    if (jobId <= 0) throw new MissingArgumentException("Null job ID")
    else jobResultsResource.del(jobId))

  /**
   * Purge jobs in expired catalog.
   * @return the status of the batch deletion and a list of purged report IDs.
   */
  def purgeExpiredReports(): (model.DeleteStatus, List[String]) = wrapper(() => jobCatalogResource.purge)

  /**
   * Shut down the instance of [[com.ksmpartners.ernie.engine]] in use by this object
   */
  def shutDown() = shutdownResource.shutdown

}

/**
 * Resource for instantiating an instance of [[com.ksmpartners.ernie.api.ErnieControl]] given an [[com.ksmpartners.ernie.api.ErnieBuilder.ErnieConfiguration]]
 */
class ErnieEngine private () {
  private val ec = new ErnieControl

  /**
   * Initialize the ErnieControl and all dependencies
   * @return the initialized ErnieControl
   */
  def start: ErnieControl = {
    ec.start()
    ec
  }
}

/**
 * Factory for [[com.ksmpartners.ernie.api.ErnieEngine]] instances. Main entry-point for using [[com.ksmpartners.ernie.api]].
 */
object ErnieEngine {
  /**
   * Creates and configures an ErnieEngine
   * @param config the configuration to use in instantiating an [[com.ksmpartners.ernie.api.ErnieEngine]]
   * @return a new ErnieEngine configured using the config parameter
   */
  def apply(config: ErnieConfiguration): ErnieEngine = {
    val e = new ErnieEngine()
    e.ec.configure(config)
    e
  }
}

/**
 * Provides a DSL for building a new ErnieConfiguration.
 * For example:
 * {{{
 *   import com.ksmpartners.ernie.ErnieBuilder._
 *   val ec:ErnieConfiguration = ErnieBuilder()
 *    withMemoryReportManager()
 *    timeoutAfter(30 seconds)
 *    withWorkers(50)
 *    build()
 * }}}
 */
object ErnieBuilder {

  def apply() = ernieBuilder

  sealed abstract class ReportManager
  case class FileReportManager(jobDir: String, defDir: String, outputDir: String) extends ReportManager
  case class MemoryReportManager() extends ReportManager

  case class ErnieConfiguration(val reportManager: ReportManager, val timeout: Option[FiniteDuration], val defaultRetentionDays: Option[Int], val maxRetentionDays: Option[Int], val workerCount: Option[Int])

  abstract class BOOL
  abstract class TRUE extends BOOL
  abstract class FALSE extends BOOL

  class ConfigBuilder[HRM](val reportManager: Option[ReportManager], val timeout: Option[FiniteDuration], val defaultRetentionDays: Option[Int], val maxRetentionDays: Option[Int], val workerCount: Option[Int]) {

    def withFileReportManager(jobDir: String, defDir: String, outputDir: String): ConfigBuilder[TRUE] = new ConfigBuilder[TRUE](Some(FileReportManager(jobDir, defDir, outputDir)),
      timeout, defaultRetentionDays, maxRetentionDays, workerCount)

    def withMemoryReportManager(): ConfigBuilder[TRUE] = new ConfigBuilder[TRUE](Some(MemoryReportManager()),
      timeout, defaultRetentionDays, maxRetentionDays, workerCount)

    def timeoutAfter(fD: FiniteDuration) =
      new ConfigBuilder[HRM](reportManager, Some(fD), defaultRetentionDays, maxRetentionDays, workerCount)

    def withDefaultRetentionDays(days: Int) =
      new ConfigBuilder[HRM](reportManager, timeout, Some(days), maxRetentionDays, workerCount)

    def withMaxRetentionDays(days: Int) =
      new ConfigBuilder[HRM](reportManager, timeout, defaultRetentionDays, Some(days), workerCount)

    def withWorkers(count: Int) =
      new ConfigBuilder[HRM](reportManager, timeout, defaultRetentionDays, maxRetentionDays, Some(count))
  }

  implicit def enableBuild(builder: ConfigBuilder[TRUE]) = new {
    def build() =
      new ErnieConfiguration(builder.reportManager.get, builder.timeout, builder.defaultRetentionDays, builder.maxRetentionDays, builder.workerCount)
  }

  def ernieBuilder = new ConfigBuilder[FALSE](None, None, None, None, None)
}