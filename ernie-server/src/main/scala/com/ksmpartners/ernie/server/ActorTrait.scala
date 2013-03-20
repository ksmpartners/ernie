/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.server

import com.ksmpartners.ernie.engine
import com.ksmpartners.ernie.engine.Coordinator
import net.liftweb.util.Props

/**
 * Trait that contains and maintains the actor(s) for coordinating report creation
 */
trait ActorTrait {
  val coordinator = new Coordinator(Props.get("rpt.def.dir").open_!, Props.get("output.dir").open_!).start()

  class ShutdownResource {
    def shutdown() {
      coordinator ! engine.ShutDownRequest
    }
  }
}