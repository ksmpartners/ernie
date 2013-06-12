/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.util

import com.ksmpartners.ernie.util.Utility._
import org.testng.annotations.Test
import java.io._
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
  def canConvertReportAndJobIds() {
    Assert.assertEquals(jobToRptId(1234L), "REPORT_1234")
    Assert.assertEquals(rptToJobId("REPORT_1234"), 1234L)
  }

  @Test(expectedExceptions = Array(classOf[NumberFormatException]))
  def badReportIdThrowsException() {
    rptToJobId("REPORT_NOT_A_NUMBER")
  }

  var tempDir: File = null

  @Test
  def canCreateTempDir() {
    tempDir = createTempDirectory()
    Assert.assertTrue(tempDir.exists())
  }

  @Test(dependsOnMethods = Array("canCreateTempDir"))
  def canRecursivelyDeleteDirectory() {
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

  @Test(expectedExceptions = Array(classOf[FileNotFoundException]))
  def nonExistantDirThrowsException() {
    val tempTestDir = createTempDirectory()
    recDel(tempTestDir)
    recDel(tempTestDir)
  }

  @Test(expectedExceptions = Array(classOf[FileNotFoundException]))
  def nonExistantFileThrowsException() {
    val tempFile = new File(tempDir, "subFile")
    try_(new FileOutputStream(tempFile)) { fos =>
      fos.write(100)
    }
    recDel(tempFile)
    recDel(tempFile)
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