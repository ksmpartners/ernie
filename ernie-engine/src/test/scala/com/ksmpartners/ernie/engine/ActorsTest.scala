package com.ksmpartners.ernie.engine

import org.testng.annotations.Test

class ActorsTest {

  @Test
  def testActors() {
    Coordinator.start()
    Worker.start()
    for ( i <- 0 to 2) {
      Coordinator ! ReportRequest(i)
    }
    Thread.sleep(5000)
  }

}
