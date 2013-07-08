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
 */

package bootstrap.liftweb

import _root_.net.liftweb.http
import net.liftweb.http.{ Req, LiftRules }
import net.liftweb.http.provider.{ HTTPParam, HTTPRequest }
import com.ksmpartners.ernie.server.{ PropertyNames, DispatchRestAPI }
import net.liftweb.http.auth.{ AuthRole, userRoles }
import com.ksmpartners.ernie.server.filter.SAMLConstants
import org.slf4j.{ LoggerFactory, Logger }
import net.liftweb.common.Full
import net.liftweb.mockweb.MockWeb
import javax.servlet.http.HttpServletRequest

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {

  def boot() {

    LiftRules.early.append(makeUtf8)

    DispatchRestAPI.init()

    LiftRules.statelessDispatch.prepend(DispatchRestAPI)

    if (System.getProperty(PropertyNames.authModeProp) == "BASIC") {
      LiftRules.authentication = net.liftweb.http.auth.HttpBasicAuthentication("Ernie Server")(DispatchRestAPI.basicAuthentication)
      LiftRules.httpAuthProtectedResource.prepend(DispatchRestAPI.protectedResources)
    }

    LiftRules.supplimentalHeaders = s => s.addHeaders(
      List(HTTPParam("X-Lift-Version", LiftRules.liftVersion),
        HTTPParam("Access-Control-Allow-Origin", "*"),
        HTTPParam("Access-Control-Allow-Credentials", "true"),
        HTTPParam("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, HEAD, OPTIONS"),
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
