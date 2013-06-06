/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.util

import java.io.{ IOException, FileNotFoundException, File, Closeable }

object Utility {

  /**
   * Method that mimics Java 1.7's try-with-resources
   *
   * Usage:
   * try_(new Closable...) { closableInstance =>
   *   closableInstance.doSomething()
   * }
   */
  def try_[A <: Closeable](closeable: A)(tryBlock: A => Unit) {
    try {
      tryBlock(closeable)
    } finally {
      try {
        closeable.close()
      } catch {
        case e: Throwable =>
      }
    }
  }

  private implicit def funcAsPartial(f: Throwable => Unit) = new {
    def asPartial(isDefinedAt: Throwable => Boolean): PartialFunction[Throwable, Unit] = {
      case a if isDefinedAt(a) => f(a)
    }
  }

  /**
   * Helper method to be used with try_catch. See that method's documentation for usage.
   */
  def catch_[T <: Throwable](clazz: Class[T])(f: Throwable => Unit) = f.asPartial(e => clazz.isInstance(e))

  /**
   * Method that mimics Java 1.7's try-with-resources with a catch block
   *
   * Usage:
   * try_catch(new Closable...) { closableInstance =>
   *   closableInstance.doSomething()
   * }(catch_(classOf[DesiredException){
   *   case e => handleException(e)
   * })
   */
  def try_catch[A <: Closeable](closeable: A)(tryBlock: A => Unit)(catchBlock: PartialFunction[Throwable, Unit]) {
    try {
      tryBlock(closeable)
    } catch {
      case e: Throwable => if (catchBlock.isDefinedAt(e)) catchBlock(e) else throw e
    }
    finally {
      try {
        closeable.close()
      } catch {
        case e: Throwable =>
      }
    }
  }

  /**
   * Deletes a File. If the File is a directory, all sub files and directories will also be deleted
   */
  def recDel(file: File) {
    if (file.isDirectory) {
      for (f <- file.listFiles()) {
        recDel(f)
      }
      if (!file.delete())
        throw new FileNotFoundException("Failed to delete file: " + file)
    } else {
      if (!file.delete())
        throw new FileNotFoundException("Failed to delete file: " + file)
    }
  }

  def jobToRptId(jobId: Long): String = "REPORT_" + jobId.toString
  def rptToJobId(rptId: String): Long = rptId.replaceAll("REPORT_", "").toLong
  def createTempDirectory(): File = {

    var temp: File = null

    temp = File.createTempFile("temp", System.nanoTime.toString)

    if (!(temp.delete())) {
      throw new IOException("Could not delete temp file: " + temp.getAbsolutePath)
    }

    if (!(temp.mkdir())) {
      throw new IOException("Could not create temp directory: " + temp.getAbsolutePath)
    }

    temp
  }
}
