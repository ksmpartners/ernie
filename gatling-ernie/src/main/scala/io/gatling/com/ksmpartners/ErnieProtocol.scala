package io.gatling.com.ksmpartners

import io.gatling.core.action.{ Chainable, Interruptable }
import io.gatling.core.Predef._
import io.gatling.core.session.Session
import io.gatling.http._
import action._
import akka.actor.{ Props, ActorRef }
import com.ksmpartners.ernie.api._
import com.ksmpartners.ernie.model._
import com.typesafe.scalalogging.slf4j.Logging
import io.gatling.core.config.{ ProtocolConfigurationRegistry, ProtocolConfiguration }
import io.gatling.core.result.writer.DataWriter
import io.gatling.core.result.message.{ RequestMessage, KO, OK, Status }
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.action._
import java.io.File
import io.gatling.core.session.EL

import io.gatling.core.validation.Success
import scala.concurrent.duration.{ FiniteDuration, Duration }
import org.joda.time.DateTime
import io.gatling.http.request.builder.{ AbstractHttpRequestWithBodyAndParamsBuilder, HttpRequestBaseBuilder }
import io.gatling.http.Predef._
import io.gatling.com.ksmpartners.DeleteResp
import scala.{ util, Some }
import io.gatling.com.ksmpartners.DefList
import io.gatling.core.validation.Success
import io.gatling.core.result.message.RequestMessage
import io.gatling.core.session.Session
import com.ksmpartners.ernie.api.Definition
import com.ksmpartners.ernie.model.JobStatus
import com.ksmpartners.ernie.api.JobStatus
import com.ksmpartners.ernie.api.ReportOutputException
import com.ksmpartners.ernie.api.ReportOutput
import com.ksmpartners.ernie.util.MapperUtility._
import io.gatling.com.ksmpartners.DeleteResp
import scala.Some
import io.gatling.com.ksmpartners.DefList
import io.gatling.core.validation.Success
import io.gatling.core.result.message.RequestMessage
import io.gatling.core.session.Session
import com.ksmpartners.ernie.api.Definition
import com.ksmpartners.ernie.model.JobStatus
import com.ksmpartners.ernie.api.JobStatus
import com.ksmpartners.ernie.api.ReportOutputException
import com.ksmpartners.ernie.api.ReportOutput
import io.gatling.core.structure.{ AbstractStructureBuilder, ChainBuilder, ConditionalStatements, Execs }

class ErnieProtocol {

}
object ErnieProtocolConfiguration {
  val HTTP_PROTOCOL_TYPE = "httpProtocol"
  var embedded = true
}

class ErnieProtocolConfiguration(embedded: Boolean = true) extends ProtocolConfiguration {
  def getEmbedded = embedded
}

trait ErnieActionDefinition {
  val actionFunc: (Session) => (Session, Expression[ErnieResponse])
  val afterFunc: (Session, Expression[ErnieResponse]) => Session
  val validationFunc: (Session, Expression[ErnieResponse]) => Boolean
  val requestName: String
  def successMessage(s: Session, e: Expression[ErnieResponse]): String
  def failMessage(s: Session, e: Expression[ErnieResponse]): String
}

class ErnieAction(actionDef: ErnieActionDefinition, protocolConfigReg: ProtocolConfigurationRegistry, val next: ActorRef, rB: ErnieActionBuilder) extends Interruptable with Logging {

  val requestBuilder: ErnieActionBuilder = rB

  def execute(session: Session) {

    val requestStartDate = System.currentTimeMillis()
    var (s, resp) = actionDef.actionFunc.apply(session)
    val responseEndDate = System.currentTimeMillis()
    val valid = actionDef.validationFunc(s, resp)
    val requestResult = if (valid) {
      s = actionDef.afterFunc(session, resp)
      OK
    } else {
      KO
    }
    val requestMessage = if (valid) {
      actionDef.requestName + " successful: " + actionDef.successMessage(s, resp)
    } else {
      actionDef.requestName + " failed" + actionDef.failMessage(s, resp)
    }

    DataWriter.tell(RequestMessage(s.scenarioName, s.userId, s.groupStack, "Request " + actionDef.requestName,
      requestStartDate, responseEndDate, requestStartDate, responseEndDate,
      requestResult, Some(requestMessage)))

    next ! s

  }
}

