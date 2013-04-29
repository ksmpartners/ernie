/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.server.filter

import net.liftweb.http.Req
import net.liftweb.http.provider.HTTPRequest
import net.liftweb.http.provider.servlet.HTTPRequestServlet
import javax.servlet.http.HttpServletRequest

/**
 * Utility object with methods for handling authorization
 */
object AuthUtil {

  def isUserInRole(req: Req, role: String): Boolean = {
    val httpRequest: HTTPRequest = req.request
    if (httpRequest == null)
      throw new IllegalStateException("Request is null")
    val hrs = httpRequest.asInstanceOf[HTTPRequestServlet]
    val hsr: HttpServletRequest = hrs.req
    hsr.isUserInRole(role)
  }

}
