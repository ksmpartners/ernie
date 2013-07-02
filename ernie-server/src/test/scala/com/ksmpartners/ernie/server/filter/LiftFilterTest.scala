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
import java.io.{ ByteArrayOutputStream, FileInputStream, File }
import com.ksmpartners.ernie.util.Utility._
import org.apache.cxf.rs.security.saml.DeflateEncoderDecoder
import com.ksmpartners.ernie.util.Base64Util
import com.ksmpartners.ernie.server.filter.SAMLConstants._
import org.testng.Assert
import net.liftweb.http.{ LiftServlet, ForbiddenResponse, LiftFilter }
import com.ksmpartners.ernie.server.{ MockNoAuthReq, TestBoot, DispatchRestAPI }
import net.liftweb.http.auth.{ userRoles, AuthRole }
import com.ksmpartners.ernie.model.ModelObject
import net.liftweb.mockweb.MockWeb
import net.liftweb.http.provider.HTTPResponse

//import com.ksmpartners.common.annotations.tracematrix.{ TestSpec, TestSpecs }
import org.slf4j.{ LoggerFactory, Logger }
import com.ksmpartners.ernie.server.filter.SAMLFilter.SAMLHttpServletRequestWrapper
import com.ksmpartners.ernie.util.TestLogger

class LiftFilterTest extends TestLogger {

  private val readMode = "read"
  private val writeMode = "write"
  private val readWriteMode = "read-write"
  private val log: Logger = LoggerFactory.getLogger("com.ksmpartners.ernie.server.filter.SAMLFilterTest")

  @BeforeClass
  def setup() {
    val ks = Thread.currentThread.getContextClassLoader.getResource("keystore.jks")
    System.setProperty(keystoreLocProp, ks.getPath)

    System.setProperty(authModeProp, "BASIC")

    DispatchRestAPI.basicAuthentication = {
      case (u: String, p: String, req) => {
        if (u == "mockReadUser") userRoles(AuthRole(SAMLConstants.readRole) :: Nil)
        if (u == "mockWriteUser") userRoles(AuthRole(SAMLConstants.writeRole) :: Nil)
        if (u == "mockRunUser") userRoles(AuthRole(SAMLConstants.runRole) :: Nil)
        true
      }
    }

    //(new TestBoot).setUpAndBoot
  }

  @Test
  def cantGetJobsWithoutReadAuthBasic() {
    val filter = new LiftServlet
    val req = new MockNoAuthReq("/jobs")
    val resp: HTTPResponse = null
    val chain = new Chain
    MockWeb.testReq(req) {
      r =>
        Assert.assertTrue(filter.service(r, resp))
        Assert.assertEquals(resp.getStatus, 403)
    }
  }

  @Test
  def goodAuthReturns200() {

    // Assert.assertEquals(resp.getStatusCode, 200)
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