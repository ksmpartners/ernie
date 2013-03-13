/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.engine

import org.testng.annotations.Test
import java.io.{ IOException, Closeable }

class ActorsTest {

  @Test
  def testActors() {

    try_(new Cls("C")) { cls =>
      cls.doSomething()
    }

  }

  private class Cls(id: String) extends Closeable {

    private var closed = false

    def close() {
      println(id + ": Closed")
      closed = true
    }

    def doSomething() {
      if (!closed)
        println(id + ": Something...")
      else
        println(id + ": Can't do something...")
    }

  }

  private def try_[A <% Closeable](ac: A)(f: A => Unit) {
    try {
      f(ac)
    } finally {
      try {
        ac.close()
      } catch {
        case _ =>
      }
    }
  }
}
