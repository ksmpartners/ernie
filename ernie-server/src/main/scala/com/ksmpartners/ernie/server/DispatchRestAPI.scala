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
import com.ksmpartners.ernie.model.ModelObject

/**
 * Object containing the stateless dispatch definition for an ernie server
 */
object DispatchRestAPI extends RestHelper with JsonTranslator {

  private val log = LoggerFactory.getLogger("com.ksmpartners.ernie.server.DispatchRestAPI")

  // Required override, though not used
  def createTag(contents: NodeSeq) = <api>{ contents }</api>

  /**
   * Stateless dispatch.
   */
  def dispatch: LiftRules.DispatchPF = {
    case req@Req(List("jobs"), _, PostRequest) => () => ServiceRegistry.jobsResource.post(req.body)
    case req@Req(List("jobs"), _, GetRequest) => () => ServiceRegistry.jobsResource.get("/jobs")
    case req@Req(List("jobs", jobId, "status"), _, GetRequest) => () => ServiceRegistry.jobStatusResource.get(jobId)
    case req@Req(List("jobs", jobId, "result"), _, GetRequest) => () => ServiceRegistry.jobResultsResource.get(jobId)
    case req@Req(List("jobs", jobId, "result"), _, DeleteRequest) => () => Full(NotImplementedResponse()) // TODO: Implement
    case req@Req(List("defs"), _, GetRequest) => (authFilter(req, READ_ROLE)_ compose ctypeFilter(req)_)({ ServiceRegistry.defsResource.get("/defs") })
    case req@Req(List("defs"), _, PostRequest) => () => Full(NotImplementedResponse()) // TODO: Implement
    case req@Req(List("defs", defId), _, GetRequest) => (authFilter(req, READ_ROLE)_ compose ctypeFilter(req)_)({ ServiceRegistry.defDetailResource.get(defId) })
    case req@Req(List("defs", defId), _, PutRequest) => () => Full(NotImplementedResponse()) // TODO: Implement
    case req@Req(List("defs", defId), _, DeleteRequest) => () => Full(NotImplementedResponse()) // TODO: Implement
    case req => {
      log.error("Got unknown request: {}", req)
      () => Full(NotFoundResponse())
    }
  }

  /**
   * Method that verifies that the requesting user is in the given role
   * @param req - The request being handled
   * @param role - The role to verify
   * @param f - The function to be called if the user is in the role
   * @return the function f, or a ForbiddenResponse if the user is not in the specified role
   */
  private def authFilter(req: Req, role: String)(f: () => Box[LiftResponse]): () => Box[LiftResponse] = {
    if (isUserInRole(req, role)) f else () => Full(ForbiddenResponse("User is not authorized to perform that action"))
  }

  /**
   * Method that verifies that the requesting user accepts the correct ctype
   * @param req - The request being handled
   * @param f - The function to be called if the user accepts the correct ctype
   * @return the function f, or a NotAcceptableResponse if the user does not accept the correct ctype
   */
  private def ctypeFilter(req: Req)(f: () => Box[LiftResponse]): () => Box[LiftResponse] = {
    if (acceptsErnieJson(req)) f else () => Full(NotAcceptableResponse("Accept header does not contain " + ModelObject.TYPE_PREFIX + "/" + ModelObject.TYPE_POSTFIX))
  }

  def shutdown() {
    ServiceRegistry.shutdownResource.shutdown()
  }

  def init() {
    ServiceRegistry.init()
  }

}
