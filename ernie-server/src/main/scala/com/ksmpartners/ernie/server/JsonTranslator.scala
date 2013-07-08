/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ksmpartners.ernie.server

import net.liftweb.common.{ Full, Box }
import net.liftweb.http.{ PlainTextResponse, LiftResponse }
import com.ksmpartners.ernie.model.ModelObject
import com.ksmpartners.ernie.util.MapperUtility._

/**
 * Trait containing methods for serializing/deserializing JSONs
 */
trait JsonTranslator {

  /**
   * Serializes an object into a JSON String
   */
  def serialize[A <: ModelObject](obj: A): String = {
    mapper.writeValueAsString(obj)
  }

  /**
   * Deserializes the given JSON String into an object of the type clazz represents
   */
  def deserialize[A <: ModelObject](json: String, clazz: Class[A]): A = {
    mapper.readValue(json, clazz)
  }

  /**
   * Deserializes the given JSON Array[Byte] into an object of the type clazz represents
   */
  def deserialize[A <: ModelObject](json: Array[Byte], clazz: Class[A]): A = {
    mapper.readValue(json, clazz)
  }

  /**
   * Serializes the given response object into a Full[PlainTextResponse] with a content-type of application/json and
   * an HTTP code of 200
   */
  def getJsonResponse[A <: ModelObject](response: A): Box[LiftResponse] = {
    getJsonResponse(response, 200)
  }

  /**
   * Serializes the given response object into a Full[PlainTextResponse] with a content-type of application/json and
   * an HTTP code of 200
   */
  def getJsonResponse[A <: ModelObject](response: A, statusCode: Int): Box[LiftResponse] = {
    Full(PlainTextResponse(serialize(response), List(("Content-Type", response.cType())), statusCode))
  }

  /**
   * Serializes the given response object into a Full[PlainTextResponse] with a content-type of application/json and
   * an HTTP code of 200
   */
  def getJsonResponse[A <: ModelObject](response: A, statusCode: Int, headers: List[(String, String)]): Box[LiftResponse] = {
    Full(PlainTextResponse(serialize(response), List(("Content-Type", response.cType())) ++ headers, statusCode))
  }

}