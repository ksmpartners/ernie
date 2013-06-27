package com.ksmpartners.ernie.server.client
import com.wordnik.swagger.codegen.BasicRubyGenerator

object RubyErnieApiCodegen extends BasicRubyGenerator {
  def main(args: Array[String]) = generateClient(args)

  // to avoid recompiling ...
  override def templateDir = "src/main/resources/client-templates/ruby"
  // where to write generated code
  override def destinationDir = "src/main/resources/ernie-api/ruby/ernie"
}