/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.util

import org.testng.annotations.Test

class TestLoggerTest extends TestLogger {

  @Test
  def ccLogger() {
    logMethodBefore(getClass.getMethod("ccLogger"))
    logMethodAfter(getClass.getMethod("ccLogger"))
  }

}
