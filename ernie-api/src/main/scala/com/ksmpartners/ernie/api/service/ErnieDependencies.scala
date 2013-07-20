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

package com.ksmpartners.ernie.api.service

import com.ksmpartners.ernie.engine.report._
import com.ksmpartners.ernie.engine.Coordinator
import java.io.File
import scala.concurrent.duration._
import akka.actor.{ ActorRef, ActorSystem, ActorDSL }
import com.ksmpartners.ernie.api.ErnieBuilder
import com.ksmpartners.ernie.engine.report.FileReportManager
import com.ksmpartners.ernie.api._
import scala.Some
import com.ksmpartners.ernie.engine.report.ReportManager
import com.ksmpartners.ernie.engine.report.MemoryReportManager

/**
 * Provides the full set of dependencies for interacting with [[com.ksmpartners.ernie.engine]]
 */
abstract class ErnieDependencies extends JobDependencies
    with DefinitionDependencies
    with ReportActorDependencies
    with RequiresCoordinator
    with RequiresReportManager {

  private var ernieConfig: Option[ErnieConfiguration] = None

  private def fileReportManager: Option[com.ksmpartners.ernie.api.FileReportManager] =
    ernieConfig match {
      case Some(ErnieConfiguration(f: com.ksmpartners.ernie.api.FileReportManager, _, _, _, _)) => Some(f)
      case _ => None
    }

  /**
   * Get the timeout for requests to [[com.ksmpartners.ernie.engine.Coordinator]] as specified by the initial [[com.ksmpartners.ernie.api.ErnieConfiguration]]
   */
  def timeoutDuration = ernieConfig.map(c => c.timeout.getOrElse(5 minutes)) getOrElse (5 minutes)

  protected val system = ActorSystem("ernie-actor-system")

  private var reportManagerOpt: Option[ReportManager] = None
  private var coordinatorOpt: Option[ActorRef] = None

  protected def reportManager = reportManagerOpt.getOrElse {
    var rm: ReportManager = null
    if (fileReportManager.isDefined) {
      if (!(new File(fileReportManager.get.defDir).isDirectory)) throw new RuntimeException("Definition path is not a directory")
      if (!(new File(fileReportManager.get.outputDir)).isDirectory) throw new RuntimeException("Output path is not a directory")
      rm = new FileReportManager(fileReportManager.get.defDir, fileReportManager.get.outputDir)
    } else rm = new MemoryReportManager
    rm.putDefaultRetentionDays(ernieConfig.map(c => c.defaultRetentionDays getOrElse (7)) getOrElse (7))
    rm.putMaximumRetentionDays(ernieConfig.map(c => c.maxRetentionDays getOrElse (14)) getOrElse (14))
    reportManagerOpt = Some(rm)
    rm
  }

  protected def coordinator = coordinatorOpt getOrElse {
    val coord = ActorDSL.actor(system)({
      val c = new Coordinator(if (fileReportManager.isDefined) Some(fileReportManager.get.jobDir) else None, reportManager,
        ernieConfig.map(c => c.timeout getOrElse (5 minutes)),
        ernieConfig.map(c => c.workerCount getOrElse (50)) getOrElse 50) with BirtReportGeneratorFactory
      c.startReportGenerator()
      c
    })
    coordinatorOpt = Some(coord)
    coord
  }

  protected[api] def setCoordinator(c: ActorRef) { coordinatorOpt = Some(c) }

  val jobsResource = new JobsResource
  val jobStatusResource = new JobStatusResource
  val jobCatalogResource = new JobCatalogResource
  val jobEntityResource = new JobEntityResource
  val jobResultsResource = new JobResultsResource

  val defsResource = new DefsResource

  val shutdownResource = new ShutdownResource

  /**
   * Prepare the dependencies using the provided [[com.ksmpartners.ernie.api.ErnieConfiguration]]
   * @param config a configuration for this class built using [[com.ksmpartners.ernie.api.ErnieBuilder]]
   * @throws RuntimeException if invalid directories are provided
   */
  def configure(config: ErnieConfiguration) {
    ernieConfig = Some(config)
    if (fileReportManager.isDefined) if (!(new File(fileReportManager.get.jobDir)).isDirectory) throw new RuntimeException("Jobs path is not a directory")
    coordinatorOpt = None
    reportManagerOpt = None
    reportManager
  }

  /**
   * Initialize and run all dependencies
   */
  def start() {
    if (ernieConfig.isEmpty || reportManagerOpt.isEmpty) throw new RuntimeException("Ernie engine is not configured")
    if (coordinatorOpt.isDefined) throw new RuntimeException("Ernie engine already started")
    coordinator
  }

  /**
   * Spawn an additional Akka Actor to generate BIRT reports
   */
  def spawnWorker() {
    coordinator ! com.ksmpartners.ernie.engine.NewWorkerRequest()
  }

}
