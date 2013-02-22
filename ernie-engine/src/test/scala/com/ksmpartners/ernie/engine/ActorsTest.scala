package com.ksmpartners.ernie.engine

import org.testng.annotations.Test

class ActorsTest {

  @Test
  def testActors() {
    Coordinator.start()
    Worker.start()
    Coordinator ! Request(0, null)
    Coordinator ! Request(1, null)
    Coordinator ! Request(2, null)
    Thread.sleep(5000)
  }

}
