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
import net.liftweb.http.{ ResponseWithReason, PlainTextResponse }
import com.ksmpartners.ernie.model._
import org.joda.time.DateTime
import com.ksmpartners.ernie.util.Utility._
import com.ksmpartners.ernie.util.MapperUtility._
import com.ksmpartners.ernie.engine.Coordinator
import org.slf4j.{ LoggerFactory, Logger }
import com.ksmpartners.ernie.util.TestLogger
import net.liftweb.mockweb.MockWeb
import com.ksmpartners.ernie.server.DispatchRestAPI
import net.liftweb.mocks.MockHttpServletRequest
import com.ksmpartners.ernie.server.filter.SAMLConstants
import java.io.{ FileInputStream, File }
import com.ksmpartners.ernie.api.ErnieAPI

class DefinitionDependenciesTest extends DefinitionDependencies with TestLogger with RequiresAPI {

  val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.server.DefDependenciesTest")

  protected val ernie =
    ErnieAPI(createTempDirectory.getAbsolutePath, createTempDirectory.getAbsolutePath, createTempDirectory.getAbsolutePath, 300000L, 7, 14)

  var defId = ""

  @BeforeClass
  def setup() {
    val file = new File(Thread.currentThread.getContextClassLoader.getResource("in/test_def_params.rptdesign").getPath)

    defId = ernie.createDefinition(Some(Right(scala.xml.XML.loadFile(file).toString.getBytes)), "Test def", "testUser").defEnt.get.getDefId
    //  reportManager.putDefinition("test_def", byteArr, new DefinitionEntity(DateTime.now(), "test_def", "default", null, "", null, null))
  }

  @Test
  def canGetDefinitions() {
    val defsResource = new DefsResource
    val respBox = defsResource.get("/defs")

    Assert.assertTrue(respBox.isDefined)

    val resp = respBox.open_!.asInstanceOf[PlainTextResponse]
    Assert.assertEquals(resp.code, 200)
    Assert.assertEquals(resp.headers, List(("Content-Type", "application/vnd.ksmpartners.ernie+json")))
  }

  @Test
  def canGetDefinition() {
    val defsResource = new DefDetailResource
    val respBox = defsResource.get(defId)

    Assert.assertTrue(respBox.isDefined)

    val resp = respBox.open_!.asInstanceOf[PlainTextResponse]
    Assert.assertEquals(resp.code, 200)
    Assert.assertEquals(resp.headers, List(("Content-Type", "application/vnd.ksmpartners.ernie+json")))
    val defEnt = mapper.readValue(resp.text, classOf[DefinitionEntity])
    Assert.assertEquals(defEnt.getCreatedUser, "testUser")
    Assert.assertEquals(defEnt.getDefId, defId)
    Assert.assertNull(defEnt.getParamNames)
  }
}

