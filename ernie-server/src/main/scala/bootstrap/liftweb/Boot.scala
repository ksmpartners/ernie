/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package bootstrap.liftweb

import _root_.net.liftweb.http
import net.liftweb.http.{ Req, LiftRules }
import net.liftweb.http.provider.{ HTTPParam, HTTPRequest }
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

    LiftRules.supplimentalHeaders = s => s.addHeaders(
      List(HTTPParam("X-Lift-Version", LiftRules.liftVersion),
        HTTPParam("Access-Control-Allow-Origin", "*"),
        HTTPParam("Access-Control-Allow-Credentials", "true"),
        HTTPParam("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS"),
        HTTPParam("Access-Control-Allow-Headers", "Authorization,WWW-Authenticate,Keep-Alive,User-Agent,X-Requested-With,Cache-Control,Content-Type"),
        HTTPParam("Access-Control-Request-Headers", "Authorization,WWW-Authenticate,Keep-Alive,User-Agent,X-Requested-With,Cache-Control,Content-Type")))

    LiftRules.unloadHooks.prepend(() => DispatchRestAPI.shutdown())

    LiftRules.liftRequest.append {

      case Req("static" :: _, _, _) => false

    }
  }

  /**
   * Force the request to be UTF-8
   */
  private def makeUtf8(req: HTTPRequest) {
    req.setCharacterEncoding("UTF-8")
  }

}
