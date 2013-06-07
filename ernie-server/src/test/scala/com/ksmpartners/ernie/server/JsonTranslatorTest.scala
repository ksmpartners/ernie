/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.server

import org.testng.annotations.Test
import org.testng.Assert
import com.ksmpartners.ernie.model.{ JobStatus, StatusResponse }

class JsonTranslatorTest extends JsonTranslator {

  @Test
  def canSerializeModelObjects() {
    val statusResponse = new StatusResponse(JobStatus.COMPLETE)
    val statusResponseJson = serialize(statusResponse)
    Assert.assertEquals(statusResponseJson, """{"jobStatus":"COMPLETE"}""")
  }

  @Test
  def canDeserializeModelObjectStrings() {
    val statusResponse = deserialize("""{"jobStatus":"COMPLETE"}""", new StatusResponse().getClass)
    Assert.assertEquals(statusResponse.getJobStatus, JobStatus.COMPLETE)
  }

  @Test
  def canDeserializeModelObjectBytes() {
    val statusResponse = deserialize("""{"jobStatus":"COMPLETE"}""".getBytes, new StatusResponse().getClass)
    Assert.assertEquals(statusResponse.getJobStatus, JobStatus.COMPLETE)
  }

}
