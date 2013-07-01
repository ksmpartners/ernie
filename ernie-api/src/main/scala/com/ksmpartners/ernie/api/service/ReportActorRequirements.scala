/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.api.service

import com.ksmpartners.ernie.engine.ErnieCoordinator
import com.ksmpartners.ernie.engine.report.ReportManager
import akka.actor.{ ActorRef, ActorSystem }
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit
import akka.util.Timeout

/**
 * Trait that indicates a requirement on a Coordinator
 */
trait RequiresCoordinator {
  protected def coordinator: ActorRef
  //protected def timeout: Long
  def timeoutDuration: FiniteDuration
  implicit def timeoutAkka = Timeout(timeoutDuration)
  //protected def workerCount: Int
  //protected def jobsDir: String
  protected val system: ActorSystem
}

/**
 * Trait that indicates a requirement on a ReportManager
 */
trait RequiresReportManager {
  protected def reportManager: ReportManager
  //protected def outputDir: String
  //protected def defDir: String
}