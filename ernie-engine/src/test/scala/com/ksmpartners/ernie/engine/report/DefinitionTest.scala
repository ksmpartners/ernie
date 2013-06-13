/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
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
