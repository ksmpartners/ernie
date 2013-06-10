/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package bootstrap.liftweb

import _root_.net.liftweb.http
import net.liftweb.http.LiftRules
import http.provider.HTTPRequest
import com.ksmpartners.ernie.server.DispatchRestAPI

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {

  def boot() {

    LiftRules.early.append(makeUtf8)

    DispatchRestAPI.init()

    LiftRules.statelessDispatch.prepend(DispatchRestAPI)

    LiftRules.unloadHooks.prepend(() => DispatchRestAPI.shutdown())
  }

  /**
   * Force the request to be UTF-8
   */
  private def makeUtf8(req: HTTPRequest) {
    req.setCharacterEncoding("UTF-8")
  }

}
