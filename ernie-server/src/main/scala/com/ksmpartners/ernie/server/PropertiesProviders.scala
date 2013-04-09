/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.server

import java.util.Properties
import org.slf4j.{ LoggerFactory, Logger }
import java.io.{ FileInputStream, File }

/** Constants object for centralizing the names of the properties in the system configuration **/
object PropertyNames {
  val PROPERTIES_FILE_NAME_PROP = "ernie.props"
  val RPT_DEFS_DIR_PROP = "rpt.def.dir"
  val OUTPUT_DIR_PROP = "output.dir"
}

/** Dependency injection trait to advertise dependence on a java.util.Properties instance */
trait RequiresProperties {
  protected val properties: Properties
}
