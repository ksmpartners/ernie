/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.server

import net.liftweb.http.rest.{ RestHelper, XMLApiHelper }
import net.liftweb.common.{ Box, Empty, Failure, Full, Logger }

import scala.xml._
import net.liftweb.http._
import org.slf4j.LoggerFactory

/**
 * Object containing the stateless dispatch definition for an ernie server
 */
object DispatchRestAPI extends XMLApiHelper {

  private val log = LoggerFactory.getLogger(this.getClass)

  // Required override, though not used
  def createTag(contents: NodeSeq) = <api>{ contents }</api>

  /**
   * Stateless dispatch.
   */
  def dispatch: LiftRules.DispatchPF = {
    case req@Req(List("jobs"), _, PostRequest) => () => ServiceRegistry.jobsResource.post(req.body)
    case req@Req(List("jobs"), _, GetRequest) => () => ServiceRegistry.jobsResource.get( /* req.hostAndPath + */ "/jobs")
    case req@Req(List("jobs", jobId, "status"), _, GetRequest) => () => ServiceRegistry.jobStatusResource.get(jobId)
    case req@Req(List("jobs", jobId, "result"), _, GetRequest) => () => ServiceRegistry.jobResultsResource.get(jobId)
    case req@Req(List("jobs", jobId, "result"), _, DeleteRequest) => () => Full(NotImplementedResponse()) // TODO: Implement
    case req@Req(List("defs"), _, GetRequest) => () => ServiceRegistry.defsResource.get("/defs")
    case req@Req(List("defs"), _, PostRequest) => () => Full(NotImplementedResponse()) // TODO: Implement
    case req@Req(List("defs", rptId), _, GetRequest) => () => Full(NotImplementedResponse()) // TODO: Implement
    case req@Req(List("defs", rptId), _, PutRequest) => () => Full(NotImplementedResponse()) // TODO: Implement
    case req@Req(List("defs", rptId), _, DeleteRequest) => () => Full(NotImplementedResponse()) // TODO: Implement
    case req => {
      log.error("Got unknown request: {}", req)
      () => Full(NotFoundResponse())
    }
  }

  def shutdown() {
    ServiceRegistry.shutdownResource.shutdown()
  }

}
