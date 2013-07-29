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
import sbtassembly.Plugin._
import AssemblyKeys._

object ErnieBuild extends MavenBuild {

  val gatlingReleasesRepo = "Gatling Releases Repo" at "http://repository.excilys.com/content/repositories/releases"
  val gatling3PartyRepo = "Gatling Third-Party Repo" at "http://repository.excilys.com/content/repositories/thirdparty"

   implicit def dependencyFilterer(deps: Seq[ModuleID]) = new Object {
	  def excluding(group: String, artifactId: String) =
		deps.map(_.exclude(group, artifactId))
	}
  
  lazy val buildSettings = Defaults.defaultSettings ++ Seq(
    version := "1.0-SNAPSHOT",
    organization := "com.ksmpartners",
    scalaVersion := "2.10.1"
  )

  private val LicenseFile = """(license|licence|notice|copying)([.]\w+)?$""".r
  private def isLicenseFile(fileName: String): Boolean =
    fileName.toLowerCase match {
      case LicenseFile(_, ext) if ext != ".class" => true // DISLIKE
      case _ => false
    }

  private val ReadMe = """(readme)([.]\w+)?$""".r
  private def isReadme(fileName: String): Boolean =
    fileName.toLowerCase match {
      case ReadMe(_, ext) if ext != ".class" => true
      case _ => false
    }

  val ernieMerge:String => MergeStrategy = {
    case "reference.conf" | "rootdoc.txt" =>
      MergeStrategy.concat
    case PathList(ps @ _*) if isReadme(ps.last) || isLicenseFile(ps.last) =>
      MergeStrategy.rename
    case PathList("META-INF", xs @ _*) =>
      (xs map {_.toLowerCase}) match {
        case ("manifest.mf" :: Nil) | ("index.list" :: Nil) | ("dependencies" :: Nil) =>
          MergeStrategy.discard
        case ps @ (x :: xs) if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") =>
          MergeStrategy.discard
        case "plexus" :: xs =>
          MergeStrategy.discard
        case "services" :: xs =>
          MergeStrategy.filterDistinctLines
        case ("spring.schemas" :: Nil) | ("spring.handlers" :: Nil) =>
          MergeStrategy.filterDistinctLines
        case _ => MergeStrategy.first
      }
    case _ => MergeStrategy.first
  }

  project("*")(
    (
      (scalaVersion := "2.10.1") ++
      buildSettings ++ assemblySettings ++
      (unmanagedJars in Compile <++= unmanagedBase map { ub =>
	     (ub ** "*.jar").classpath
	    }) ++
      (test in assembly := {}) ++
      (
        mergeStrategy in assembly := ernieMerge
      )
    ):_*
  )

  project("ernie-engine")(
    libraryDependencies ~= { deps =>
      deps.filter( d =>           {
        !d.name.contains("csv") &&
        !d.name.contains("flute")
      }
      ).excluding("milyn","flute")
    },
	(unmanagedJars in Compile <++= unmanagedBase map { ub =>
	     (ub ** "*.jar").classpath
	})
 )
  
  project("ernie-model")(publishArtifact in (Compile, packageDoc) := false)
  project("ernie-util")(publishArtifact in (Compile, packageDoc) := false)
  project("ernie-java-api")(publishArtifact in (Compile, packageDoc) := false)

  project("ernie-server")(
    (com.earldouglas.xsbtwebplugin.WebPlugin.webSettings ++
    (libraryDependencies += "org.mortbay.jetty" % "jetty" % "6.1.22" % "container") ++
    (javacOptions ++= Seq(
        "ernie.props", "./ernie-server/src/main/resources/props/default.props"
       // "keystore.location=", "./ernie-server/src/test/resources/keystore.jks",
       // "authentication.mode", "SAML"
    ))):_*
  )

  project("ernie-gatling") {
    resolvers ++= Seq(gatlingReleasesRepo, gatling3PartyRepo)
  }
  
 

}
