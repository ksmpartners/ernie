package com.ksmpartners.ernie.engine

import org.eclipse.birt.report.engine.api._
import org.eclipse.birt.core.framework.Platform

class ReportGenerator(pathToDefinitions: String, pathToOutputs: String) {

  var engine: IReportEngine = null

  def startup {
    val ec = new EngineConfig

    Platform.startup(ec)
    val factory = Platform.createFactoryObject( IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY )

    engine = (factory match {
      case fact: IReportEngineFactory => fact
      case _ => throw new ClassCastException
    }).createReportEngine(ec)

  }

  def runPdfReport(rptDefName: String, outputFileName: String) {
    val filePath = pathToDefinitions + "/" + rptDefName
    val design = engine.openReportDesign(filePath)
    val renderOption = new PDFRenderOption
    renderOption.setOutputFileName(pathToOutputs + "/" + outputFileName)
    renderOption.setOutputFormat("pdf")
    runReport(design, renderOption)
  }

  def runHtmlReport(rptDefName: String, outputFileName: String) {
    val filePath = pathToDefinitions + "/" + rptDefName
    val design = engine.openReportDesign(filePath)
    val renderOption = new HTMLRenderOption
    renderOption.setOutputFileName(pathToOutputs + "/" + outputFileName)
    renderOption.setOutputFormat("html")
    runReport(design, renderOption)
  }

  private def runReport(design: IReportRunnable, option: RenderOption) {
    val task: IRunAndRenderTask = engine.createRunAndRenderTask(design)
    task.setRenderOption(option)
    task.run
    task.close
  }

  def shutdown {
    engine.destroy
    Platform.shutdown
  }

}
