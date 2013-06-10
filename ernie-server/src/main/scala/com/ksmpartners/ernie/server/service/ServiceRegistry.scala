/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.server.service

import com.ksmpartners.ernie.engine.report.{ BirtReportGeneratorFactory, FileReportManager, ReportManager }
import com.ksmpartners.ernie.engine.Coordinator
import com.ksmpartners.ernie.server.PropertyNames._
import com.ksmpartners.ernie.util.Utility._
import java.util.Properties
import java.io.{ FileInputStream, File }
import org.slf4j.{ LoggerFactory, Logger }
import com.ksmpartners.ernie.server.RequiresProperties
import scala.Either

/**
 * Object that registers the services used by the stateless dispatch
 */
object ServiceRegistry extends JobDependencies
    with DefinitionDependencies
    with ReportActorDependencies
    with RequiresCoordinator
    with RequiresReportManager
    with RequiresProperties {

  private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.server.ServiceRegistry")

  def getTimeout: Long = timeout

  def getDefaultRetentionDays: Int = {
    reportManager.getDefaultRetentionDays
  }

  protected val properties: Properties = {

    val propsPath = System.getProperty(propertiesFileNameProp)

    if (null == propsPath) {
      throw new RuntimeException("System property " + propertiesFileNameProp + " is undefined")
    }

    val propsFile = new File(propsPath)
    if (!propsFile.exists) {
      throw new RuntimeException("Properties file " + propsPath + " does not exist.")
    }

    if (!propsFile.canRead) {
      throw new RuntimeException("Properties file " + propsPath + " is not readable; check file privileges.")
    }
    val props = new Properties()
    try_(new FileInputStream(propsFile)) { propsFileStream =>
      props.load(propsFileStream)
    }
    props
  }

  protected val reportManager: ReportManager = {

    if (!properties.stringPropertyNames.contains(rptDefsDirProp)) {
      throw new RuntimeException("Properties file does not contain property " + rptDefsDirProp)
    }
    if (!properties.stringPropertyNames.contains(outputDirProp)) {
      throw new RuntimeException("Properties file does not contain property " + outputDirProp)
    }

    val rptDefsDir = properties.get(rptDefsDirProp).toString
    val outputDir = properties.get(outputDirProp).toString

    val fileReportManager = new FileReportManager(rptDefsDir, outputDir)

    val defaultRetentionDays: Int = try { properties.get(defaultRetentionPeriod).toString.toInt } catch { case e: Exception => ReportManager.getDefaultRetentionDays }
    val maximumRetentionDays: Int = try { properties.get(maximumRetentionPeriod).toString.toInt } catch { case e: Exception => ReportManager.getMaximumRetentionDays }

    fileReportManager.putDefaultRetentionDays(defaultRetentionDays)
    fileReportManager.putMaximumRetentionDays(maximumRetentionDays)

    fileReportManager
  }

  protected val coordinator: Coordinator = {

    if (!properties.stringPropertyNames.contains(jobDirProp)) {
      throw new RuntimeException("Properties file does not contain property " + jobDirProp)
    }
    val jobDir = properties.get(jobDirProp).toString
    val coord = new Coordinator(jobDir, reportManager) with BirtReportGeneratorFactory
    coord.start()
    coord.setTimeout(timeout)
    coord
  }

  val jobsResource = new JobsResource
  val jobStatusResource = new JobStatusResource
  val jobResultsResource = new JobResultsResource

  val defsResource = new DefsResource
  val defDetailResource = new DefDetailResource

  val shutdownResource = new ShutdownResource

  /**
   * Empty method. Calling instantiates this object.
   */
  def init() {
    log.info("BEGIN Initializing ServiceRegistry...")
    log.info("Loaded properties: {}", properties.toString)
    timeout = if (properties.contains("timeout")) try {
      properties.get("timeout").asInstanceOf[Long]
    } catch {
      case _ => 1000L
    }
    else 1000L
    log.info("END Initializing ServiceRegistry...")
  }

}
