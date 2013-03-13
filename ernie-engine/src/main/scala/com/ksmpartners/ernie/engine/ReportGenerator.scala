/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.engine

import org.eclipse.birt.report.engine.api._
import org.eclipse.birt.core.framework.Platform
import org.slf4j.LoggerFactory
import java.io._

/**
 * Class used to generate BIRT reports
 * <br><br>
 * This Class is not thread safe.
 */
class ReportGenerator(pathToDefinitions: String, pathToOutputs: String) {

  private val log = LoggerFactory.getLogger(this.getClass)

  private val rptDefDir: File = new File(pathToDefinitions)
  private val outputDir: File = new File(pathToOutputs)

  // Validate directories
  if (!(rptDefDir.isDirectory && rptDefDir.canRead) || !(outputDir.isDirectory && outputDir.canWrite)) {
    throw new IOException("Input/output directories do not exist or do not have the correct read/write access. " +
      "Def Dir: " + rptDefDir +
      ". Output Dir: " + outputDir)
  }

  private var engine: IReportEngine = null

  /**
   * Method to be called before any reports can be generated
   */
  def startup() {
    log.info("Starting Report Engine")
    val ec = new EngineConfig

    Platform.startup(ec)
    val factory = Platform.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY)

    engine = (factory match {
      case fact: IReportEngineFactory => fact
      case _ => throw new ClassCastException
    }).createReportEngine(ec)

  }

  /**
   * Method that runs the .rtpdesign file at the given location rptDefName, and outputs the results to outputFileName
   * as a .pdf
   */
  def runPdfReport(rptDefFileStream: InputStream, outputFileStream: OutputStream) {
    if (engine == null) throw new IllegalStateException("ReportGenerator was not started")
    val design = engine.openReportDesign(rptDefFileStream)
    val renderOption = new PDFRenderOption
    renderOption.setOutputStream(outputFileStream)
    renderOption.setOutputFormat("pdf")
    runReport(design, renderOption)
  }

  /**
   * Method that runs the .rtpdesign file at the given location rptDefName, and outputs the results to outputFileName
   * as a .pdf
   */
  def runPdfReport(rptDefName: String, outputFileName: String): File = {
    if (engine == null) throw new IllegalStateException("ReportGenerator was not started")
    log.debug("Generating PDF from report definition {}", rptDefName)
    val outputFile = new File(outputDir, outputFileName)
    try_(new FileInputStream(new File(rptDefDir, rptDefName))) { rptDefFileStream =>
      try_(new FileOutputStream(outputFile)) { outputFileStream =>
        runPdfReport(rptDefFileStream, outputFileStream)
      }
    }
    outputFile
  }

  /**
   * Method that runs the .rtpdesign file at the given location rptDefName, and outputs the results to outputFileName
   * as .html
   */
  def runHtmlReport(rptDefName: String, outputFileName: String): File = {
    if (engine == null) throw new IllegalStateException("ReportGenerator was not started")
    log.debug("Generating HTML from report definition {}", rptDefName)
    val outputFile = new File(outputDir, outputFileName)
    try_(new FileInputStream(new File(rptDefDir, rptDefName))) { rptDefFileStream =>
      try_(new FileOutputStream(outputFile)) { outputFileStream =>
        val design = engine.openReportDesign(rptDefFileStream)
        val renderOption = new HTMLRenderOption
        renderOption.setOutputStream(outputFileStream)
        renderOption.setOutputFormat("html")
        runReport(design, renderOption)
      }
    }
    outputFile
  }

  /**
   * Method that creates and runs a BIRT task based on the given design and options
   */
  private def runReport(design: IReportRunnable, option: RenderOption) {
    log.debug("BEGIN Running report...")
    val task: IRunAndRenderTask = engine.createRunAndRenderTask(design)
    task.setRenderOption(option)
    task.run
    task.close
    log.debug("END Running report...")
  }

  /**
   * Method to be called after all the reports have been run.
   */
  def shutdown() {
    log.info("BEGIN Shutting down Report Engine")
    engine.destroy()
    Platform.shutdown()
    engine = null
    log.info("END Shutting down Report Engine")
  }

  // TODO: Create util package/class/object
  /**
   * Method that mimics Java 1.7's try-with-resources
   *
   * Usage:
   * try_(new Closable...) { closableInstance =>
   *   closableInstance.doSomething()
   * }
   *
   */
  private def try_[A <% Closeable](ac: A)(f: A => Unit) {
    try {
      f(ac)
    } finally {
      try {
        ac.close()
      } catch {
        case e =>
      }
    }
  }

}
