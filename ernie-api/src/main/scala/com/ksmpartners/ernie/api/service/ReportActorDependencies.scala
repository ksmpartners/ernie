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

import com.ksmpartners.ernie.engine
import akka.pattern.ask

/**
 * Trait that contains and maintains the actor(s) for coordinating report creation
 */
trait ReportActorDependencies extends RequiresCoordinator {

  /**
   * Resource for handling the shutdown process of the Actors
   */
  class ShutdownResource {
    /**
     * Sends a shutdown request to the coordinator
     */
    def shutdown() {
      coordinator ! (engine.ShutDownRequest())
    }
  }
}
