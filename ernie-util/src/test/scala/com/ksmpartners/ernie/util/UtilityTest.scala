/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.util

import com.ksmpartners.ernie.util.Utility._
import org.testng.annotations.Test
import java.io.Closeable
import org.testng.Assert

class UtilityTest {

  @Test
  def canCloseReaderWithTry() {
    val cls = new Cls
    cls.open()
    Assert.assertFalse(cls.isClosed)
    try_(cls) { reader =>
      reader.doSomething()
      reader.close()
    }
    Assert.assertTrue(cls.isClosed)
  }

  @Test
  def canUseTryCatch() {
    val cls = new Cls
    cls.open()
    Assert.assertFalse(cls.isClosed)
    var caughtEx = false
    try_catch(cls) { reader =>
      reader.doSomething()
      reader.throwEx()
    } { ex =>
      caughtEx = true
    }
    Assert.assertTrue(cls.isClosed)
    Assert.assertTrue(caughtEx)
  }

  @Test
  def catchBlockExceptionsHandledProperly() {
    val cls = new Cls
    cls.open()
    Assert.assertFalse(cls.isClosed)
    try {
      try_catch(cls) { reader =>
        reader.doSomething()
        reader.throwEx()
      } { ex =>
        throw new IllegalStateException("Exception AWAY!!")
      }
    } catch { case e => }
    Assert.assertTrue(cls.isClosed)
  }

}

class Cls extends Closeable {

  var isClosed = true

  def doSomething() {
    if (isClosed)
      throw new IllegalStateException("Cls is closed")
  }

  def throwEx() {
    throw new IllegalStateException("Exception away!")
  }

  def close() {
    if (isClosed)
      throw new IllegalStateException("Cls is already closed")
    isClosed = true
  }

  def open() {
    if (!isClosed)
      throw new IllegalStateException("Cls is already open")
    isClosed = false
  }
}