package com.ksmpartners.ernie.engine

import org.testng.annotations.Test

class ActorsTest {

  def testActors() {
    val rptGen = new ReportGenerator("./ernie-engine/src/main/resources", "./ernie-engine/src/main/resources/output")

    val coord = new Coordinator(rptGen)

    coord.start()

    coord ! ReportRequest("Report-" + 0)

    Thread.sleep(60000)
  }

}
