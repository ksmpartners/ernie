/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.api.service

import com.ksmpartners.ernie.engine.report.{ MemoryReportManager, BirtReportGeneratorFactory, FileReportManager, ReportManager }
import com.ksmpartners.ernie.engine.{ Coordinator, ErnieCoordinator }
import org.slf4j.{ LoggerFactory, Logger }
import com.ksmpartners.ernie.api.ErnieConfig
import java.io.File
import scala.concurrent.duration.{ FiniteDuration, Duration }
import java.util.concurrent.TimeUnit
import akka.actor.{ ActorRef, ActorSystem, ActorDSL, Actor }

package object ServiceRegistry extends JobDependencies
    with DefinitionDependencies
    with ReportActorDependencies
    with RequiresCoordinator
    with RequiresReportManager {

  private var ernieConfig: Option[ErnieConfig] = None

  protected def outputDir = ernieConfig.map(c => c.outputDir) getOrElse {
    throw new RuntimeException("No output directory specified")
  }
  protected def jobsDir = ernieConfig.map(c => c.jobsDir) getOrElse {
    throw new RuntimeException("No jobs directory specified")
  }
  protected def defDir = ernieConfig.map(c => c.defDir) getOrElse {
    throw new RuntimeException("No definitions directory specified")
  }

  protected def workerCount = ernieConfig.map(c => c.workerCount) getOrElse 5

  protected def timeout = ernieConfig.map(c => c.timeout) getOrElse 30000L

  protected def defaultRetentionDays = ernieConfig.map(c => c.defaultRetentionDays) getOrElse 7
  protected def maxRetentionDays = ernieConfig.map(c => c.defaultRetentionDays) getOrElse 14
  protected def fileReportManager = ernieConfig.map(frm => frm.fileMgr) getOrElse false

  protected val system = ActorSystem("ernie-actor-system")

  private var reportManagerOpt: Option[ReportManager] = None
  private var coordinatorOpt: Option[ActorRef] = None

  protected def reportManager = reportManagerOpt.getOrElse {

    var rm: ReportManager = null
    if (fileReportManager) {
      if (!(new File(defDir)).isDirectory) throw new RuntimeException("Definition path is not a directory")
      if (!(new File(outputDir)).isDirectory) throw new RuntimeException("Output path is not a directory")
      rm = new FileReportManager(defDir, outputDir)
    } else rm = new MemoryReportManager
    val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.engine.report.FileReportManager")
    rm.putDefaultRetentionDays(defaultRetentionDays)
    rm.putMaximumRetentionDays(maxRetentionDays)
    reportManagerOpt = Some(rm)
    rm
  }

  protected def coordinator = coordinatorOpt getOrElse {
    if (fileReportManager) if (!(new File(jobsDir)).isDirectory) throw new RuntimeException("Jobs path is not a directory")
    val coord = ActorDSL.actor(system)({
      val c = new Coordinator(if (fileReportManager) Some(jobsDir) else None, reportManager, Some(FiniteDuration.apply(timeout, TimeUnit.MILLISECONDS)), workerCount) with BirtReportGeneratorFactory
      c.startReportGenerator
      c
    })
    coordinatorOpt = Some(coord)
    coord
  }

  def setCoordinator(c: ActorRef) { coordinatorOpt = Some(c) }

  val jobsResource = new JobsResource
  val jobStatusResource = new JobStatusResource
  val jobCatalogResource = new JobCatalogResource
  val jobEntityResource = new JobEntityResource
  val jobResultsResource = new JobResultsResource

  val defsResource = new DefsResource

  val shutdownResource = new ShutdownResource

  def init(config: ErnieConfig) {
    ernieConfig = Some(config)
    coordinatorOpt = None
    reportManagerOpt = None
    coordinator
  }

  def spawnWorker() {
    coordinator ! com.ksmpartners.ernie.engine.NewWorkerRequest()
  }

}