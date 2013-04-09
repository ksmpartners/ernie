/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.server

import com.ksmpartners.ernie.engine.report.{ FileReportManager, ReportManager }
import com.ksmpartners.ernie.engine.Coordinator
import com.ksmpartners.ernie.server.PropertyNames._
import java.util.Properties
import java.io.{ FileInputStream, File }

/**
 * Object that registers the services used by the stateless dispatch
 */
object ServiceRegistry extends JobDependencies
    with DefinitionDependencies
    with ReportActorDependencies
    with RequiresCoordinator
    with RequiresReportManager
    with RequiresProperties {

  protected val properties: Properties = {

    val propsPath = System.getProperties.getProperty(PROPERTIES_FILE_NAME_PROP)

    if (null == propsPath) {
      throw new RuntimeException("System property " + PROPERTIES_FILE_NAME_PROP + " is undefined")
    }

    val propsFile = new File(propsPath)
    if (!propsFile.exists()) {
      throw new RuntimeException("Properties file " + propsPath + " does not exist.")
    }

    if (!propsFile.canRead()) {
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

  protected val reportManager: ReportManager =
    new FileReportManager(properties.get(RPT_DEFS_DIR_PROP).toString,
      properties.get(OUTPUT_DIR_PROP).toString)

  protected val coordinator: Coordinator = new Coordinator(reportManager).start().asInstanceOf[Coordinator]

  val jobsResource = new JobsResource
  val jobStatusResource = new JobStatusResource
  val jobResultsResource = new JobResultsResource

  val defsResource = new DefsResource

  val shutdownResource = new ShutdownResource

}
