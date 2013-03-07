/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.engine

import org.testng.annotations.Test
import actors.Actor
import actors.Actor._
import java.util.Date

class ActorsTest {

  val time = System.currentTimeMillis()

  def testActors() {

    val singleActor = new TestActor
    singleActor.start()

    println(Thread.currentThread())
    println("[" + new Date(System.currentTimeMillis()) + "] BEGIN Sending messages")
    for (i <- 1 to 10) {
      val multiActor = actor {
        loop {
          react {
            case msg =>
              println("[" + new Date(System.currentTimeMillis()) + "] Anon Actor received message \"" + msg + "\" on thread " + Thread.currentThread() + " in " + (System.currentTimeMillis() - time) + " msecs")
              Thread.sleep(5000)
              println("[" + new Date(System.currentTimeMillis()) + "] Anon Actor end " + Thread.currentThread())
          }
        }
      }
      multiActor ! "Message-" + i
      singleActor ! "Message-" + i
    }
    println("[" + new Date(System.currentTimeMillis()) + "] END Sending messages")

    Thread.sleep(60000)

  }

  class TestActor extends Actor {

    def act {
      loop {
        react {
          case msg =>
            println("[" + new Date(System.currentTimeMillis()) + "] Instantiated Actor received message \"" + msg + "\" on thread " + Thread.currentThread() + " in " + (System.currentTimeMillis() - time) + " msecs")
            Thread.sleep(5000)
            println("[" + new Date(System.currentTimeMillis()) + "] Instantiated Actor end " + Thread.currentThread())
        }
      }
    }

  }

}
