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
 *
 */

package com.ksmpartners.ernie

import java.lang.Throwable
import scala.concurrent.duration._
import io.gatling.core.Predef._
import java.io.File
import scala.Predef._
import scala.Predef.String
import com.ksmpartners.ernie.util.Utility._
import io.gatling.com.ksmpartners._
import ErnieSimulation._
import com.ksmpartners.ernie.api.ErnieEngine

class ErnieApiSimulation extends Simulation {

  val config = {
    val defsDir = createTempDirectory
    org.apache.commons.io.FileUtils.copyFile(new File(Thread.currentThread.getContextClassLoader.getResource("test_def.rptdesign").getPath), new File(defsDir, "test_def.rptdesign"))
    org.apache.commons.io.FileUtils.copyFile(new File(Thread.currentThread.getContextClassLoader.getResource("test_def_params.rptdesign").getPath), new File(defsDir, "test_def_params.rptdesign"))
    ErnieProtocolConfiguration(ErnieEngine(api.ErnieBuilder()
      .withFileReportManager(createTempDirectory.getAbsolutePath, defsDir.getAbsolutePath, createTempDirectory().getAbsolutePath)
      .withDefaultRetentionDays(7)
      .withMaxRetentionDays(14)
      .withWorkers(100)
      .build())
    )

  }

  def getADef(defsDir: File, defaultDef: String = "test_def.rptdesign"): String = try {
    defsDir.listFiles.apply(math.random.toInt % defsDir.listFiles.size).getAbsolutePath
  } catch {
    case _: Throwable => defaultDef
  }

  var e = Predef.ernie

  setUp(scn("ErnieAPI", e).protocolConfig(config).inject(ramp(100 users) over (1 seconds)))

}
