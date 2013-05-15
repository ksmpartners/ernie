/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.server

import org.testng.annotations.Test
import java.io.{ FileOutputStream, FileInputStream, File }
import com.ksmpartners.commons.util.Base64Util
import org.apache.cxf.rs.security.saml.DeflateEncoderDecoder
import com.ksmpartners.ernie.util.FileUtils._

class TokenEncoderTest {

  //@Test
  def encodeToken() {
    val samlUrl = Thread.currentThread.getContextClassLoader.getResource("MySAML.xml")
    val samlFile = new File(samlUrl.getFile)
    val outputFile = new File("./encodedTok")

    var deflatedToken: Array[Byte] = null
    try_(new FileInputStream(samlFile)) { file =>
      {
        var fileBytes: Array[Byte] = new Array[Byte](file.available())
        file.read(fileBytes)
        deflatedToken = new DeflateEncoderDecoder().deflateToken(fileBytes)
      }
    }

    val encodedToken = Base64Util.encode(deflatedToken)

    try_(new FileOutputStream(outputFile)) { os =>
      os.write(encodedToken)
    }

  }

}
