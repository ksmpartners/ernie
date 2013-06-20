/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.util

import org.testng.annotations.{ Test, BeforeMethod, AfterMethod }
import org.slf4j.{ LoggerFactory, Logger }

trait TestLogger {

  @Test(enabled = false)
  private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.server.TestLogger")

  @AfterMethod
  def logMethodAfter(result: java.lang.reflect.Method) {
    log.debug("END test: " + result.getName)
  }

  @BeforeMethod
  def logMethodBefore(result: java.lang.reflect.Method) {
    log.debug("BEGIN test: " + result.getName)
  }

}
