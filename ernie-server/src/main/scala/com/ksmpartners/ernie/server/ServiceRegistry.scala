package com.ksmpartners.ernie.server

object ServiceRegistry extends JobDependencies {

  val jobsResource = new JobsResource
  val jobStatusResource = new JobStatusResource
  val jobResultsResource = new JobResultsResource

  val shutdownResource = new ShutdownResource

}
