package com.ksmpartners.ernie.server.client
import com.wordnik.swagger.codegen.BasicPHPGenerator
import java.io.File

object PHPErnieApiCodegen extends BasicPHPGenerator {
  def main(args: Array[String]) = generateClient(args)

  override def templateDir = "src/main/resources/client-templates/php"

  override def destinationDir = "src/main/resources/ernie-api/php/ernie"

  override def supportingFiles = List(
    ("Swagger.mustache", destinationDir + File.separator + apiPackage.get, "Swagger.php"))
}
