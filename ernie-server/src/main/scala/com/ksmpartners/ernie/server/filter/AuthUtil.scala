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
import com.ksmpartners.ernie.server.filter.SAMLConstants._

/**
 * Utility object with methods for handling authorization
 */
object AuthUtil {

  /**
   * Determines if requesting user is in the provided role
   */
  def isUserInRole(req: Req, role: String): Boolean = reqToHSR(req).isUserInRole(role)

  /**
   * Return a list of roles the requesting user has
   */
  def getRoles(req: Req): List[String] = {
    val hsr = reqToHSR(req)
    var lst: List[String] = Nil
    if (hsr.isUserInRole(writeRole)) lst = writeRole :: lst
    if (hsr.isUserInRole(readRole)) lst = readRole :: lst
    lst
  }

  /**
   * Returns a userName for the requesting user
   */
  def getUserName(req: Req) = reqToHSR(req).getRemoteUser

  def reqToHSR(req: Req): HttpServletRequest = {
    val httpRequest: HTTPRequest = req.request
    if (httpRequest == null)
      throw new IllegalStateException("Request is null")
    val hrs = httpRequest.asInstanceOf[HTTPRequestServlet]
    hrs.req
  }
}
