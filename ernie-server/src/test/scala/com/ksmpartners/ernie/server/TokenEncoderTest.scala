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

import org.testng.annotations.Test
import java.io.{ FileOutputStream, FileInputStream, File }
import com.ksmpartners.ernie.util.Base64Util
import org.apache.cxf.rs.security.saml.DeflateEncoderDecoder
import com.ksmpartners.ernie.util.Utility._
import com.ksmpartners.ernie.util.TestLogger

class TokenEncoderTest extends TestLogger {

  //@Test
  def encodeToken() {
    val samlUrl = Thread.currentThread.getContextClassLoader.getResource("saml/MySAML.xml")
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
