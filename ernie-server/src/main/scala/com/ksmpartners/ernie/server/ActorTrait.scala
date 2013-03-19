package com.ksmpartners.ernie.server

import com.ksmpartners.ernie.engine
import com.ksmpartners.ernie.engine.Coordinator
import net.liftweb.util.Props

trait ActorTrait {
  val coordinator = new Coordinator(Props.get("rpt.def.dir").open_!, Props.get("output.dir").open_!).start()

  class ShutdownResource {
    def shutdown() {
      coordinator ! engine.ShutDownRequest
    }
  }
}