/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.server

import com.fasterxml.jackson.databind.ObjectMapper
import net.liftweb.common.{ Full, Box }
import net.liftweb.http.{ PlainTextResponse, LiftResponse }

/**
 * Trait containing methods for serializing/deserializing JSONs
 */
trait JsonTranslator {
  private val mapper = new ObjectMapper

  /**
   * Serializes an object into a JSON String
   */
  def serialize[A](obj: A): String = {
    mapper.writeValueAsString(obj)
  }

  /**
   * Deserializes the given JSON String into an object of the type clazz represents
   */
  def deserialize[A](json: String, clazz: Class[A]): A = {
    mapper.readValue(json, clazz) match {
      case a: A => a
      case _ => throw new ClassCastException
    }
  }

  /**
   * Deserializes the given JSON Array[Byte] into an object of the type clazz represents
   */
  def deserialize[A](json: Array[Byte], clazz: Class[A]): A = {
    mapper.readValue(json, clazz) match {
      case a: A => a
      case _ => throw new ClassCastException
    }
  }

  /**
   * Serializes the given response object into a Full[PlainTextResponse] with a content-type of application/json and
   * an HTTP code of 200
   */
  def getJsonResponse[A](response: A): Box[LiftResponse] = {
    Full(PlainTextResponse(serialize(response), List(("Content-Type", "application/json")), 200))
  }
}