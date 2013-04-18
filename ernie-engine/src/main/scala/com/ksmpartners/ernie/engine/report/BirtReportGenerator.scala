/**
 * This source code file is the intellectual property of KSM Technology Partners LLC.
 * The contents of this file may not be reproduced, published, or distributed in any
 * form, except as allowed in a license agreement between KSM Technology Partners LLC
 * and a licensee. Copyright 2012 KSM Technology Partners LLC.  All rights reserved.
 */

package com.ksmpartners.ernie.engine.report

import org.eclipse.birt.report.engine.api._
import org.eclipse.birt.core.framework.Platform
import org.slf4j.LoggerFactory
import java.io._
import com.ksmpartners.ernie.model.ReportType
import org.eclipse.birt.report.engine.emitter.csv.CSVRenderOption
import com.ksmpartners.ernie.util.FileUtils._

/**
 * Class used to generate BIRT reports
 * <br><br>
 * This Class is not thread safe.
 */
class BirtReportGenerator(reportManager: ReportManager) extends ReportGenerator {

  private val log = LoggerFactory.getLogger(classOf[ReportGenerator])

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
   * Get the list of available definitions
   */
  def getAvailableRptDefs: List[String] = reportManager.getAllDefinitionIds

  /**
   * Method that runs the design file at the given location defId, and outputs the results to rptId
   * as a rptType
   */
  def runReport(defId: String, rptId: String, rptType: ReportType) {
    if (engine == null) throw new IllegalStateException("ReportGenerator was not started")
    log.debug("Generating PDF from report definition {}", defId)
    try_(reportManager.getDefinition(defId).get) { defInputStream =>
      try_(reportManager.putReport(rptId, rptType)) { rptOutputStream =>
        runReport(defInputStream, rptOutputStream, rptType)
      }
    }
  }

  /**
   * Method that runs the .rtpdesign file in the input stream defInputStream, and outputs the results to
   * rptOutputStream as rptType
   */
  def runReport(defInputStream: InputStream, rptOutputStream: OutputStream, rptType: ReportType) {
    if (engine == null) throw new IllegalStateException("ReportGenerator was not started")
    val design = engine.openReportDesign(defInputStream)
    var renderOption: RenderOption = null
    rptType match {
      case ReportType.PDF => {
        renderOption = new PDFRenderOption
        renderOption.setOutputFormat("pdf")
      }
      case ReportType.CSV => {
        renderOption = new CSVRenderOption
        renderOption.setOutputFormat("csv")
      }
      case ReportType.HTML => {
        renderOption = new HTMLRenderOption
        renderOption.setOutputFormat("html")
      }
      case t => {
        log.error("Invalid report type: {}", t)
        throw new IllegalArgumentException("Invalid report type: " + t)
      }
    }
    renderOption.setOutputStream(rptOutputStream)
    runReport(design, renderOption)
  }

  /**
   * Method that creates and runs a BIRT task based on the given design and options
   */
  private def runReport(design: IReportRunnable, option: RenderOption) {
    log.debug("BEGIN Running report...")
    val task: IRunAndRenderTask = engine.createRunAndRenderTask(design)
    task.setRenderOption(option)
    task.run()
    task.close()
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

}
