package com.ksmpartners.ernie.server

import net.liftweb.http.rest.{RestHelper, XMLApiHelper}
import net.liftweb.common.{Box,Empty,Failure,Full,Logger}

import scala.xml._
import net.liftweb.http._

object DispatchRestAPI extends XMLApiHelper {

  // Required override, though not used
  def createTag(contents : NodeSeq) = <api>{contents}</api>

  /**
   * Stateless dispatch.
   */
  def dispatch: LiftRules.DispatchPF = {
    case Req(List("reports", "pending"), _, GetRequest) => () => Full(OkResponse())
    case _ => () => Full(NotFoundResponse())
 }

}
