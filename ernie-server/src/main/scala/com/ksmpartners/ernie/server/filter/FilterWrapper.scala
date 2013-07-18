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

package com.ksmpartners.ernie.server.filter

import javax.servlet._
import javax.servlet.http.{ HttpServletResponse, HttpServletRequest, HttpServletRequestWrapper }
import com.ksmpartners.ernie.server.PropertyNames._
import java.util.Properties
import java.io.{ FileInputStream, File }
import com.ksmpartners.ernie.util.Utility._
import com.ksmpartners.ernie.server.PropertyNames
import org.slf4j.LoggerFactory

/**
 * Filter that delegates filtering based on the value of authentication.mode variable.
 */
class FilterWrapper extends Filter {

  private var wrappedFilter: Filter = null

  /**
   * Initialize the filter and instantiate the authentication filter.
   */
  def init(config: FilterConfig) {
    val authMode = System.getProperty(authModeProp)

    authMode match {
      case "SAML" => wrappedFilter = new SAMLFilter
      case _ => wrappedFilter = new PassThroughFilter
    }

    wrappedFilter.init(config)
  }

  private val properties: Properties = {

    val propsPath = System.getProperty(propertiesFileNameProp)

    if (null == propsPath) {
      throw new RuntimeException("System property " + propertiesFileNameProp + " is undefined")
    }

    val propsFile = new File(propsPath)
    if (!propsFile.exists) {
      throw new RuntimeException("Properties file " + propsPath + " does not exist.")
    }

    if (!propsFile.canRead) {
      throw new RuntimeException("Properties file " + propsPath + " is not readable; check file privileges.")
    }
    val props = new Properties()
    try_(new FileInputStream(propsFile)) { propsFileStream =>
      props.load(propsFileStream)
    }
    props
  }

  def doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain) {
    res.asInstanceOf[HttpServletResponse].addHeader("Access-Control-Allow-Origin", "*")
    res.asInstanceOf[HttpServletResponse].addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
    res.asInstanceOf[HttpServletResponse].addHeader("Access-Control-Request-Headers", "Authorization,WWW-Authenticate,Keep-Alive,User-Agent,X-Requested-With,Cache-Control,Content-Type")
    res.asInstanceOf[HttpServletResponse].addHeader("Access-Control-Allow-Headers", "Authorization,WWW-Authenticate,Keep-Alive,User-Agent,X-Requested-With,Cache-Control,Content-Type")
    val uri = req.asInstanceOf[HttpServletRequest].getRequestURI
    if (properties.stringPropertyNames().contains(PropertyNames.swaggerDocsProp) && (properties.get(PropertyNames.swaggerDocsProp).toString == "true"))
      if (uri.contains("static") || uri.contains("resources.json") || uri.contains("jobs.json") || uri.contains("defs.json")) {
        (new PassThroughFilter).doFilter(new SAMLFilter.SAMLHttpServletRequestWrapper(req.asInstanceOf[HttpServletRequest], "staticUser", SAMLConstants.allRoles.toSet), res, chain)
      } else wrappedFilter.doFilter(req, res, chain)
  }

  def destroy() { wrappedFilter.destroy() }

  /**
   * Filter that wraps each request with a wrapper that allows full authorization.
   */
  private class PassThroughFilter extends Filter {

    def init(config: FilterConfig) {}

    def doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain) {
      req match { case request: HttpServletRequest => chain.doFilter(new PassThroughAuthRequest(request), res) }
    }

    def destroy() {}

    private class PassThroughAuthRequest(req: HttpServletRequest) extends HttpServletRequestWrapper(req) {
      override def isUserInRole(role: String): Boolean = true
      override def getAuthType: String = "None"
    }

  }

}