class ErnieActionBuilder(actionDef: ErnieActionDefinition, next: ActorRef) extends ActionBuilder {
  /**
   * Something todo with the action chain. Creates a new instance of our builder with a new
   * next action point.
   */
  private[gatling] def withNext(next: ActorRef) = new ErnieActionBuilder(actionDef, next)

  private[gatling] def build(next: ActorRef, protocolConfigurationRegistry: ProtocolConfigurationRegistry): ActorRef = {
    system.actorOf(Props(new ErnieAction(actionDef, protocolConfigurationRegistry, next, this)))
  }
}

class PostJobActionBuilder(val defId: Expression[String], val user: Expression[String] = value2Expression("test"), val rptType: Expression[ReportType] = value2Expression(ReportType.PDF)) extends ErnieActionDefinition {
  import com.ksmpartners.ernie.api.JobStatus
  import io.gatling.http.Predef._
  val actionFunc = (s: Session) => {
    (s, (for {
      d <- defId(s)
      us <- user(s)
      rp <- rptType(s)
    } yield value2Expression(build.createJob(d, rp, Some(4), Map.empty[String, String], us))) match {
      case Success(s: Expression[JobStatus]) => s
      case _ => value2Expression(com.ksmpartners.ernie.api.JobStatus(-1L, None, Some(new Exception)))
    })
  }

  val validationFunc = (s: Session, e: Expression[ErnieResponse]) => e.apply(s) match {
    case Success(f: ErnieResponse) =>
      if (f.errorOpt.isEmpty && f.isInstanceOf[com.ksmpartners.ernie.api.JobStatus] && (f.asInstanceOf[com.ksmpartners.ernie.api.JobStatus].jobId > 0))
        true
      else false
    case _ => false
  }

  val afterFunc = (s: Session, e: Expression[ErnieResponse]) => e.apply(s) match {
    case Success(jS: com.ksmpartners.ernie.api.JobStatus) => {
      val jobs: List[Long] = s.get[List[Long]]("jobs") getOrElse List.empty[Long]
      val ret = s.set("jobs", jobs.::(jS.jobId)).set("currentJob", jS.jobId)
      ret
    }
    case _ => s
  }

  val requestName = "Post Job"

  def successMessage(s: Session, e: Expression[ErnieResponse]) = e.apply(s) match {
    case Success(f) => f.asInstanceOf[com.ksmpartners.ernie.api.JobStatus].jobId + " created with JS=" + f.asInstanceOf[com.ksmpartners.ernie.api.JobStatus].jobStatus + " and def=" + defId(s)
    case _ => "did not actually succeed"
  }
  def failMessage(s: Session, er: Expression[ErnieResponse]) = er(s) match {
    case Success(e: ErnieResponse) => e.errorOpt.map(f => f.getClass + " " + f.getMessage) getOrElse ("Unknown failure")
    case _ => "Unknown failure"
  }

  //def defId(dId : Expression[String]):PostJobActionBuilder = new PostJobActionBuilder(dId, user, rptType)
  def user(u: String): PostJobActionBuilder = new PostJobActionBuilder(defId, EL.compile[String](u), rptType)
  def rptType(r: ReportType): PostJobActionBuilder = new PostJobActionBuilder(defId, user, value2Expression(r))

  private[gatling] def build = {
    new ErnieAPI
  }

  private[gatling] def toActionBuilder = new ErnieActionBuilder(this, null)

}

class GetResultActionBuilder(j: Expression[Long] = value2Expression(-1L), waitFor: Duration = FiniteDuration(10, "seconds")) extends ErnieActionDefinition {

