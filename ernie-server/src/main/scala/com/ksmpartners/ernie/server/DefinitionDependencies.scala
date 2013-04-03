/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.server

import com.ksmpartners.ernie.{ model, engine }
import java.util

/**
 * Dependencies for interacting with report definitions
 */
trait DefinitionDependencies extends ActorTrait {

  class DefsResource extends JsonTranslator {
    def get(uriPrefix: String) = {
      val defMap: util.Map[String, String] = new util.HashMap
      reportManager.getAllDefinitionIds.foreach({ defId =>
        defMap.put(defId, uriPrefix + "/" + defId)
      })
      getJsonResponse(new model.ReportDefinitionMapResponse(defMap))
    }
  }

}
