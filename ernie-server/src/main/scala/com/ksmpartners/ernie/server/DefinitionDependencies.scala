package com.ksmpartners.ernie.server

import com.ksmpartners.ernie.{ model, engine }

trait DefinitionDependencies extends ActorTrait {

  class DefsResource extends JsonTranslator {
    def get(uriPrefix: String) = {
      val response = (coordinator !? engine.ReportDefinitionMapRequest(uriPrefix)).asInstanceOf[engine.ReportDefinitionMapResponse]
      getJsonResponse(new model.ReportDefinitionMapResponse(response.rptDefMap))
    }
  }

}