  val actionFunc = (s: Session) => {
    var ses = s
    val job = j(s) match {
      case Success(jId: Long) if (jId > 0L) => jId
      case _ => ses.get[Long]("currentJob") getOrElse (ses.get[List[Long]]("jobs").map(
        jobs => {
          if (jobs.isInstanceOf[List[Long]]) {
            val jobList = jobs
            if (jobList.isDefinedAt(0)) {
              ses = ses.set("jobs", jobList.drop(0))
              jobList(0)
            } else -1L
          } else -1L
        }) getOrElse -1L)
    }
    ses = ses.set("currentJob", job)
    val end = DateTime.now().plusMillis(waitFor.toMillis.toInt)
    var res = build.getReportOutput(job)
    var running = true
    import com.ksmpartners.ernie.model
    while (DateTime.now.isBefore(end) && running)
      if ((res.error.map(f => if (f.isInstanceOf[ReportOutputException])
        f.asInstanceOf[ReportOutputException].status getOrElse model.JobStatus.FAILED
      else model.JobStatus.FAILED) getOrElse model.JobStatus.FAILED) == model.JobStatus.IN_PROGRESS) {
        res = build.getReportOutput(job)
      } else running = false
    (ses, value2Expression(res))
  }
  val validationFunc = (s: Session, e: Expression[ErnieResponse]) => e.apply(s) match {
    case Success(e: ReportOutput) =>
      if (e.stream.isDefined)
        true
      else {
        s.get[Long]("currentJob").map(job => {
          build.getJobStatus(job).jobStatus.map(jS => {
            if (jS == com.ksmpartners.ernie.model.JobStatus.FAILED_NO_SUCH_DEFINITION) true
            else false
          }) getOrElse false
        }) getOrElse false
      }
    case _ => false
  }
  val afterFunc = (s: Session, e: Expression[ErnieResponse]) => {
    s.remove("currentJob")
  }
  val requestName = "Get Job Result"

  private[gatling] def build = {
    new ErnieAPI
  }

  def jobId(j: String): GetResultActionBuilder = new GetResultActionBuilder(EL.compile[Long](j), waitFor)
  def jobId(j: Long): GetResultActionBuilder = new GetResultActionBuilder(value2Expression(j), waitFor)
  def wait(w: Duration): GetResultActionBuilder = new GetResultActionBuilder(j, w)

  def successMessage(s: Session, er: Expression[ErnieResponse]) = er(s) match {
    case Success(e: ReportOutput) => "RptEnt" + e.rptEnt
    case _ => "Did not actually succeed"
  }

  def failMessage(s: Session, er: Expression[ErnieResponse]) = er(s) match {
    case Success(e: ReportOutput) => e.errorOpt.map(f => if (f.getClass == classOf[ReportOutputException]) f.asInstanceOf[ReportOutputException].toString + "\t" + f.asInstanceOf[ReportOutputException].status else f.getClass + " " + f.getMessage) getOrElse "Unknown failure"
    case _ => "Unknown failure"
  }

  private[gatling] def toActionBuilder = new ErnieActionBuilder(this, null)

}

case class DefList(list: List[String], e: Option[Exception]) extends ErnieResponse(e)

class GetDefsActionBuilder extends ErnieActionDefinition {

  val actionFunc = (s: Session) => {
    try {
      val (dL, e) = build.getDefinitionList
      (s, value2Expression(DefList(dL, e)))
    } catch {
      case e: Exception => (s, value2Expression(DefList(Nil, (Some(e)))))
    }
  }
  val validationFunc = (s: Session, e: Expression[ErnieResponse]) => e.apply(s) match {
    case Success(er: DefList) => !er.list.isEmpty
    case _ => false
  }
  val afterFunc = (s: Session, e: Expression[ErnieResponse]) => e.apply(s) match {
    case Success(er: DefList) => {
      val defs: List[String] = s.get[List[String]]("defs") getOrElse List.empty[String]
      s.set("defs", er.list ::: defs)
    } case _ => s
  }

  val requestName = "Get Definitions"

  def successMessage(s: Session, er: Expression[ErnieResponse]) = er(s) match {
    case Success(e: DefList) => e.toString
    case _ => "\"Success\" dubious"
  }

  def failMessage(s: Session, er: Expression[ErnieResponse]) = er(s) match {
    case Success(e: DefList) => e.errorOpt.map(f => f.getClass + " " + f.getMessage) getOrElse ("Unknown failure")
    case _ => "Unknown failure"
  }

  private[gatling] def build = {
    new ErnieAPI
  }

  private[gatling] def toActionBuilder = new ErnieActionBuilder(this, null)

}

class CreateDefActionBuilder(val defLoc: Expression[String], val user: Expression[String] = value2Expression("testUser")) extends ErnieActionDefinition {

  val actionFunc = (s: Session) => (s, (for {
    d <- defLoc(s)
    u <- user(s)
  } yield {
    try {
      value2Expression(build.createDefinition(Some(Right(scala.xml.XML.loadFile(new File(d)).toString.getBytes)), "Test definition", u))
    } catch {
      case e: Exception => value2Expression(com.ksmpartners.ernie.api.ErnieResponse(Some(e)))
    }
  }) match {
    case Success(r: Expression[ErnieResponse]) => r
    case _ => value2Expression(ErnieResponse(Some(new Exception)))
  })

