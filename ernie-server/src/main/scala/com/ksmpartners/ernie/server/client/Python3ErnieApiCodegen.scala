package com.ksmpartners.ernie.server.client
import com.wordnik.swagger.codegen.BasicPython3Generator

import java.io.File

object Python3ErnieApiCodegen extends BasicPython3Generator {
  def main(args: Array[String]) = generateClient(args)

  override def templateDir = "src/main/resources/client-templates/python3"

  def destinationRoot = "src/main/resources/ernie-api/python3/ernie"

  // where to write generated code
  override def destinationDir = destinationRoot

  // supporting classes
  override def supportingFiles = List(
    ("__init__.mustache", destinationDir, "__init__.py"),
    ("swagger.mustache", destinationDir + File.separator + apiPackage.getOrElse(""), "swagger.py"),
    ("__init__.mustache", destinationDir + File.separator + modelPackage.getOrElse(""), "__init__.py"))
}
