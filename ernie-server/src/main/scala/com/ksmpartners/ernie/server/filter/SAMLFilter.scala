/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.server.filter

import javax.servlet._
import http.{ HttpServletRequestWrapper, HttpServletResponse, HttpServletRequest }
import org.slf4j.{ Logger, LoggerFactory }
import com.ksmpartners.commons.security.SAML2.{ SAMLProcessor, SAMLParseException }
import com.ksmpartners.commons.util.Base64Util
import org.apache.cxf.rs.security.saml.DeflateEncoderDecoder
import com.ksmpartners.ernie.util.FileUtils._
import com.ksmpartners.ernie.server.filter.SAMLConstants._
import com.ksmpartners.ernie.server.PropertyNames._
import scala.collection._
import java.io.InputStream

/**
 * Servlet filter used for SAML authentication
 */
class SAMLFilter extends Filter {

  private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.server.filter.SAMLFilter")

  private val keystoreLoc: String = {
    val ksl = System.getProperty(KEYSTORE_LOC_PROP)
    if (ksl == null)
      throw new IllegalStateException("Must set keystore.location")
    log.info("keystoreLoc = {}", ksl)
    ksl
  }

  def init(config: FilterConfig) {}

  def doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain) {
    (req, res) match {
      case (request: HttpServletRequest, response: HttpServletResponse) => {
        try {
          val samlRequestWrapper = handleRequest(request, response)
          chain.doFilter(samlRequestWrapper, response)
        } catch {
          case e: Exception => {
            log.info("Caught exception while handling request. Return 401. Exception: {}", e.getMessage)
            response.sendError(401, "Invalid or nonexistent credentials")
          }
        }
      }
      case _ => {
        log.info("SAMLFilter ignoring non-http request: {}", req.getProtocol)
      }
    }
  }

  private def handleRequest(req: HttpServletRequest, res: HttpServletResponse): HttpServletRequest = {
    // Get Authorization header value
    val samlTokenHeader = req.getHeader(AUTH_HEADER_PROP)

    // Null check
    if (samlTokenHeader == null || !samlTokenHeader.startsWith("SAML"))
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
      userName = (attr.get(USER_NAME_PROP).asInstanceOf[java.util.ArrayList[String]]).get(0)
      val rolesProp = attr.get(ROLES_PROP).asInstanceOf[java.util.ArrayList[java.lang.Object]].toArray
      for (role <- rolesProp) {
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

  final class SAMLHttpServletRequestWrapper(req: HttpServletRequest, userName: String, roles: Set[String])
      extends HttpServletRequestWrapper(req) {

    override def isUserInRole(role: String): Boolean = {
      log.debug("Checking if user [{}] is in role [{}]", userName, role)
      roles.contains(role)
    }

    override def getAuthType: String = {
      "SAML"
    }

  }

}