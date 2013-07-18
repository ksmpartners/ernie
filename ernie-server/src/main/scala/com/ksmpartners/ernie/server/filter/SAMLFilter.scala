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

import javax.servlet._
import http.{ HttpServletRequestWrapper, HttpServletResponse, HttpServletRequest }
import org.slf4j.{ Logger, LoggerFactory }
import com.ksmpartners.ernie.util.{ SAMLParseException, SAMLProcessor }
import com.ksmpartners.ernie.util.Base64Util
import org.apache.cxf.rs.security.saml.DeflateEncoderDecoder
import com.ksmpartners.ernie.util.Utility._
import com.ksmpartners.ernie.server.filter.SAMLConstants._
import com.ksmpartners.ernie.server.PropertyNames._
import com.ksmpartners.ernie.server.filter.SAMLFilter._
import scala.collection._
import java.io.InputStream

/**
 * Servlet filter used for SAML authentication.
 */
class SAMLFilter extends Filter {

  private val keystoreLoc: String = {
    val ksl = System.getProperty(keystoreLocProp)
    if (ksl == null)
      throw new IllegalStateException("Must set " + keystoreLocProp)
    log.info("keystoreLoc = {}", ksl)
    ksl
  }

  def init(config: FilterConfig) {}

  def doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain) {
    (req, res) match {
      case (request: HttpServletRequest, response: HttpServletResponse) => {
        try {
          val uri = req.asInstanceOf[HttpServletRequest].getRequestURI
          val samlRequestWrapper = handleRequest(request, response)
          chain.doFilter(samlRequestWrapper, response)
        } catch {
          case e: Exception => {
            log.debug("Caught exception while handling request. Return 401. Exception: {}", e.getMessage)
            response.sendError(401, "Invalid or nonexistent credentials")
          }
        }
      }
      case _ => {
        log.debug("SAMLFilter ignoring non-http request: {}", req.getProtocol)
      }
    }
  }

  private def handleRequest(req: HttpServletRequest, res: HttpServletResponse): HttpServletRequest = {
    // Get Authorization header value
    val samlTokenHeader = req.getHeader(authHeaderProp)
    // Null check
    if (samlTokenHeader == null)
      throw new SAMLParseException("No Authorization token in HTTP Request.")
    if (!samlTokenHeader.startsWith("SAML"))
      throw new SAMLParseException("No SAML token in HTTP Request.")
    // Header value fits format: SAML ENCODED_TOKEN
    // ENCODED_TOKEN = DEFLATED + Base64 encoded
    // Need to Decode, then INFLATE
    val encodedSamlToken = samlTokenHeader.substring(samlTokenHeader.indexOf(" "), samlTokenHeader.length)
    val deflatedSamlToken = Base64Util.decode(encodedSamlToken.getBytes("UTF-8"))

    var userName: String = null
    var roles: Set[String] = new mutable.HashSet[String]

    try_(new DeflateEncoderDecoder().inflateToken(deflatedSamlToken)) { samlTokenStream =>
      val samlProcessor = getSAMLProcessor(samlTokenStream)

      val attr = samlProcessor.getAttributes
      userName = (attr.get(userNameProp).asInstanceOf[java.util.ArrayList[String]]).get(0)
      val rolesPropObj = attr.get(rolesProp).asInstanceOf[java.util.ArrayList[java.lang.Object]].toArray
      for (role <- rolesPropObj) {
        roles += role.toString
      }
    }
    new SAMLHttpServletRequestWrapper(req, userName, roles)
  }

  private def getSAMLProcessor(samlTokenStream: InputStream): SAMLProcessor = {
    val samlProcessor = new SAMLProcessor

    samlProcessor.setKeystoreLocation(keystoreLoc)

    samlProcessor.parse(samlTokenStream)
    samlProcessor.validate()
    samlProcessor
  }

  def destroy() {}

}

/**
 * Singleton companion for SAMLFilter.
 */
object SAMLFilter {
  private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.server.filter.SAMLFilter")

  class SAMLHttpServletRequestWrapper(req: HttpServletRequest, userName: String, roles: Set[String])
      extends HttpServletRequestWrapper(req) {
    override def isUserInRole(role: String): Boolean = roles.contains(role)
    override def getAuthType: String = "SAML"
    override def getRemoteUser: String = {
      userName
    }
  }
}