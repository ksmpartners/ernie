/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.server

import java.util.Properties

/** Constants object for centralizing the names of the properties in the system configuration **/
object PropertyNames {
  val propertiesFileNameProp = "ernie.props"
  val keystoreLocProp = "keystore.location"
  val authModeProp = "authentication.mode"
  val rptDefsDirProp = "rpt.def.dir"
  val outputDirProp = "output.dir"
  val defaultRetentionPeriod = "retention.period.default"
  val maximumRetentionPeriod = "retention.period.maximum"
}

/** Dependency injection trait to advertise dependence on a java.util.Properties instance */
trait RequiresProperties {
  protected val properties: Properties
}
