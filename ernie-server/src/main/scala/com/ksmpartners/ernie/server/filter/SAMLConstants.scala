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
  val AUTH_HEADER_PROP = "Authorization"
  val USER_NAME_PROP = "userName"
  val ROLES_PROP = "ernieRole"

  val READ_ROLE = "read"
  val WRITE_ROLE = "write"
}