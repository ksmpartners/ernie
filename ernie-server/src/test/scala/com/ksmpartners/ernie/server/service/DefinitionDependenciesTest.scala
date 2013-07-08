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
package com.ksmpartners.ernie.server.service

import com.ksmpartners.ernie.engine.report.MemoryReportManager
import org.testng.annotations.{ AfterClass, Test, BeforeClass }
import org.testng.Assert
import net.liftweb.http.{ ResponseWithReason, PlainTextResponse }
import com.ksmpartners.ernie.model._
import org.joda.time.DateTime
import com.ksmpartners.ernie.util.Utility._
import com.ksmpartners.ernie.util.MapperUtility._
import com.ksmpartners.ernie.engine.Coordinator
import org.slf4j.{ LoggerFactory, Logger }
import com.ksmpartners.ernie.util.TestLogger
import net.liftweb.mockweb.MockWeb
import com.ksmpartners.ernie.server.service.ServiceRegistry._
import net.liftweb.mocks.MockHttpServletRequest
import com.ksmpartners.ernie.server.filter.SAMLConstants
import java.io.{ FileInputStream, File }

class DefinitionDependenciesTest extends TestLogger { // extends DefinitionDependencies with TestLogger with RequiresAPI {

  @Test(enabled = false)
  val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.server.DefDependenciesTest")

  @Test(enabled = false)
  var defId = ""

  @AfterClass
  def shutdown() {
  }

  @BeforeClass
  def setup() {
    val file = new File(Thread.currentThread.getContextClassLoader.getResource("in/test_def_params.rptdesign").getPath)
  }

  @Test
  def canGetDefinitions() {
    val respBox = defsResource.get

    Assert.assertTrue(respBox.isDefined)

    val resp = respBox.open_!.asInstanceOf[PlainTextResponse]
    Assert.assertEquals(resp.code, 200)
    Assert.assertEquals(resp.headers, List(("Content-Type", "application/vnd.ksmpartners.ernie+json")))
  }

  @Test
  def canGetDefinition() {
    val respBox = defDetailResource.get("test_def_nocsv")

    Assert.assertTrue(respBox.isDefined)

    val resp = respBox.open_!.asInstanceOf[PlainTextResponse]
    Assert.assertEquals(resp.code, 200)
    Assert.assertEquals(resp.headers, List(("Content-Type", "application/vnd.ksmpartners.ernie+json")))
    val defEnt = mapper.readValue(resp.text, classOf[DefinitionEntity])
    Assert.assertEquals(defEnt.getDefId, "test_def_nocsv")
  }
}

