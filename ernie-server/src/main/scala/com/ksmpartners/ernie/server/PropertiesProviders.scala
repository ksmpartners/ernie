/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
  val workerCountProp = "worker.count"
  val jobDirProp = "jobs.dir"
  val swaggerDocsProp = "swagger.docs"
  val defaultRetentionPeriod = "retention.period.default"
  val maximumRetentionPeriod = "retention.period.maximum"
  val requestTimeoutSeconds = "request.timeout.seconds"
}

/** Dependency injection trait to advertise dependence on a java.util.Properties instance */
trait RequiresProperties {
  protected val properties: Properties
}
