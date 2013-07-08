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

package com.ksmpartners.ernie.server.filter

import net.liftweb.http.Req
import net.liftweb.http.provider.HTTPRequest
import net.liftweb.http.provider.servlet.HTTPRequestServlet
import javax.servlet.http.HttpServletRequest
import com.ksmpartners.ernie.server.filter.SAMLConstants._
import org.slf4j.{ LoggerFactory, Logger }

/**
 * Utility object with methods for handling authorization.
 */
object AuthUtil {
  private val logd: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.server.filter.AuthUtil")

  /**
   * Determines if requesting user is in the provided role.
   */
  def isUserInRole(req: Req, role: String): Boolean = {
    reqToHSR(req).isUserInRole(role)
  }

  /**
   * Return a list of roles for the requesting user.
   * @param req extract roles from this request
   * @return a list of roles
   */
  def getRoles(req: Req): List[String] = {
    val hsr = reqToHSR(req)
    var lst: List[String] = Nil
    if (hsr.isUserInRole(writeRole)) lst = writeRole :: lst
    if (hsr.isUserInRole(readRole)) lst = readRole :: lst
    if (hsr.isUserInRole(runRole)) lst = runRole :: lst
    if (hsr.isUserInRole(writeRole) && hsr.isUserInRole(runRole)) lst = (writeRole + "," + runRole) :: lst
    lst
  }

  /**
   * Returns a userName for the requesting user.
   * @param req extract userName from this request
   */
  def getUserName(req: Req) = {
    reqToHSR(req).getRemoteUser
  }

  private def reqToHSR(req: Req): HttpServletRequest = {
    val httpRequest: HTTPRequest = req.request
    if (httpRequest == null)
      throw new IllegalStateException("Request is null")
    val hrs = httpRequest.asInstanceOf[HTTPRequestServlet]
    hrs.req
  }
}
