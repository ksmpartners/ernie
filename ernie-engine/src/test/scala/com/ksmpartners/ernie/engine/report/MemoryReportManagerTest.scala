/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.engine.report

import org.testng.annotations.{ BeforeMethod, Test }
import org.testng.Assert
import java.io.IOException
import com.ksmpartners.ernie.model.ReportType

class MemoryReportManagerTest {

  private var reportManager: MemoryReportManager = new MemoryReportManager

  @BeforeMethod
  def setup() {
    reportManager = new MemoryReportManager
    reportManager.putDefinition("def_1", "DEF_1".getBytes())
    reportManager.putDefinition("def_2", "DEF_2".getBytes())
    reportManager.putDefinition("def_3", "DEF_3".getBytes())
    reportManager.putDefinition("def_4", "DEF_4".getBytes())
    reportManager.putDefinition("def_5", "DEF_5".getBytes())
    reportManager.putReport("rpt_1", "RPT_1".getBytes())
    reportManager.putReport("rpt_2", "RPT_2".getBytes())
    reportManager.putReport("rpt_3", "RPT_3".getBytes())
    reportManager.putReport("rpt_4", "RPT_4".getBytes())
    reportManager.putReport("rpt_5", "RPT_5".getBytes())
  }

  @Test
  def testPut() {
    val bosR = reportManager.putReport("rpt_6", ReportType.CSV)
    Assert.assertFalse(reportManager.hasReport("rpt_6"))
    bosR.close()
    Assert.assertTrue(reportManager.hasReport("rpt_6"))

    val bosD = reportManager.putDefinition("def_6")
    Assert.assertFalse(reportManager.hasDefinition("def_6"))
    bosD.close()
    Assert.assertTrue(reportManager.hasDefinition("def_6"))
  }

  @Test()
  def testGet() {
    val buf: Array[Byte] = new Array(5)
    reportManager.getDefinition("def_1").read(buf)
    Assert.assertEquals(buf, "DEF_1".getBytes)
    reportManager.getReport("rpt_1").read(buf)
    Assert.assertEquals(buf, "RPT_1".getBytes)
  }

  @Test
  def testHas() {
    Assert.assertTrue(reportManager.hasDefinition("def_1"))
    Assert.assertTrue(reportManager.hasDefinition("def_2"))
    Assert.assertTrue(reportManager.hasDefinition("def_3"))
    Assert.assertTrue(reportManager.hasDefinition("def_4"))
    Assert.assertTrue(reportManager.hasDefinition("def_5"))
    Assert.assertTrue(reportManager.hasReport("rpt_1"))
    Assert.assertTrue(reportManager.hasReport("rpt_2"))
    Assert.assertTrue(reportManager.hasReport("rpt_3"))
    Assert.assertTrue(reportManager.hasReport("rpt_4"))
    Assert.assertTrue(reportManager.hasReport("rpt_5"))
    Assert.assertFalse(reportManager.hasReport("def_1"))
    Assert.assertFalse(reportManager.hasReport("def_2"))
    Assert.assertFalse(reportManager.hasReport("def_3"))
    Assert.assertFalse(reportManager.hasReport("def_4"))
    Assert.assertFalse(reportManager.hasReport("def_5"))
    Assert.assertFalse(reportManager.hasDefinition("rpt_1"))
    Assert.assertFalse(reportManager.hasDefinition("rpt_2"))
    Assert.assertFalse(reportManager.hasDefinition("rpt_3"))
    Assert.assertFalse(reportManager.hasDefinition("rpt_4"))
    Assert.assertFalse(reportManager.hasDefinition("rpt_5"))
  }

  @Test
  def testDelete() {
    reportManager.deleteDefinition("def_1")
    reportManager.deleteDefinition("def_2")
    reportManager.deleteDefinition("def_3")
    reportManager.deleteDefinition("def_4")
    reportManager.deleteDefinition("def_5")
    reportManager.deleteReport("rpt_1")
    reportManager.deleteReport("rpt_2")
    reportManager.deleteReport("rpt_3")
    reportManager.deleteReport("rpt_4")
    reportManager.deleteReport("rpt_5")
    Assert.assertFalse(reportManager.hasReport("def_1"))
    Assert.assertFalse(reportManager.hasReport("def_2"))
    Assert.assertFalse(reportManager.hasReport("def_3"))
    Assert.assertFalse(reportManager.hasReport("def_4"))
    Assert.assertFalse(reportManager.hasReport("def_5"))
    Assert.assertFalse(reportManager.hasDefinition("rpt_1"))
    Assert.assertFalse(reportManager.hasDefinition("rpt_2"))
    Assert.assertFalse(reportManager.hasDefinition("rpt_3"))
    Assert.assertFalse(reportManager.hasDefinition("rpt_4"))
    Assert.assertFalse(reportManager.hasDefinition("rpt_5"))
  }

  @Test
  def testGetAll() {
    Assert.assertEquals(reportManager.getAllDefinitionIds.sortWith({ (x, y) => x < y }),
      List("def_1", "def_2", "def_3", "def_4", "def_5"))
    Assert.assertEquals(reportManager.getAllReportIds.sortWith({ (x, y) => x < y }),
      List("rpt_1", "rpt_2", "rpt_3", "rpt_4", "rpt_5"))
  }

  @Test(expectedExceptions = Array(classOf[IOException]))
  def missingReportThrowsExceptionOnGet() {
    reportManager.getReport("FAIL")
  }

  @Test(expectedExceptions = Array(classOf[IOException]))
  def missingDefinitionThrowsExceptionOnGet() {
    reportManager.getDefinition("FAIL")
  }

}
