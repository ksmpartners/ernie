/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.server

import net.liftweb.common.{ Box, Full }
import com.ksmpartners.ernie.server.filter.AuthUtil._
import com.ksmpartners.ernie.server.filter.SAMLConstants._

import scala.xml._
import net.liftweb.http._
import org.slf4j.LoggerFactory
import rest.RestHelper

/**
 * Object containing the stateless dispatch definition for an ernie server
 */
object DispatchRestAPI extends RestHelper {

  private val log = LoggerFactory.getLogger("com.ksmpartners.ernie.server.DispatchRestAPI")

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
    case req@Req(List("defs"), _, GetRequest) => () => authFilter(req, READ_ROLE)(() => ServiceRegistry.defsResource.get("/defs"))
    case req@Req(List("defs"), _, PostRequest) => () => Full(NotImplementedResponse()) // TODO: Implement
    case req@Req(List("defs", defId), _, GetRequest) => () => authFilter(req, READ_ROLE)(() => ServiceRegistry.defDetailResource.get(defId))
    case req@Req(List("defs", defId), _, PutRequest) => () => Full(NotImplementedResponse()) // TODO: Implement
    case req@Req(List("defs", defId), _, DeleteRequest) => () => Full(NotImplementedResponse()) // TODO: Implement
    case req => {
      log.error("Got unknown request: {}", req)
      () => Full(NotFoundResponse())
    }
  }

  private def authFilter(req: Req, role: String)(f: () => Box[LiftResponse]): Box[LiftResponse] = {
    if (isUserInRole(req, role)) f.apply() else Full(ForbiddenResponse("User is not authorized to perform that action"))
  }

  def shutdown() {
    ServiceRegistry.shutdownResource.shutdown()
  }

  def init() {
    ServiceRegistry.init()
  }

}
