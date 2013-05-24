/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.server.service

import com.ksmpartners.ernie.engine.report.MemoryReportManager
import org.testng.annotations.{ Test, BeforeClass }
import org.testng.Assert
import net.liftweb.http.PlainTextResponse
import com.ksmpartners.ernie.model.DefinitionEntity
import org.joda.time.DateTime
import com.ksmpartners.ernie.util.MapperUtility._

class DefinitionDependenciesTest extends DefinitionDependencies {

  val reportManager = new MemoryReportManager

  @BeforeClass
  def setup() {
    val byteArr = Array[Byte](1, 2, 3)
    reportManager.putDefinition("test_def", byteArr, new DefinitionEntity(DateTime.now(), "test_def", "default", null, "", null))
  }

  @Test
  def canGetDefinitions() {
    val defsResource = new DefsResource
    val respBox = defsResource.get("/defs")

    Assert.assertTrue(respBox.isDefined)

    val resp = respBox.open_!.asInstanceOf[PlainTextResponse]
    Assert.assertEquals(resp.code, 200)
    Assert.assertEquals(resp.headers, List(("Content-Type", "application/vnd.ksmpartners.ernie+json")))
    Assert.assertEquals(resp.text, """{"reportDefMap":{"test_def":"/defs/test_def"}}""")
  }

  @Test
  def canGetDefinition() {
    val defsResource = new DefDetailResource
    val respBox = defsResource.get("test_def")

    Assert.assertTrue(respBox.isDefined)

    val resp = respBox.open_!.asInstanceOf[PlainTextResponse]
    Assert.assertEquals(resp.code, 200)
    Assert.assertEquals(resp.headers, List(("Content-Type", "application/vnd.ksmpartners.ernie+json")))
    val defEnt = mapper.readValue(resp.text, classOf[DefinitionEntity])
    Assert.assertEquals(defEnt.getCreatedUser, "default")
    Assert.assertEquals(defEnt.getDefDescription, "")
    Assert.assertEquals(defEnt.getDefId, "test_def")
    Assert.assertNull(defEnt.getParamNames)
  }

}
