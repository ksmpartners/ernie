package com.ksmpartners.ernie
/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

import io.gatling.com.ksmpartners.ErnieGatling
import io.gatling.core.Predef._
import bootstrap._
import java.lang.String
import scala.concurrent.duration._
import scala.Some
import com.ksmpartners.ernie.model.ReportType

/**
 * Created with IntelliJ IDEA.
 * User: acoimbra
 * Date: 6/28/13
 * Time: 10:20 AM
 * To change this template use File | Settings | File Templates.
 */

object ErnieSimulation {
  def scn(s: String, e: ErnieGatling) = {
    scenario(s)
      .exec(session => {
        session.set("postCount", range(1, 5))
          .set("resCount", range(4, 8))
          .set("defs", List())
      })
      .exec(e.createDef(Thread.currentThread.getContextClassLoader.getResource("test_def_params.rptdesign").getPath))
      .exec(e.getDefs)
      .repeat(5) {
        randomSwitch(
          50 -> exec(e.postJob(Some("${defs(0)}"), ReportType.PDF)).exec(e.getResult(None)),
          50 -> exec(e.postJob(Some("${defs(0)}"), ReportType.PDF))).repeat(5) {
            exec(session => {
              val jobs = session.get[List[Long]]("jobs") getOrElse List.empty[Long]
              session.set("currentJob", scala.util.Random.shuffle(jobs).headOption getOrElse (session.get[Long]("currentJob") getOrElse -1L))
            })
            randomSwitch(
              60 -> exec(e.getResult(None)),
              40 -> pause(1 second))
          }
      }
  }
  def range(start: Int, end: Int): Int = start + (math.random.toInt % end)
}