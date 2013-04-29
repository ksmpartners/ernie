/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.server

import com.ksmpartners.ernie.engine.report.{ BirtReportGeneratorFactory, FileReportManager, ReportManager }
import com.ksmpartners.ernie.engine.Coordinator
import com.ksmpartners.ernie.server.PropertyNames._
import java.util.Properties
import java.io.{ FileInputStream, File }
import org.slf4j.{ LoggerFactory, Logger }

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

  protected val properties: Properties = {

    val propsPath = System.getProperty(PROPERTIES_FILE_NAME_PROP)

    if (null == propsPath) {
      throw new RuntimeException("System property " + PROPERTIES_FILE_NAME_PROP + " is undefined")
    }

    val propsFile = new File(propsPath)
    if (!propsFile.exists) {
      throw new RuntimeException("Properties file " + propsPath + " does not exist.")
    }

    if (!propsFile.canRead) {
      throw new RuntimeException("Properties file " + propsPath + " is not readable; check file privileges.")
    }
    val props = new Properties()
    var propsFileStream: FileInputStream = null
    try {
      propsFileStream = new FileInputStream(propsFile)
      props.load(propsFileStream)
    } finally {
      if (propsFileStream != null) propsFileStream.close()
    }
    props
  }

  protected val reportManager: ReportManager = {

    if (!properties.stringPropertyNames.contains(RPT_DEFS_DIR_PROP)) {
      throw new RuntimeException("Properties file does not contain property " + RPT_DEFS_DIR_PROP)
    }
    if (!properties.stringPropertyNames.contains(OUTPUT_DIR_PROP)) {
      throw new RuntimeException("Properties file does not contain property " + OUTPUT_DIR_PROP)
    }

    val rptDefsDir = properties.get(RPT_DEFS_DIR_PROP).toString
    val outputDir = properties.get(OUTPUT_DIR_PROP).toString

    new FileReportManager(rptDefsDir, outputDir)
  }

  protected val coordinator: Coordinator = {
    val coord = new Coordinator(reportManager) with BirtReportGeneratorFactory
    coord.start()
    coord
  }

  val jobsResource = new JobsResource
  val jobStatusResource = new JobStatusResource
  val jobResultsResource = new JobResultsResource

  val defsResource = new DefsResource

  val shutdownResource = new ShutdownResource

  def init() {
    log.info("BEGIN Initializing ServiceRegistry...")
    log.info("Loaded properties: {}", properties.toString)
    log.info("END Initializing ServiceRegistry...")
  }

}
