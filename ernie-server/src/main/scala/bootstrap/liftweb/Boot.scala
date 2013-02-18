package bootstrap.liftweb

import _root_.net.liftweb.http
import http.LiftRules
import http.provider.HTTPRequest
import com.ksmpartners.ernie.server.DispatchRestAPI

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {

    LiftRules.early.append(makeUtf8)

    LiftRules.statelessDispatchTable.prepend(DispatchRestAPI.dispatch)
  }


  /**
   * Force the request to be UTF-8
   */
  private def makeUtf8(req: HTTPRequest) {
    req.setCharacterEncoding("UTF-8")
  }

}
