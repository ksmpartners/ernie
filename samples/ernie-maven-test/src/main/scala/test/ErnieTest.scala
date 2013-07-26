package test

import  com.ksmpartners.ernie.api._
import com.ksmpartners.ernie.util.Utility._
import ErnieBuilder._
import scala.concurrent.duration._
import com.ksmpartners.ernie.model.ParameterEntity
import scala.Long
import com.ksmpartners.ernie.model

object ErnieTest {
	
	/*val engine = ErnieEngine(ErnieBuilder()
		.withMemoryReportManager()
		.withDefaultRetentionDays(7)
		.withMaxRetentionDays(14)
		.withWorkers(100)
	.build())*/

	val ernie = ErnieEngine(
      ernieBuilder
        withFileReportManager (createTempDirectory.getAbsolutePath, createTempDirectory().getAbsolutePath, createTempDirectory().getAbsolutePath)
        withDefaultRetentionDays (5)
        withMaxRetentionDays (10)
        timeoutAfter (5 minutes)
        withWorkers (5)
        build ()).start


	def cj(fil:String, params:Map[String, String]):(Long, model.JobStatus) = {
		val design = scala.xml.XML.loadFile(fil)

		val d = ernie.createDefinition(Some(new java.io.ByteArrayInputStream(design.toString.getBytes)), "test", "adam")
		ernie.createJob(d.getDefId, com.ksmpartners.ernie.model.ReportType.PDF, None, Map.empty[String, String], "adam")
	}

}
