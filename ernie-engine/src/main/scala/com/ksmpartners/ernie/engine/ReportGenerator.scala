package com.ksmpartners.ernie.engine

import org.eclipse.birt.report.engine.api._
import org.eclipse.birt.core.framework.Platform

class ReportGenerator(rptDefDirName: String, outputDirName: String, outputFormat: String  ) {

  var engine: IReportEngine = null

  def startup {
    val ec = new EngineConfig

    Platform.startup(ec)
    val iFactory = Platform.createFactoryObject( IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY )

    iFactory match {
      case fact: IReportEngineFactory => fact
      case _ => throw new ClassCastException
    }
    val factory = iFactory.asInstanceOf[IReportEngineFactory]

    engine = factory.createReportEngine(ec)
  }

  def runReport(rptDefName: String, outputFileName: String) {
    val filePath = rptDefDirName + "/" + rptDefName

    val design = engine.openReportDesign(filePath)
    val task: IRunAndRenderTask = engine.createRunAndRenderTask(design)
    val renderOption = new PDFRenderOption()
    renderOption.setOutputFileName(outputDirName + "/" + outputFileName)
    renderOption.setOutputFormat(outputFormat)

    task.setRenderOption(renderOption)
    task.run
    task.close
  }

  def shutdown {
    engine.destroy
    Platform.shutdown
  }

}
