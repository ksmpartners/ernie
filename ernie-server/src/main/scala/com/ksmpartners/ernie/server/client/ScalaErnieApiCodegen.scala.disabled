package com.ksmpartners.ernie.server.client
import com.wordnik.swagger.codegen.BasicScalaGenerator

object ScalaErnieApiCodegen extends BasicScalaGenerator {
  def main(args: Array[String]) = generateClient(args)

  def destinationRoot = "src/main/resources/ernie-api/scala"

  override def templateDir = "src/main/resources/client-templates/scala"

  // where to write generated code
  override def destinationDir = destinationRoot + "/ernie/src/main/scala"

  // package for api invoker
  override def invokerPackage = Some("com.ksmpartners.ernie.client.common")

  // package for models
  override def modelPackage = Some("com.ksmpartners.ernie.client.model")

  // package for api classes
  override def apiPackage = Some("com.ksmpartners.ernie.client.api")

  // supporting classes
  override def supportingFiles = List(
    ("apiInvoker.mustache", destinationDir + "/" + invokerPackage.get.replace(".", java.io.File.separator), "ApiInvoker.scala"),
    ("pom.mustache", destinationRoot, "pom.xml"))
}
