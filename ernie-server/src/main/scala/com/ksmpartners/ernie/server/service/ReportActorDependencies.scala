/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.server.service

import com.ksmpartners.ernie.engine

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
      coordinator ! engine.ShutDownRequest()
    }
  }
}
