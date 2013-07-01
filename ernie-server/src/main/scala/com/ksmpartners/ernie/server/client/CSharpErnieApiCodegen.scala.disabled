package com.ksmpartners.ernie.server.client
import com.wordnik.swagger.codegen.BasicCSharpGenerator

object CSharpErnieApiCodegen extends BasicCSharpGenerator {
  def main(args: Array[String]) = generateClient(args)

  // location of templates
  override def templateDir = "src/main/resources/client-templates/csharp"

  // where to write generated code
  override def destinationDir = "src/main/resources/ernie-api/csharp/ernie"

  // package for api invoker, error files
  override def invokerPackage = Some("com.ksmpartners.ernie")

  // package for models
  override def modelPackage = Some("com.ksmpartners.ernie.model")

  // package for api classes
  override def apiPackage = Some("com.ksmpartners.ernie.api")

  // supporting classes
  override def supportingFiles =
    List(
      ("apiInvoker.mustache", destinationDir + java.io.File.separator + invokerPackage.get.replace(".", java.io.File.separator) + java.io.File.separator, "ApiInvoker.cs"),
      ("apiException.mustache", destinationDir + java.io.File.separator + invokerPackage.get.replace(".", java.io.File.separator) + java.io.File.separator, "ApiException.cs"),
      ("Newtonsoft.Json.dll", "samples/client/petstore/csharp/bin", "Newtonsoft.Json.dll"),
      ("compile.mustache", "samples/client/petstore/csharp", "compile.bat"))
}
