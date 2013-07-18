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

package com.ksmpartners.ernie.server.service

import com.ksmpartners.ernie.model
import java.util
import net.liftweb.http._
import com.ksmpartners.ernie.server.{ DispatchRestAPI, JsonTranslator }
import com.ksmpartners.ernie.model.{ ModelObject, DeleteStatus, DefinitionEntity }
import java.io.ByteArrayInputStream
import net.liftweb.http.BadResponse
import net.liftweb.common.{ Box, Full }
import org.slf4j.{ LoggerFactory, Logger }
import com.ksmpartners.ernie.server.RestGenerator._
import com.ksmpartners.ernie.server.filter.AuthUtil
import com.ksmpartners.ernie.api.MissingArgumentException
import com.ksmpartners.ernie.util.Utility

/**
 * Dependencies for interacting with report definitions
 */
trait DefinitionDependencies extends RequiresAPI {

  /**
   * Resource for handling HTTP requests at /defs.
   */
  class DefsResource extends JsonTranslator {
    val unexpectedError = ErnieError(InternalServerErrorResponse(), None)
    private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.server.DefsResource")

    val getDefsAction: Action = Action("getDefinitions", get(_: Package), "Retrieve a mapping of definition IDs to URIs", "", "reportDefMap",
      DispatchRestAPI.timeoutErnieError("Defs list"))

    /**
     * GET a list of definition IDs.
     * @param p a set of parameters from the request
     */
    def get(p: Package): Box[LiftResponse] = get

    /**
     * GET a list of definition IDs.
     */
    def get =
      apiCall[List[String]](getDefsAction, _ => ernie.getDefinitionList(), (list) => {
        val defMap: util.Map[String, String] = new util.HashMap
        list.foreach({ defId =>
          defMap.put(defId, "/defs/" + defId)
        })
        getJsonResponse(new model.ReportDefinitionMapResponse(defMap))
      })

    val noDefEntInBody = ErnieError(ResponseWithReason(BadResponse(), "No DefinitionEntity in request body"), None)
    val malformedDefEnt = ErnieError(ResponseWithReason(BadResponse(), "Malformed DefinitionEntity"), None)
    val postDefAction = Action("postDefinition", post(_), "Post a DefinitionEntity", "", "DefinitionEntity",
      noDefEntInBody,
      malformedDefEnt,
      unexpectedError,
      DispatchRestAPI.timeoutErnieError("Definition creation"))

    /**
     * POST a serialized DefinitionEntity to create a definition.
     * @param p a set of parameters from the request
     */
    def post(p: Package): Box[LiftResponse] = post(p.req)

    /**
     * POST a serialized DefinitionEntity to create a definition.
     * @param req a request with a serialized DefinitionEntity in the body
     */
    def post(req: net.liftweb.http.Req): Box[LiftResponse] = {
      if (req.body.isEmpty) {
        noDefEntInBody.send
      } else try {
        var defEnt: DefinitionEntity = deserialize(req.body.open_!, classOf[DefinitionEntity])
        apiCall[DefinitionEntity](postDefAction, _ => ernie.createDefinition(None, defEnt.getDefDescription, AuthUtil.getUserName(req)), defEnt =>
          getJsonResponse(defEnt, 201, List(("Location", req.hostAndPath + "/defs/" + defEnt.getDefId))))
      } catch {
        case e: Exception => {
          log.debug("Caught exception while posting definition: {}", e.getMessage + "\n" + e.getStackTraceString)
          malformedDefEnt.send
        }
      }
    }
  }

  /**
   * Resource for handling HTTP requests at /defs/<DEF_ID>.
   */
  class DefDetailResource extends JsonTranslator {

    val defNotFound = ErnieError(NotFoundResponse(), Some(com.ksmpartners.ernie.api.NotFoundException("Definition ID not found")))
    val getDefDetailAction: Action = Action("getDefinitionDetail", get(_), "Retrieve the DefinitionEntity for a specific Definition ID", "", "DefinitionEntity",
      defNotFound,
      DispatchRestAPI.timeoutErnieError("Get definition"))