  val validationFunc = (s: Session, er: Expression[ErnieResponse]) => er(s) match {
    case Success(e: Definition) => e.defEnt.isDefined
    case _ => false
  }
  val afterFunc = (s: Session, er: Expression[ErnieResponse]) => er(s) match {
    case Success(e: Definition) => {
      val defs: List[String] = s.get[List[String]]("defs") getOrElse List.empty[String]
      e.defEnt.map(dE => s.set("defs", defs.::(dE.getDefId))) getOrElse s
    } case _ => s
  }

  val requestName = "Create Definition"

  def successMessage(s: Session, er: Expression[ErnieResponse]) = er(s) match {
    case Success(e: Definition) => (e.defEnt getOrElse ("Did not actually succeed")).toString
    case _ => "did not actually succeed"
  }

  def failMessage(s: Session, er: Expression[ErnieResponse]) = er(s) match {
    case Success(e: Definition) => e.errorOpt.map(f => f.getClass + " " + f.getMessage) getOrElse ("Unknown failure")
    case _ => "Unknown failure"
  }

  //def location(loc : Expression[String]):CreateDefActionBuilder = new CreateDefActionBuilder(loc, user)
  def user(u: String): CreateDefActionBuilder = new CreateDefActionBuilder(defLoc, EL.compile[String](u))

  private[gatling] def build = {
    new ErnieAPI
  }

  private[gatling] def toActionBuilder = new ErnieActionBuilder(this, null)

}

case class DeleteResp(d: com.ksmpartners.ernie.model.DeleteStatus, e: Option[Exception]) extends ErnieResponse(e)

class DeleteJobActionBuilder(val job: Expression[Long], val as: Expression[Int] = value2Expression(15)) extends ErnieActionDefinition {

  val actionFunc = (s: Session) => (s, (for {
    j <- job(s)
    a <- as(s)
  } yield try {
    var i = 0
    var (d, e) = build.deleteReportOutput(j)
    while ((i < a) && (d != DeleteStatus.SUCCESS)) {
      val dE = build.deleteReportOutput(j)
      d = dE._1
      e = dE._2
    }
    value2Expression(DeleteResp(d, e))
  } catch {
    case e: Exception => value2Expression(DeleteResp(null, Some(e)))
  }) match {
    case Success(r: Expression[ErnieResponse]) => r
    case _ => value2Expression(DeleteResp(null, Some(new Exception)))
  })

  val validationFunc = (s: Session, er: Expression[ErnieResponse]) => er(s) match {
    case Success(e: DeleteResp) => ((e.d == com.ksmpartners.ernie.model.DeleteStatus.SUCCESS) || (e.d == com.ksmpartners.ernie.model.DeleteStatus.FAILED_IN_USE))
    case _ => false
  }

  val afterFunc = (s: Session, er: Expression[ErnieResponse]) => er(s) match {
    case Success(e: DeleteResp) => {
      var ses = s
      if (s.get[Long]("currentJob") == Some(job)) ses = ses.remove("currentJob")
      ses.get[List[Long]]("jobs").map(jobs => {
        ses = ses.set("jobs", jobs.drop(jobs.indexOf(job)))
      })
      ses
    } case _ => s
  }

  val requestName = "Delete Job"

  def successMessage(s: Session, e: Expression[ErnieResponse]) = e.apply(s) match {
    case Success(d: DeleteResp) => "Delete of job " + job + " succeeded (or job was in use)"
    case _ => "did not actually succeed"
  }

  def failMessage(s: Session, er: Expression[ErnieResponse]) = er(s) match {
    case Success(e: DeleteResp) => e.errorOpt.map(f => f.getClass + " " + f.getMessage) getOrElse ("Unknown failure")
    case _ => "Unknown failure"
  }

  def attempts(a: Int): DeleteJobActionBuilder = new DeleteJobActionBuilder(job, value2Expression(a))

  private[gatling] def build = {
    new ErnieAPI
  }

  private[gatling] def toActionBuilder = new ErnieActionBuilder(this, null)

}

class DeleteDefActionBuilder(val defId: Expression[String], val as: Expression[Int] = value2Expression(15)) extends ErnieActionDefinition {

