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
import com.ksmpartners.ernie.model.ModelObject

/**
 * Trait containing methods for serializing/deserializing JSONs
 */
trait JsonTranslator {
  private val mapper = new ObjectMapper

  /**
   * Serializes an object into a JSON String
   */
  def serialize[A <% ModelObject](obj: A): String = {
    mapper.writeValueAsString(obj)
  }

  /**
   * Deserializes the given JSON String into an object of the type clazz represents
   */
  def deserialize[A <% ModelObject](json: String, clazz: Class[A]): A = {
    mapper.readValue(json, clazz) match {
      case a: A => a
      case _ => throw new ClassCastException
    }
  }

  /**
   * Deserializes the given JSON Array[Byte] into an object of the type clazz represents
   */
  def deserialize[A <% ModelObject](json: Array[Byte], clazz: Class[A]): A = {
    mapper.readValue(json, clazz) match {
      case a: A => a
      case _ => throw new ClassCastException
    }
  }

  /**
   * Serializes the given response object into a Full[PlainTextResponse] with a content-type of application/json and
   * an HTTP code of 200
   */
  def getJsonResponse[A <% ModelObject](response: A): Box[LiftResponse] = {
    getJsonResponse(response, 200)
  }

  /**
   * Serializes the given response object into a Full[PlainTextResponse] with a content-type of application/json and
   * an HTTP code of 200
   */
  def getJsonResponse[A <% ModelObject](response: A, statusCode: Int): Box[LiftResponse] = {
    Full(PlainTextResponse(serialize(response), List(("Content-Type", response.cType())), statusCode))
  }
}