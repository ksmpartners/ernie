/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.server.filter

import javax.servlet._
import javax.servlet.http.{ HttpServletResponse, HttpServletRequest, HttpServletRequestWrapper }
import com.ksmpartners.ernie.server.PropertyNames._

/**
 * Filter that delegates filtering based on the value of authentication.mode variable
 */
class FilterWrapper extends Filter {

  private var wrappedFilter: Filter = null

  def init(config: FilterConfig) {
    val authMode = System.getProperty(authModeProp)

    authMode match {
      case "SAML" => wrappedFilter = new SAMLFilter
      case _ => wrappedFilter = new PassThroughFilter
    }

    wrappedFilter.init(config)
  }

  def doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain) {
    res.asInstanceOf[HttpServletResponse].addHeader("Access-Control-Allow-Origin", "*")
    res.asInstanceOf[HttpServletResponse].addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
    res.asInstanceOf[HttpServletResponse].addHeader("Access-Control-Request-Headers", "Authorization,WWW-Authenticate,Keep-Alive,User-Agent,X-Requested-With,Cache-Control,Content-Type")
    res.asInstanceOf[HttpServletResponse].addHeader("Access-Control-Allow-Headers", "Authorization,WWW-Authenticate,Keep-Alive,User-Agent,X-Requested-With,Cache-Control,Content-Type")

    wrappedFilter.doFilter(req, res, chain)
  }

  def destroy() { wrappedFilter.destroy() }

  /**
   * Filter that wraps each request with a wrapper that allows full authorization
   */
  private class PassThroughFilter extends Filter {

    def init(config: FilterConfig) {}

    def doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain) {
      req match { case request: HttpServletRequest => chain.doFilter(new PassThroughAuthRequest(request), res) }
    }

    def destroy() {}

    private class PassThroughAuthRequest(req: HttpServletRequest) extends HttpServletRequestWrapper(req) {
      override def isUserInRole(role: String): Boolean = true
      override def getAuthType: String = "None"
    }

  }

}
