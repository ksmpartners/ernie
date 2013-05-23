/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.server.filter

/**
 * Object containing constants for use with SAML authentication
 */
object SAMLConstants {
  val authHeaderProp = "Authorization"
  val userNameProp = "userName"
  val rolesProp = "ernieRole"

  val readRole = "read"
  val writeRole = "write"
}