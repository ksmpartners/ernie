/*
	Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/

package com.ksmpartners.ernie.api.service

import com.ksmpartners.ernie.engine.report.ReportManager
import akka.actor.{ ActorRef, ActorSystem }
import scala.concurrent.duration.FiniteDuration
import akka.util.Timeout

/**
 * Trait that indicates a requirement on a Coordinator
 */
trait RequiresCoordinator {
  protected def coordinator: ActorRef
  def timeoutDuration: FiniteDuration
  implicit def timeoutAkka = Timeout(timeoutDuration)
  protected val system: ActorSystem
}

/**
 * Trait that indicates a requirement on a ReportManager
 */
trait RequiresReportManager {
  protected def reportManager: ReportManager
}