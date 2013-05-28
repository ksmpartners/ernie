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
import com.ksmpartners.ernie.engine.report.BirtReportGenerator._
import org.eclipse.birt.report.engine.emitter.csv.CSVRenderOption
import com.ksmpartners.ernie.util.Utility._
import scala.collection._
import org.joda.time.DateTime

/**
 * Class used to generate BIRT reports
 * <br><br>
 * This Class is not thread safe.
 */
class BirtReportGenerator(reportManager: ReportManager) extends ReportGenerator {

  def startup() { startEngine() }

  /**
   * Get the list of available definitions
   */
  def getAvailableRptDefs: List[String] = reportManager.getAllDefinitionIds

  /**
   * Method that runs the design file at the given location defId, and outputs the results to rptId
   * as a rptType
   */
  def runReport(defId: String, rptId: String, rptType: ReportType, retentionDate: Option[Int]) {
    if (engine == null) throw new IllegalStateException("ReportGenerator was not started")
    log.debug("Generating report from definition {}", defId)
    try_(reportManager.getDefinitionContent(defId).get) { defInputStream =>
      val entity: mutable.Map[String, Any] = new mutable.HashMap()
      entity += (ReportManager.rptId -> rptId)
      entity += (ReportManager.sourceDefId -> defId)
      entity += (ReportManager.reportType -> rptType)
      entity += (ReportManager.createdUser -> "default")
      entity += (ReportManager.retentionDate -> DateTime.now().plusDays(retentionDate getOrElse (reportManager.getDefaultRetentionDays)))
      try_(reportManager.putReport(entity)) { rptOutputStream =>
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
    val task: IRunAndRenderTask = engine.createRunAndRenderTask(design)
    task.setRenderOption(option)
    task.run()
    task.close()
  }

  /**
   * Method to be called after all the reports have been run.
   */
  def shutdown() { shutdownEngine() }

}

object BirtReportGenerator {

  protected[report] var engine: IReportEngine = null
  private val log = LoggerFactory.getLogger("c.k.e.e.report.BirtReportGenerator")

  /**
   * Method to be called before any reports can be generated
   */
  protected[report] def startEngine() {
    if (engine != null)
      return
    val ec = new EngineConfig
    Platform.startup(ec)

    val factory = Platform.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY)
      .asInstanceOf[IReportEngineFactory]

    engine = factory.createReportEngine(ec)
    log.debug("BIRT Engine started.")
  }

  protected[report] def shutdownEngine() {
    if (engine == null)
      return
    engine.destroy()
    Platform.shutdown()
    engine = null
    log.debug("BIRT Engine shutdown.")
  }

  /**
   * Method that validates a report definition
   */
  def isValidDefinition(is: InputStream): Boolean = try {
    if (engine == null) {
      return false
    }
    engine.openReportDesign(is)
    true
  } catch {
    case e: Exception => false
  }
}
