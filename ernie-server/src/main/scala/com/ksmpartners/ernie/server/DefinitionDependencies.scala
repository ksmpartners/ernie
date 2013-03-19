package com.ksmpartners.ernie.server

import com.ksmpartners.ernie.{ model, engine }

trait DefinitionDependencies extends ActorTrait {

  class DefsResource extends JsonTranslator {
    def get = {
      val response = (coordinator !? engine.ReportDefinitionMapRequest()).asInstanceOf[engine.ReportDefinitionMapResponse]
      getJsonResponse(new model.ReportDefinitionMap(response.rptDefMap))
    }
  }

}
