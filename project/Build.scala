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

import sbt._
import Keys._
import com.github.shivawu.sbt.maven.MavenBuild

object ErnieBuild extends MavenBuild {

  val gatlingReleasesRepo = "Gatling Releases Repo" at "http://repository.excilys.com/content/repositories/releases"
  val gatling3PartyRepo = "Gatling Third-Party Repo" at "http://repository.excilys.com/content/repositories/thirdparty"

  project("*")(scalaVersion := "2.10.1",
  	unmanagedJars in Compile <++= unmanagedBase map { ub =>
	  (ub ** "*.jar").classpath
	}
  ) //, libraryDependencies += "org.scalatest" % "scalatest_2.10" % "2.0.M6-SNAP28")
  //libraryDependencies += "org.scalatest" % "scalatest_2.10" % "2.0.M6-SNAP28"

  project("ernie-engine")(
    libraryDependencies ~= { deps =>
      deps.filter( d =>
        !d.name.contains("csv") &&
        !d.name.contains("flute")
      )
    }
 )

  project("ernie-server")(
    (com.earldouglas.xsbtwebplugin.WebPlugin.webSettings ++
    (libraryDependencies += "org.mortbay.jetty" % "jetty" % "6.1.22" % "container") ++
    (javacOptions ++= Seq(
        "ernie.props", "./ernie-server/src/main/resources/props/default.props",
        "keystore.location=", "./ernie-server/src/test/resources/keystore.jks",
        "authentication.mode", "SAML"
    ))):_*
  )

  project("ernie-gatling") {
    resolvers ++= Seq(gatlingReleasesRepo, gatling3PartyRepo)
  }

}
