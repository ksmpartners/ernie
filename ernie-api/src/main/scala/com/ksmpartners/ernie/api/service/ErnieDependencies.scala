/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.api.service

import com.ksmpartners.ernie.engine.report._
import com.ksmpartners.ernie.engine.{ Coordinator, ErnieCoordinator }
import org.slf4j.{ LoggerFactory, Logger }
import java.io.File
import scala.concurrent.duration._
import java.util.concurrent.TimeUnit
import akka.actor.{ ActorRef, ActorSystem, ActorDSL, Actor }
import com.ksmpartners.ernie.api.ErnieBuilder
import ErnieBuilder._
import com.ksmpartners.ernie.engine.report.FileReportManager
import com.ksmpartners.ernie.api.ErnieBuilder.ErnieConfiguration
import scala.Some
import com.ksmpartners.ernie.engine.report.ReportManager
import com.ksmpartners.ernie.engine.report.MemoryReportManager

abstract class ErnieDependencies extends JobDependencies
    with DefinitionDependencies
    with ReportActorDependencies
    with RequiresCoordinator
    with RequiresReportManager {

  private var ernieConfig: Option[ErnieConfiguration] = None

  /*protected def outputDir = ernieConfig.map(c => c.reportManager match {
    case FileReportManager(_, _, o) => o
  }) getOrElse {
    throw new RuntimeException("No output directory specified")
  }

  protected def jobsDir = ernieConfig.map(c => c.jobsDir) getOrElse {
    throw new RuntimeException("No jobs directory specified")
  }
  protected def defDir = ernieConfig.map(c => c.defDir) getOrElse {
    throw new RuntimeException("No definitions directory specified")
  }   */

  private def fileReportManager: Option[ErnieBuilder.FileReportManager] =
    ernieConfig match {
      case Some(ErnieConfiguration(f: ErnieBuilder.FileReportManager, _, _, _, _)) => Some(f)
      case _ => None
    }

  /*    .map(c => if (c.reportManager.isInstanceOf[ErnieBuilder.FileReportManager]) c.reportManager.asInstanceOf[ErnieBuilder.FileReportManager] else null)

  protected def workerCount = ernieConfig.map(c => c.workerCount) getOrElse 5

  protected def timeout = ernieConfig.map(c => c.timeout) getOrElse 30000L  */

  def timeoutDuration = ernieConfig.map(c => c.timeout.getOrElse(5 minutes)) getOrElse (5 minutes)

  /*protected def defaultRetentionDays = ernieConfig.map(c => c.defaultRetentionDays) getOrElse 7
  protected def maxRetentionDays = ernieConfig.map(c => c.defaultRetentionDays) getOrElse 14      */
  //protected def fileReportManager = ernieConfig.map(frm => frm.fileMgr) getOrElse false

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
    val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.engine.report.FileReportManager")
    rm.putDefaultRetentionDays(ernieConfig.map(c => c.defaultRetentionDays getOrElse (7)) getOrElse (7))
    rm.putMaximumRetentionDays(ernieConfig.map(c => c.maxRetentionDays getOrElse (14)) getOrElse (14))
    reportManagerOpt = Some(rm)
    rm
  }

  /*  private lazy val _coordinator = {
    if (ernieConfig.isEmpty) throw new RuntimeException("Attempting to start engine without configuring")
    if (fileReportManager.isDefined) if (!(new File(fileReportManager.get.jobDir)).isDirectory) throw new RuntimeException("Jobs path is not a directory")
    val c = new Coordinator(if (fileReportManager.isDefined) Some(fileReportManager.get.jobDir) else None, reportManager,
      ernieConfig.map(c => c.timeout getOrElse (5 minutes)),
      ernieConfig.map(c => c.workerCount getOrElse (50)) getOrElse 50) with BirtReportGeneratorFactory
    c
  }                 */

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

  def configure(config: ErnieConfiguration) {
    ernieConfig = Some(config)
    if (fileReportManager.isDefined) if (!(new File(fileReportManager.get.jobDir)).isDirectory) throw new RuntimeException("Jobs path is not a directory")
    coordinatorOpt = None
    reportManagerOpt = None
    reportManager
  }

  def start() {
    if (ernieConfig.isEmpty || reportManagerOpt.isEmpty) throw new RuntimeException("Ernie engine is not configured")
    if (coordinatorOpt.isDefined) throw new RuntimeException("Ernie engine already started")
    coordinator
  }

  def spawnWorker() {
    coordinator ! com.ksmpartners.ernie.engine.NewWorkerRequest()
  }

}
