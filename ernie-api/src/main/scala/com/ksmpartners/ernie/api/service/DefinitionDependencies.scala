/*
	Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/

package com.ksmpartners.ernie.api.service

import com.ksmpartners.ernie.engine
import com.ksmpartners.ernie.model.{ ParameterEntity, DeleteStatus, DefinitionEntity }
import java.io.{ InputStream, ByteArrayInputStream }
import com.ksmpartners.ernie.engine.report.BirtReportGenerator
import com.ksmpartners.ernie.api._
import com.ksmpartners.ernie.engine.DeleteDefinitionResponse
import akka.pattern.ask
import scala.concurrent.Await

/**
 * Dependencies for interacting with report definitions.
 */
trait DefinitionDependencies extends RequiresReportManager with RequiresCoordinator {

  /**
   * Provides definitions operations used by API.
   */
  class DefsResource {

    /**
     *  Create or update a definition.
     *  @param defId existing definition to update
     * @param rptDesign BIRT report design XML as byte array input stream
     * @param definitionEntity definition metadata
     * @throws NotFoundException if defId is not found
     * @throws MissingArgumentException if neither report design nor DefinitionEntity is provided
     * @throws InvalidDefinitionException if rptDesign is null or contains malformed XML
     * @return updated definition metadata.
     */
    def putDefinition(defId: Option[String], rptDesign: Option[InputStream], definitionEntity: Option[DefinitionEntity]): DefinitionEntity = {
      defId.map(f => if (!reportManager.getDefinition(f).isDefined) throw new NotFoundException(f + " not found"))
      if (!(definitionEntity.isDefined || rptDesign.isDefined)) throw new MissingArgumentException("Must specify at least a definition entity or design")
      var defEnt = definitionEntity.getOrElse(defId.flatMap(d => reportManager.getDefinition(d).map(f => f.getEntity)) getOrElse (new DefinitionEntity))
      if (rptDesign.isDefined) {
        if (rptDesign.get == null) throw new InvalidDefinitionException("Definition null")
        if (!BirtReportGenerator.isValidDefinition(rptDesign.get))
          throw new InvalidDefinitionException("Definition invalid")
        else try {
          rptDesign.get.reset
          val designXml = scala.xml.XML.load(rptDesign.get)

          var paramList: java.util.List[ParameterEntity] = if (defEnt.getParams == null) new java.util.ArrayList[ParameterEntity]() else defEnt.getParams

          (designXml \\ "parameters").foreach(f => f.child.foreach(g => {
            var param = new ParameterEntity()
            param.setParamName((g \ "@name").text)

            g.child.foreach(prop => (prop \ "@name").text match {
              case "allowBlank" => param.setAllowNull(prop.text == "true")
              case "dataType" => param.setDataType(prop.text)
              case "defaultValue" => param.setDefaultValue(prop.text.trim)
              case _ =>
            })

            if ((param.getAllowNull == null) & (param.getDefaultValue != "")) param.setAllowNull(true)

            if ((param.getParamName != "") && (param.getDataType != "") && (param.getAllowNull != null)) paramList.add(param)
          }))

          defEnt.setParams(paramList)
        } catch {
          case e: Exception => throw InvalidDefinitionException("Malformed report design while extracting parameters: " + e.getMessage)
        }
      }
      if (!defId.isDefined) {
        val (defEntRes: DefinitionEntity, stream: java.io.OutputStream) = reportManager.putDefinition(defEnt)
        defEnt = defEntRes

        rptDesign.map(r => { r.reset; org.apache.commons.io.CopyUtils.copy(r, stream) })
        stream.close
        defEnt
      } else {
        defEnt.setDefId(defId.get)
        val result = reportManager.updateDefinition(defId.get, defEnt)
        rptDesign.map(r => { r.reset; org.apache.commons.io.CopyUtils.copy(r, result) })
        result.close
        defEnt
      }
    }

    /**
     * Return all existing definition IDs.
     */
    def getList(): List[String] = reportManager.getAllDefinitionIds

    /**
     * Return DefinitionEntities for all definitions.
     */
    def getCatalog() = {
      reportManager.getAllDefinitionIds.foldLeft[List[DefinitionEntity]](Nil)((list, dId) =>
        list ::: (reportManager.getDefinition(dId).map(f => f.getEntity).toList))
    }

    /**
     * Get definition metadata.
     * @param defId definition to interrogate
     * @return DefinitionEntity if defId is found; otherwise, [[scala.None]].
     */
    def getDefinitionEntity(defId: String): Option[DefinitionEntity] = reportManager.getDefinition(defId).map(de => de.getEntity)

    /**
     * Get definition design as input stream.
     * @param defId definition to interrogate
     * @return InputStream if defId is found; otherwise, [[scala.None]].
     */
    def getDefinitionDesign(defId: String): Option[InputStream] = reportManager.getDefinitionContent(defId)

    /**
     * Delete a definition. Completely remove the report design and DefinitionEntity from the report manager and filesystem (if applicable).
     * @param defId definition to delete
     * @throws MissingArgumentException if defId is null
     * @return a DeleteStatus indicating the result of deletion.
     */
    def deleteDefinition(defId: String): DeleteStatus = {
      if (defId == null) throw new MissingArgumentException("Definition ID null")
      Await.result((coordinator ? (engine.DeleteDefinitionRequest(defId))).mapTo[DeleteDefinitionResponse], timeoutDuration).deleteStatus
    }
  }
}