  val actionFunc = (s: Session) => (s, (for {
    dI <- defId(s)
    a <- as(s)
  } yield try {
    var i = 0

    var (d, e) = build.deleteDefinition(dI)

    while ((i < a) && (d != DeleteStatus.SUCCESS)) {
      val dE = build.deleteDefinition(dI)
      d = dE._1
      e = dE._2
    }

    value2Expression(DeleteResp(d, e))

  } catch {
    case e: Exception => value2Expression(DeleteResp(null, Some(e)))
  }) match {
    case Success(r: Expression[DeleteResp]) => r
    case _ => value2Expression(DeleteResp(null, Some(new Exception)))
  })

  val validationFunc = (s: Session, er: Expression[ErnieResponse]) => er(s) match {
    case Success(e: DeleteResp) => ((e.d == com.ksmpartners.ernie.model.DeleteStatus.SUCCESS) || (e.d == com.ksmpartners.ernie.model.DeleteStatus.FAILED_IN_USE))
    case _ => false
  }

  val afterFunc = (s: Session, e: Expression[ErnieResponse]) => {
    s.get[List[String]]("defs").map(defs => {
      s.set("defs", defs.drop(defs.indexOf(defId)))
    }) getOrElse s
  }

  val requestName = "Delete Definition"

  def successMessage(s: Session, e: Expression[ErnieResponse]) = e.apply(s) match {
    case Success(i: DeleteResp) => "Delete of job " + (defId(s) match {
      case Success(d: String) => d
      case _ => "?"
    }) + " succeeded (or def was in use)"
    case _ => "did not actually succeed"
  }

  def failMessage(s: Session, er: Expression[ErnieResponse]) = er(s) match {
    case Success(e: DeleteResp) => e.errorOpt.map(f => f.getClass + " " + f.getMessage) getOrElse ("Unknown failure")
    case _ => "Unknown failure"
  }

  def attempts(a: Int): DeleteDefActionBuilder = new DeleteDefActionBuilder(defId, value2Expression(a))

  private[gatling] def build = {
    new ErnieAPI
  }

  private[gatling] def toActionBuilder = new ErnieActionBuilder(this, null)

}

/**
 * setup the implicit conversion here.
 */
object PostJobActionBuilder {
  implicit def toActionBuilder(requestBuilder: PostJobActionBuilder) = requestBuilder.toActionBuilder
}

object GetResultActionBuilder {
  implicit def toActionBuilder(requestBuilder: GetResultActionBuilder) = requestBuilder.toActionBuilder
}

object CreateDefActionBuilder {
  implicit def toActionBuilder(requestBuilder: CreateDefActionBuilder) = requestBuilder.toActionBuilder
}

object GetDefsActionBuilder {
  implicit def toActionBuilder(requestBuilder: GetDefsActionBuilder) = requestBuilder.toActionBuilder
}

object DeleteJobActionBuilder {
  implicit def toActionBuilder(requestBuilder: DeleteJobActionBuilder) = requestBuilder.toActionBuilder
}

object DeleteDefActionBuilder {
  implicit def toActionBuilder(requestBuilder: DeleteDefActionBuilder) = requestBuilder.toActionBuilder
}

/**
 * exposes a factory method used by the Predef stuff.
 */
object ErnieBuilder {
  /**
   * Start our main DSL chain from here.
   */
  def ernie(e: ErnieAPI) = new ErnieBuilder(e, Nil)
}

abstract class ErnieGatling(aB: List[ActionBuilder]) extends ChainBuilder(aB) {
  def postJob(defId: String): ChainBuilder
  def postJob(defId: Option[String], r: ReportType): ChainBuilder
  def getResult: ChainBuilder
  def getResult(jobId: Option[String]): ChainBuilder
  def getResult(jobId: String): ChainBuilder
  def getResult(wait: Duration): ChainBuilder
  def getResult(jobId: String, wait: Duration): ChainBuilder
  def createDef(defLoc: String): ChainBuilder
  def getDefs: ChainBuilder
  def deleteJob(job: Option[String]): ChainBuilder
  def deleteDef(defId: String): ChainBuilder
}

/**
 * This class contains specific kinds of ernie actions you can take
 */
class ErnieBuilder(val ernie: ErnieAPI, val aB: List[ActionBuilder]) extends ErnieGatling(aB) {
  import io.gatling.http.Predef._

  def postJobApi(defId: String): PostJobActionBuilder = new PostJobActionBuilder(EL.compile[String](defId))
  def postJob(defId: String): ChainBuilder = exec(postJobApi(defId))

