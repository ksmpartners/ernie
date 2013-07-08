package com.ksmpartners.ernie.server.client
import com.wordnik.swagger.codegen.BasicPythonGenerator

import java.io.File

object PythonErnieApiCodegen extends BasicPythonGenerator {
  def main(args: Array[String]) = generateClient(args)

  def destinationRoot = "src/main/resources/ernie-api/python/ernie"
  override def templateDir = "src/main/resources/client-templates/python"

  // where to write generated code
  override def destinationDir = destinationRoot

  // supporting classes
  override def supportingFiles = List(
    ("__init__.mustache", destinationDir, "__init__.py"),
    ("swagger.mustache", destinationDir + File.separator + apiPackage.getOrElse(""), "swagger.py"),
    ("__init__.mustache", destinationDir + File.separator + modelPackage.getOrElse(""), "__init__.py"))
}
