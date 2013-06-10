/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.server.filter

import org.testng.annotations.{ BeforeClass, Test }
import com.ksmpartners.ernie.server.PropertyNames._
import net.liftweb.mocks.{ MockHttpServletResponse, MockHttpServletRequest }
import javax.servlet.{ ServletResponse, ServletRequest, FilterChain }
import javax.servlet.http.HttpServletResponse
import java.io.{ ByteArrayOutputStream, FileOutputStream, FileInputStream, File }
import com.ksmpartners.ernie.util.Utility._
import org.apache.cxf.rs.security.saml.DeflateEncoderDecoder
import com.ksmpartners.commons.util.Base64Util
import com.ksmpartners.ernie.server.filter.SAMLConstants._
import org.testng.Assert
import com.ksmpartners.common.annotations.tracematrix.{ TestSpec, TestSpecs }
import org.slf4j.{ LoggerFactory, Logger }
import com.ksmpartners.ernie.server.filter.SAMLFilter.SAMLHttpServletRequestWrapper

class SAMLFilterTest {

  private val readMode = "read"
  private val writeMode = "write"
  private val readWriteMode = "read-write"
  private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.server.filter.SAMLFilterTest")

  @BeforeClass
  def setup() {
    val ks = Thread.currentThread.getContextClassLoader.getResource("keystore.jks")
    System.setProperty(keystoreLocProp, ks.getPath)
  }

  @Test
  def goodAuthReturns200() {
    val filter = new SAMLFilter
    val req = new MockHttpServletRequest
    val resp = new MockResp
    val chain = new Chain

    req.headers += (authHeaderProp -> List(getSamlHeaderVal(readWriteMode)))

    filter.doFilter(req, resp, chain)

    Assert.assertEquals(resp.getStatusCode, 200)
  }

  @TestSpecs(Array(new TestSpec(key = "ERNIE-40"), new TestSpec(key = "ERNIE-86"), new TestSpec(key = "ERNIE-73"),
    new TestSpec(key = "ERNIE-79"), new TestSpec(key = "ERNIE-64"), new TestSpec(key = "ERNIE-59"),
    new TestSpec(key = "ERNIE-52"), new TestSpec(key = "ERNIE-46"), new TestSpec(key = "ERNIE-99"), new TestSpec(key = "ERNIE-93"), new TestSpec(key = "ERNIE-138"), new TestSpec(key = "ERNIE-144"), new TestSpec(key = "ERNIE-151")))
  @Test
  def noAuthReturns401() {
    val filter = new SAMLFilter
    val req = new MockHttpServletRequest
    val resp = new MockResp
    val chain = new Chain

    filter.doFilter(req, resp, chain)

    Assert.assertEquals(resp.getStatusCode, 401)
  }

  @Test
  def canGetUserName() {
    val filter = new SAMLFilter
    val req = new MockHttpServletRequest
    val resp = new MockResp
    val chain = new Chain

    req.headers += (authHeaderProp -> List(getSamlHeaderVal(readWriteMode)))

    filter.doFilter(req, resp, chain)

    Assert.assertEquals(chain.userName, "readWriteUser")
  }

  def getSamlHeaderVal(mode: String): String = "SAML " + (new String(encodeToken(mode)))

  def encodeToken(mode: String): Array[Byte] = {
    val samlUrl = Thread.currentThread.getContextClassLoader.getResource("saml/" + mode + ".xml")
    val samlFile = new File(samlUrl.getFile)
    var bos: Array[Byte] = null

    var deflatedToken: Array[Byte] = null
    try_(new FileInputStream(samlFile)) { file =>
      val fileBytes: Array[Byte] = new Array[Byte](file.available())
      file.read(fileBytes)
      deflatedToken = new DeflateEncoderDecoder().deflateToken(fileBytes)
    }

    val encodedToken = Base64Util.encode(deflatedToken)

    try_(new ByteArrayOutputStream()) { os =>
      os.write(encodedToken)
      bos = os.toByteArray
    }

    bos
  }

  class MockResp extends MockHttpServletResponse(null, null) {
    def getStatusCode: Int = statusCode
  }

  class Chain extends FilterChain {
    var userName: String = ""
    def doFilter(request: ServletRequest, response: ServletResponse) {
      if (request.isInstanceOf[SAMLHttpServletRequestWrapper])
        userName = request.asInstanceOf[SAMLHttpServletRequestWrapper].getRemoteUser
    }
  }

}