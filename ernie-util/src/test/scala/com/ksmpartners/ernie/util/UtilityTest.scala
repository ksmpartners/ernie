/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.util

import com.ksmpartners.ernie.util.Utility._
import org.testng.annotations.Test
import java.io.{ FileOutputStream, IOException, File, Closeable }
import org.testng.Assert

class UtilityTest {

  @Test
  def canCloseReaderWithTry() {
    val cls = new Cls
    cls.open()
    Assert.assertFalse(cls.isClosed)
    try_(cls) { reader =>
      reader.doSomething()
      reader.close()
    }
    Assert.assertTrue(cls.isClosed)
  }

  @Test
  def canUseTryCatch() {
    val cls = new Cls
    cls.open()
    Assert.assertFalse(cls.isClosed)
    var caughtEx = false
    try_catch(cls) { reader =>
      reader.doSomething()
      reader.throwEx()
    }(catch_(classOf[Throwable]) { ex =>
      caughtEx = true
    })
    Assert.assertTrue(cls.isClosed)
    Assert.assertTrue(caughtEx)
  }

  @Test(expectedExceptions = Array(classOf[IllegalStateException]))
  def canUseTryCatchWithSpecificException() {
    class TestException extends Exception {}

    val cls = new Cls
    cls.open()
    Assert.assertFalse(cls.isClosed)
    var caughtEx = false
    try_catch(cls) { reader =>
      reader.doSomething()
      reader.throwEx()
    }(catch_(classOf[TestException]) { ex =>
      caughtEx = true
    })
  }

  @Test
  def catchBlockExceptionsHandledProperly() {
    val cls = new Cls
    cls.open()
    Assert.assertFalse(cls.isClosed)
    try {
      try_catch(cls) { reader =>
        reader.doSomething()
        reader.throwEx()
      }(catch_(classOf[Throwable]) { ex: Throwable =>
        throw new IllegalStateException("Exception AWAY!!")
      })
    } catch {
      case e =>
    }
    Assert.assertTrue(cls.isClosed)
  }

  @Test
  def canRecursivelyDeleteDirectory() {
    val tempDir = createTempDirectory()
    val tempSubDir = new File(tempDir, "subDir")
    tempSubDir.mkdir()
    val tempFile = new File(tempSubDir, "subFile")
    try_(new FileOutputStream(tempFile)) { fos =>
      fos.write(100)
    }
    recDel(tempDir)
    Assert.assertFalse(tempFile.exists())
    Assert.assertFalse(tempSubDir.exists())
    Assert.assertFalse(tempDir.exists())
  }

  private def createTempDirectory(): File = {

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

class Cls extends Closeable {

  var isClosed = true

  def doSomething() {
    if (isClosed)
      throw new IllegalStateException("Cls is closed")
  }

  def throwEx() {
    throw new IllegalStateException("Exception away!")
  }

  def close() {
    if (isClosed)
      throw new IllegalStateException("Cls is already closed")
    isClosed = true
  }

  def open() {
    if (!isClosed)
      throw new IllegalStateException("Cls is already open")
    isClosed = false
  }
}