  def postJobApi(defId: Option[String], r: ReportType): PostJobActionBuilder = new PostJobActionBuilder(EL.compile[String](defId getOrElse ""), rptType = r)
  def postJob(defId: Option[String], r: ReportType): ChainBuilder = exec(postJobApi(defId, r))

  def getResultApi: GetResultActionBuilder = new GetResultActionBuilder()
  def getResult: ChainBuilder = exec(getResultApi)

  def getResultApi(jobId: Option[String]): GetResultActionBuilder = new GetResultActionBuilder(EL.compile[Long](jobId getOrElse "-1L"))
  def getResult(jobId: Option[String]): ChainBuilder = exec(getResultApi(jobId))

  def getResultApi(jobId: String): GetResultActionBuilder = new GetResultActionBuilder(EL.compile[Long](jobId))
  def getResult(jobId: String): ChainBuilder = exec(getResultApi(jobId))

  def getResultApi(wait: Duration): GetResultActionBuilder = new GetResultActionBuilder(waitFor = wait)
  def getResult(wait: Duration): ChainBuilder = exec(getResultApi(wait))

  def getResultApi(jobId: String, wait: Duration) = new GetResultActionBuilder(EL.compile[Long](jobId), wait)
  def getResult(jobId: String, wait: Duration): ChainBuilder = exec(getResultApi(jobId, wait))

  def createDefApi(defLoc: String) = new CreateDefActionBuilder(EL.compile[String](defLoc))
  def createDef(defLoc: String): ChainBuilder = exec(createDefApi(defLoc))

  def getDefsApi = new GetDefsActionBuilder
  def getDefs: ChainBuilder = exec(getDefsApi)

  def deleteJobApi(job: Option[String]) = new DeleteJobActionBuilder(EL.compile[Long](job getOrElse "-1L"))
  def deleteJob(job: Option[String]): ChainBuilder = exec(deleteJobApi(job))

  def deleteDefApi(defId: String) = new DeleteDefActionBuilder(EL.compile[String](defId))
  def deleteDef(defId: String): ChainBuilder = exec(deleteDefApi(defId))

}

object ErnieHttp {
  def apply(wS: String, rS: String): ErnieHttp = {
    val e = new ErnieHttp(Nil)
    e.writeSaml = wS
    e.readSaml = rS
    e
  }
}

class ErnieHttp(val aB: List[ActionBuilder]) extends ErnieGatling(aB) {

  import io.gatling.core.validation._
  import io.gatling.core.Predef._
  import bootstrap._
  import scala.concurrent.duration._

  var writeSaml = ""
  var readSaml = ""

  def postJobHttp(defId: Option[String], rptType: ReportType) = http("Post job request")
    .post("/jobs")
    .header("Authorization", writeSaml)
    .header("Accept", com.ksmpartners.ernie.model.ModelObject.TYPE_FULL)
    .body(session => {
      val dId = (
        (defId.map(d => EL.compile[String](d).apply(session)).getOrElse(Failure("No")) match {
          case Success(s) => Some(s)
          case _ => None
        }) getOrElse (session.get[List[String]]("defs").map(f => if (f.size > 0) f(0)) getOrElse ("faildef"))).toString
      serialize(new com.ksmpartners.ernie.model.ReportRequest(dId, ReportType.PDF, 7, null))
    })
    .check(
      status.is(201),
      header("Location").transform(f => f.map(s => try { s.subSequence(s.lastIndexOf("/") + 1, s.length).toString.toLong } catch { case _: Throwable => })).saveAs("tempJob"))

  def postJob(defId: String): ChainBuilder = postJob(Some(defId), ReportType.PDF)
  def postJob(defId: Option[String], rptType: ReportType): ChainBuilder = exec(postJobHttp(defId, rptType))
    .exec(session => {
      session.set("currentJob", session.get[Long]("tempJob") getOrElse -1L)
        .set("jobs", (session.get[List[Long]]("jobs") getOrElse List.empty[Long]).::(session.get[Long]("tempJob") getOrElse Nil))
        .remove("tempJob")
        .set("postCount", (session.get[Int]("postCount") getOrElse 0) + 1)
    })

