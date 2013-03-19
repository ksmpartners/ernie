/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.server

object ServiceRegistry extends JobDependencies
    with DefinitionDependencies {

  val jobsResource = new JobsResource
  val jobStatusResource = new JobStatusResource
  val jobResultsResource = new JobResultsResource

  val defsResource = new DefsResource

  val shutdownResource = new ShutdownResource

}
