/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.api.service

import com.ksmpartners.ernie.engine.report.{ MemoryReportManager, BirtReportGeneratorFactory, FileReportManager, ReportManager }
import com.ksmpartners.ernie.engine.Coordinator
import org.slf4j.{ LoggerFactory, Logger }
import com.ksmpartners.ernie.api.ErnieConfig
import java.io.File

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

  protected def timeout = ernieConfig.map(c => c.timeout) getOrElse 30000L

  protected def defaultRetentionDays = ernieConfig.map(c => c.defaultRetentionDays) getOrElse 7
  protected def maxRetentionDays = ernieConfig.map(c => c.defaultRetentionDays) getOrElse 14
  protected def fileReportManager = ernieConfig.map(frm => frm.fileMgr) getOrElse false

  private var reportManagerOpt: Option[ReportManager] = None
  private var coordinatorOpt: Option[Coordinator] = None

  protected def reportManager = reportManagerOpt.getOrElse {
    if (!(new File(defDir)).isDirectory) throw new RuntimeException("Definition path is not a directory")
    if (!(new File(outputDir)).isDirectory) throw new RuntimeException("Output path is not a directory")
    var rm: ReportManager = null
    if (fileReportManager) rm = new FileReportManager(defDir, outputDir)
    else rm = new MemoryReportManager
    val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.engine.report.FileReportManager")
    rm.putDefaultRetentionDays(defaultRetentionDays)
    rm.putMaximumRetentionDays(maxRetentionDays)
    reportManagerOpt = Some(rm)
    rm
  }

  protected def coordinator = coordinatorOpt getOrElse {
    if (!(new File(jobsDir)).isDirectory) throw new RuntimeException("Jobs path is not a directory")
    val coord = new Coordinator(jobsDir, reportManager) with BirtReportGeneratorFactory
    coord.start()
    coord.setTimeout(timeout)
    coordinatorOpt = Some(coord)
    coord
  }

  val jobsResource = new JobsResource
  val jobStatusResource = new JobStatusResource
  val jobResultsResource = new JobResultsResource

  val defsResource = new DefsResource

  val shutdownResource = new ShutdownResource

  def init(config: ErnieConfig) {
    ernieConfig = Some(config)
    coordinator
  }

}