  def createDef(bodyLoc: String) = exec(
    http("Post def request")
      .post("/defs")
      .header("Authorization", writeSaml)
      .header("Accept", com.ksmpartners.ernie.model.ModelObject.TYPE_FULL)
      .bodyAsBytes(session => {
        val defEnt = new DefinitionEntity()
        defEnt.setCreatedUser("default")
        defEnt.setDefId("test_def2")
        serialize(defEnt)
      })
      .check(
        header("Location").transform(f => f.map(s => try { s.subSequence(s.lastIndexOf("/") + 1, s.length).toString } catch { case _: Throwable => })).saveAs("tempDef"))).exec(
      http("Put def request")
        .put("/defs/${tempDef}/rptdesign")
        .header("Authorization", writeSaml)
        .header("Accept", com.ksmpartners.ernie.model.ModelObject.TYPE_FULL)
        .header("Content-type", com.ksmpartners.ernie.model.ModelObject.TYPE_RPTDESIGN_FULL)
        .bodyAsBytes(session => {
          scala.xml.XML.loadFile(new File(bodyLoc)).toString
        })
        .check(status.is(201)))

  def getResult(job: Option[Long], wait: Option[Duration]): ChainBuilder = exec(
    http("Get result request").get("/jobs/" + job.map(j => j.toString).getOrElse("${currentJob}") + "/result").header("Authorization", readSaml).header("Accept", "application/pdf").check(status.saveAs("resultStatus")))
    .doIf(session => session.get[Int]("resultStatus").getOrElse(400) != 200) {
      pause(wait getOrElse (30 seconds))
        .exec(
          http("Get result request").get("/jobs/" + job.map(j => j.toString).getOrElse("${currentJob}") + "/result").header("Authorization", readSaml).header("Accept", "application/pdf").check(status.saveAs("resultStatus"), status.is(200)))
    }

  def getResult: ChainBuilder = getResult(None, None)
  def getResult(jobId: Option[String]): ChainBuilder = getResult(jobId.map(j => toLong(j)) getOrElse None, None)
  def getResult(jobId: String): ChainBuilder = getResult(toLong(jobId), None)
  def getResult(wait: Duration): ChainBuilder = getResult(None, Some(wait))
  def getResult(jobId: String, wait: Duration): ChainBuilder = getResult(toLong(jobId), Some(wait))

  def toLong(str: String) = try {
    Some(str.toLong)
  } catch {
    case _: Throwable => None
  }

  def getDefsHttp() = {
    import scala.collection.JavaConversions._
    http("Get defs request").get("/defs").header("Authorization", readSaml).header("Accept", com.ksmpartners.ernie.model.ModelObject.TYPE_FULL)
      .check(status.is(200),
        bodyString.transform(b => b.map(str => deserialize(str, classOf[ReportDefinitionMapResponse]).getReportDefMap.keySet.toList)).saveAs("defs"))
  }

  def getDefs() = exec(getDefsHttp)

  def deleteDefHttp(defId: String) = {
    http("Delete def request").delete("/defs/" + defId).header("Authorization", writeSaml).header("Accept", com.ksmpartners.ernie.model.ModelObject.TYPE_FULL)
      .check(status.is(200))
  }

  def deleteDef(defId: Option[String]) = exec(session => {
    val defs = session.get[List[String]]("defs") getOrElse List.empty[String]
    session.set("delDef", defId getOrElse (util.Random.shuffle(defs).headOption getOrElse ("failDef")))
  }).exec(deleteDefHttp("${delDef}"))

  def deleteDef(defId: String) = deleteDef(Some(defId))

  def deleteJob(delJob: Option[String]) = exec(session => {
    session.set("delJob", delJob.map(d => EL.compile[String](d).apply(session)) getOrElse session.get[List[Long]]("jobs").getOrElse(List.empty[Long]).headOption.getOrElse(-1L))
  }).exec(
    http("Delete job request").delete("/jobs/${delJob}/result").header("Authorization", writeSaml).header("Accept", com.ksmpartners.ernie.model.ModelObject.TYPE_FULL).check(status.is(200))).exec(session => {
      var jobs = session.get[List[Long]]("jobs").getOrElse(List.empty[Long])
      val jI = jobs.indexOf(session.get[Long]("delJob") getOrElse -1L)
      if (jI >= 0) jobs = jobs.drop(jI)
      session.set("jobs", jobs).remove("delJob")
    })

  def serialize[A <: ModelObject](obj: A): String = {
    mapper.writeValueAsString(obj)
  }

  def deserialize[A <: ModelObject](json: String, clazz: Class[A]): A = {
    mapper.readValue(json, clazz)
  }

}

object Predef {
  def ernie(e: ErnieAPI) = ErnieBuilder.ernie(e)
  def ernie(wS: String, rS: String) = ErnieHttp(wS, rS)
}

