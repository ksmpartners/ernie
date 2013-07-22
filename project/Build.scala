import sbt._
import Keys._
import com.github.shivawu.sbt.maven.MavenBuild

object ErnieBuild extends MavenBuild {

  val gatlingReleasesRepo = "Gatling Releases Repo" at "http://repository.excilys.com/content/repositories/releases"
  val gatling3PartyRepo = "Gatling Third-Party Repo" at "http://repository.excilys.com/content/repositories/thirdparty"

  project("*")(scalaVersion := "2.10.1") //, libraryDependencies += "org.scalatest" % "scalatest_2.10" % "2.0.M6-SNAP28")
  //libraryDependencies += "org.scalatest" % "scalatest_2.10" % "2.0.M6-SNAP28"


  project("ernie-engine") {
    libraryDependencies ~= { deps =>
      deps.filter( d =>
        !d.name.contains("org.eclipse.birt.report.engine.emitter.csv") &&
        !d.name.contains("flute")
      )
    }
  }

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
