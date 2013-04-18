/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.util

import java.io.Closeable

object FileUtils {

  /**
   * Method that mimics Java 1.7's try-with-resources
   *
   * Usage:
   * try_(new Closable...) { closableInstance =>
   *   closableInstance.doSomething()
   * }
   */
  def try_[A <: Closeable](closeable: A)(tryBlock: A => Unit) {
    try {
      tryBlock(closeable)
    } finally {
      try {
        closeable.close()
      } catch {
        case e: Throwable =>
      }
    }
  }

  /**
   * Method that mimics Java 1.7's try-with-resources with a catch block
   *
   * Usage:
   * try_catch(new Closable...) { closableInstance =>
   *   closableInstance.doSomething()
   * } {
   *   case e => handleThrowable(e)
   * }
   */
  def try_catch[A <: Closeable](closeable: A)(tryBlock: A => Unit)(catchBlock: Throwable => Unit) {
    try {
      tryBlock(closeable)
    } catch {
      case e: Throwable => catchBlock(e)
    }
    finally {
      try {
        closeable.close()
      } catch {
        case e: Throwable =>
      }
    }
  }
}