    /**
     * GET a DefinitionEntity.
     * @param p a set of parameters from the request
     */
    def get(p: Package): Box[LiftResponse] = if (p.params.length != 1) defNotFound.send else get(p.params(0).data.toString)

    /**
     * GET a DefinitionEntity.
     * @param defId of the DefinitionEntity to receive
     */
    def get(defId: String) = {
      apiCall[DefinitionEntity](getDefDetailAction, _ => ernie.getDefinitionEntity(defId), defEnt => getJsonResponse(defEnt))
    }

    val defInUse = ErnieError(ResponseWithReason(ConflictResponse(), "Definition in use"), None)
    val deleteDefFailed = ErnieError(ResponseWithReason(BadResponse(), "Definition deletion failed"), None)
    val deleteDefAction: Action = Action("deleteDefinition", del(_: Package), "Deletes a specific definition", "", "DefinitionDeleteResponse",
      DispatchRestAPI.timeoutErnieError("Definition delete"),
      defNotFound,
      defInUse,
      deleteDefFailed)

    /**
     * DELETE a DefinitionEntity and associated .rptdesign.
     * @param p a set of parameters from the request
     */
    def del(p: Package): Box[LiftResponse] = if (p.params.length != 1) Full(ResponseWithReason(BadResponse(), "Invalid job id")) else del(p.params(0).data.toString)

    /**
     * DELETE a DefinitionEntity and associated .rptdesign.
     * @param defId of the definition to delete
     */
    def del(defId: String) =
      apiCall[DeleteStatus](deleteDefAction, _ => ernie.deleteDefinition(defId), delete =>
        if (delete == DeleteStatus.SUCCESS) getJsonResponse(new model.DeleteDefinitionResponse(delete))
        else if (delete == DeleteStatus.NOT_FOUND) defNotFound.send
        else if (delete == DeleteStatus.FAILED_IN_USE) defInUse.send
        else deleteDefFailed.send)

    val unacceptable = ErnieError(ResponseWithReason(BadResponse(), "Unacceptable Content-Type"), None)
    val noRptDesign = ErnieError(ResponseWithReason(BadResponse(), "No report design in request body"), Some(MissingArgumentException("No report design in request body")))
    val unexpectedError = ErnieError(InternalServerErrorResponse(), None)
    val malformedDesign = ErnieError(BadResponse(), Some(com.ksmpartners.ernie.api.InvalidDefinitionException("Unable to validate report design")))
    val putDefAction: Action = Action("putDefinition", put(_), "Put definition rptdesign", "", "DefinitionEntity",
      unacceptable,
      noRptDesign,
      defNotFound,
      malformedDesign,
      unexpectedError)

    /**
     * PUT a definition .rptdesign to associate with an existing DefinitionEntity.
     * If the definition already has an associated .rptdesign, it will be replaced
     * @param p a set of parameters from the request
     * \
     */
    def put(p: Package): Box[LiftResponse] = if (p.params.length != 1) Full(ResponseWithReason(BadResponse(), "Invalid definition id")) else put(p.params(0).data.toString, p.req)

    /**
     * PUT a definition .rptdesign to associate with an existing DefinitionEntity.
     * If the definition already has an associated .rptdesign, it will be replaced
     * @param defId an existing definition
     * @param req a request with a .rptdesign in the body
     */
    def put(defId: String, req: net.liftweb.http.Req) = {
      if (!Utility.checkContentType(req.headers, ModelObject.TYPE_RPTDESIGN_FULL)) unacceptable.send
      else if (req.body.isEmpty) noRptDesign.send
      else {
        apiCall[DefinitionEntity](putDefAction, _ => Utility.fTry_(new ByteArrayInputStream(req.body.open_!))(bAIS => ernie.updateDefinition(defId, None, Some(bAIS))), defEnt =>
          getJsonResponse(defEnt, 201))
      }
    }
  }
}

case class DefinitionCreatedResponse() extends LiftResponse with HeaderDefaults {
  def toResponse = InMemoryResponse(Array(), headers, cookies, 201)
}
case class ConflictResponse() extends LiftResponse with HeaderDefaults {
  def toResponse = InMemoryResponse(Array(), headers, cookies, 409)
}
