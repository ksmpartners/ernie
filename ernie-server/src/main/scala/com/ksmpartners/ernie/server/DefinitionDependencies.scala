/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

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
