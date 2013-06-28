package com.ksmpartners.ernie
/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

import com.ksmpartners.ernie.api.ErnieAPI
import io.gatling.com.ksmpartners.Predef._
import io.gatling.core.Predef._
import bootstrap._
import java.lang.{ Throwable, String }
import scala.concurrent.duration._
import io.gatling.core.Predef._
import com.ksmpartners.ernie.model.ReportType
import java.io.{ File, IOException }
import scala.Predef._
import scala.Predef.String
import scala.Some
import com.ksmpartners.ernie.util.Utility._
import io.gatling.com.ksmpartners.{ ErnieHttp, ErnieBuilder }
import io.gatling.com.ksmpartners._
import ErnieSimulation._

class ErnieApiSimulation extends Simulation {

  lazy val api = {
    val defsDir = createTempDirectory
    org.apache.commons.io.FileUtils.copyFile(new File(Thread.currentThread.getContextClassLoader.getResource("test_def.rptdesign").getPath), new File(defsDir, "test_def.rptdesign"))
    org.apache.commons.io.FileUtils.copyFile(new File(Thread.currentThread.getContextClassLoader.getResource("test_def_params.rptdesign").getPath), new File(defsDir, "test_def_params.rptdesign"))
    ErnieAPI(createTempDirectory.getAbsolutePath, defsDir.getAbsolutePath, createTempDirectory.getAbsolutePath, 5000L, 7, 14, 999)
  }

  def getADef(defsDir: File, defaultDef: String = "test_def.rptdesign"): String = try {
    defsDir.listFiles.apply(math.random.toInt % defsDir.listFiles.size).getAbsolutePath
  } catch {
    case _: Throwable => defaultDef
  }

  var e = ernie(api)

  setUp(scn("ErnieAPI", e).inject(ramp(100 users) over (1 seconds)))

}
