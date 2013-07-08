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
 *
 */

package com.ksmpartners.ernie.engine.report

import org.testng.annotations.Test
import com.ksmpartners.ernie.model.{ ReportType, ParameterEntity, DefinitionEntity }
import org.joda.time.DateTime
import java.util
import org.testng.Assert

class DefinitionTest {

  var definition: Definition = null
  var definitionEntity: DefinitionEntity = null

  @Test
  def canCreateDefinition() {
    definitionEntity = new DefinitionEntity()
    definitionEntity.setCreatedDate(DateTime.now)
    definitionEntity.setCreatedUser("default")
    definitionEntity.setDefDescription("test desc")
    definitionEntity.setDefId("test_def")

    val lst = new util.ArrayList[String]()
    lst.add("val1")
    definitionEntity.setParamNames(lst)

    val paramEnt = new ParameterEntity("val1", "integer", false, "10")
    val params = new util.ArrayList[ParameterEntity]()
    params.add(paramEnt)
    definitionEntity.setParams(params)

    val unsTypes = new util.ArrayList[ReportType]()
    unsTypes.add(ReportType.CSV)
    definitionEntity.setUnsupportedReportTypes(unsTypes)

    definition = new Definition(definitionEntity)
  }

  @Test(dependsOnMethods = Array("canCreateDefinition"))
  def getDefinitionEntityReturnsCopy() {
    Assert.assertEquals(definition.getCreatedDate, definitionEntity.getCreatedDate)
    Assert.assertEquals(definition.getCreatedUser, definitionEntity.getCreatedUser)
    Assert.assertEquals(definition.getDefDescription, definitionEntity.getDefDescription)
    Assert.assertEquals(definition.getDefId, definitionEntity.getDefId)
    Assert.assertNotSame(definition.getEntity, definitionEntity)
  }

  @Test(dependsOnMethods = Array("canCreateDefinition"))
  def canGetUnsupportedTypes() {
    Assert.assertEquals(definition.getUnsupportedReportTypes.getClass, classOf[Array[ReportType]])
    Assert.assertEquals(definition.getUnsupportedReportTypes.size, 1)
  }

  @Test(dependsOnMethods = Array("canCreateDefinition"))
  def canGetParamNames() {
    Assert.assertEquals(definition.getParamNames.getClass, classOf[Array[String]])
    Assert.assertEquals(definition.getParamNames.size, 1)
  }

  @Test
  def nullListsReturnEmptyListsInstead() {
    val defi = new Definition(new DefinitionEntity())
    Assert.assertNotNull(defi.getParamNames)
    Assert.assertNotNull(defi.getUnsupportedReportTypes)
  }

}
