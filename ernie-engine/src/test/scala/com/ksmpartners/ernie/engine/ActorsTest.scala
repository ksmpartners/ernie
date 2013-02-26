package com.ksmpartners.ernie.engine

import org.testng.annotations.Test

class ActorsTest {

  def testActors() {
    Worker.setRptGenerator(new ReportGenerator("./ernie-engine/src/main/resources", "./ernie-engine/src/main/resources/output", "PDF"))
    Worker.startRptGenerator
    Coordinator.start()
    Worker.start()

    Coordinator ! ReportRequest("Report-" + 0)

    Thread.sleep(60000)
    Worker.stopRptGenerator
  }

}
