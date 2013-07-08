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

package com.ksmpartners.ernie

import io.gatling.com.ksmpartners.ErnieGatling
import io.gatling.core.Predef._
import bootstrap._
import java.lang.String
import scala.concurrent.duration._
import scala.Some
import com.ksmpartners.ernie.model.ReportType

/**
 * Provides user scenario(s) that can be used to stress test any Ernie protocol
 */
object ErnieSimulation {
  /**
   * Execute a basic scenario:
   1. Create a new definition
   1. Get all available definitions
   1. Repeat 5 times:
    - 50% of the time, create a job and immediately retrieve the output
    - 50% of the time, create a job and repeat the following 5 times:
       1. Select a random job
       1.
        - 60% of the time get that job's output
        - 40% of the time pause for 1 seconds
   */
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