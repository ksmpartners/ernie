package ernie

import io.gatling.core.action.{Chainable, Interruptable}
import io.gatling.core.session.Session
import akka.actor.ActorRef
import com.ksmpartners.ernie.api._
import com.ksmpartners.ernie.model._
import com.typesafe.scalalogging.slf4j.Logging
import io.gatling.core.config.ProtocolConfiguration
import io.gatling.core.result.writer.DataWriter
import io.gatling.core.result.message.Status

/**
 * Created with IntelliJ IDEA.
 * User: acoimbra
 * Date: 6/21/13
 * Time: 10:38 AM
 * To change this template use File | Settings | File Templates.
 */
class ErnieProtocol {

}
object ErnieProtocolConfiguration {
  val HTTP_PROTOCOL_TYPE = "httpProtocol"
  var embedded = true
}

/**
 * Class containing the configuration for the HTTP protocol
 *
 * @param baseUrl the radix of all the URLs that will be used (eg: http://mywebsite.tld)
 * @param proxy a proxy through which all the requests must pass to succeed
 */
class ErnieProtocolConfiguration(embedded:Boolean = true) extends ProtocolConfiguration {
  def getEmbedded = embedded
}
class PostJobAction(requestName:String, val next:ActorRef) extends Interruptable with Logging {
  def execute(session:Session) {
    val ernie = if (ErnieProtocolConfiguration.embedded) new ErnieAPI else null


    val requestStartDate = System.currentTimeMillis()
    System.out.println("Posting a job")
    val resp = ernie.createJob("test_def", ReportType.PDF, Some(1), Map.empty[String,String], "test")
    val responseEndDate = System.currentTimeMillis()
    val endOfRequestSendingDate = System.currentTimeMillis()
    val requestResult = if (resp.error.isEmpty && (resp.jobId > 0)) {
      Status.
    } else {
      RequestStatus.KO
    }

    DataWriter.logRequest(session.scenarioName, session.userId, "Request " + requestName, requestStartDate, responseEndDate, endOfRequestSendingDate, endOfRequestSendingDate, requestResult, requestMessage)

  }
}