package com.ksmpartners.ernie.server.client
import com.wordnik.swagger.codegen.BasicObjcGenerator

object ObjcErnieApiCodegen extends BasicObjcGenerator {
  def main(args: Array[String]) = generateClient(args)

  override def templateDir = "src/main/resources/client-templates/objc"

  // where to write generated code
  override def destinationDir = "src/main/resources/ernie-api/objc/client"

  // supporting classes
  override def supportingFiles =
    List(
      ("NIKSwaggerObject.h", destinationDir, "NIKSwaggerObject.h"),
      ("NIKSwaggerObject.m", destinationDir, "NIKSwaggerObject.m"),
      ("NIKApiInvoker.h", destinationDir, "NIKApiInvoker.h"),
      ("NIKApiInvoker.m", destinationDir, "NIKApiInvoker.m"),
      ("NIKDate.h", destinationDir, "NIKDate.h"),
      ("NIKDate.m", destinationDir, "NIKDate.m"))
}
