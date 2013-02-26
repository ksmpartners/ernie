package com.ksmpartners.ernie.server

import net.liftweb.http.rest.{RestHelper, XMLApiHelper}
import net.liftweb.common.{Box,Empty,Failure,Full,Logger}

import scala.xml._
import net.liftweb.http._
import org.slf4j.LoggerFactory

object DispatchRestAPI extends XMLApiHelper {

  private val LOG = LoggerFactory.getLogger(this.getClass)

  // Required override, though not used
  def createTag(contents : NodeSeq) = <api>{contents}</api>

  /**
   * Stateless dispatch.
   */
  def dispatch: LiftRules.DispatchPF = {
    case Req(List("jobs"), _, PutRequest) => () => Full(OkResponse())
    case Req(List("jobs", jobId, "status"), _, GetRequest) => () => Full(OkResponse())
    case Req(List("jobs", jobId, "results", "pdf"), _, GetRequest) => () => Full(OkResponse())
    case _ =>
      LOG.error("Got unknown request")
      () => Full(NotFoundResponse())
  }

}
