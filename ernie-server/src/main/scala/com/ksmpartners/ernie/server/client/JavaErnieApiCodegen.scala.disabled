package com.ksmpartners.ernie.server.client
import com.wordnik.swagger.codegen.BasicJavaGenerator

object JavaErnieApiCodegen extends BasicJavaGenerator {
  def main(args: Array[String]) = generateClient(args)

  // location of templates
  override def templateDir = "src/main/resources/client-templates/Java"

  def destinationRoot = "src/main/resources/ernie-api/java"

  // where to write generated code
  override def destinationDir = destinationRoot + "/ernie/src/main/java"

  // package for api invoker, error files
  override def invokerPackage = Some("com.ksmpartners.ernie.client.common")

  // package for models
  override def modelPackage = Some("com.ksmpartners.ernie.client.model")

  // package for api classes
  override def apiPackage = Some("com.ksmpartners.ernie.client.api")

  additionalParams ++= Map(
    "artifactId" -> "ernie-java-client",
    "artifactVersion" -> "1.0.0",
    "groupId" -> "com.ksmpartners")

  // supporting classes
  override def supportingFiles =
    List(
      ("apiInvoker.mustache", destinationDir + java.io.File.separator + invokerPackage.get.replace(".", java.io.File.separator) + java.io.File.separator, "ApiInvoker.java"),
      ("JsonUtil.mustache", destinationDir + java.io.File.separator + invokerPackage.get.replace(".", java.io.File.separator) + java.io.File.separator, "JsonUtil.java"),
      ("apiException.mustache", destinationDir + java.io.File.separator + invokerPackage.get.replace(".", java.io.File.separator) + java.io.File.separator, "ApiException.java"),
      ("pom.mustache", destinationRoot, "pom.xml"))
}
