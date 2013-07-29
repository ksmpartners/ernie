Ernie Embedded API (Scala)
=======================================
The embedded API can be mixed into a project by including a fat jar and the BIRT runtime in the classpath (see the [setup instructions](../#prerequisites-and-setup-) and [sample code](../samples/ernie-test)) or by specifying it as a Maven dependency:

```
        <dependency>
            <groupId>com.ksmpartners.ernie</groupId>
            <artifactId>ernie-api</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
```

Basic Usage
-------------------------------
The embedded API consists of four main elements:

- ErnieBuilder
- ErnieConfiguration
- ErnieEngine
- __ErnieControl__

An instance of ErnieControl is returned when a configured ErnieEngine is started. Use ErnieBuilder to construct an ErnieConfiguration:

```scala
  val ernieConfig:ErnieConfiguration = ErnieBuilder() withMemoryReportManager() build
```

A configured ErnieEngine by passing an ErnieConfiguration to the ErnieEngine apply function:

```scala
  val engine:ErnieEngine = ErnieEngine(ernieConfig)
```

Finally, retrieve the API:

```scala
  val ernie = engine.start
```

An engine may only be started once, and it is recommended that one ErnieEngine/ErnieControl serve an entire JVM. Therefore, standard practice is to hold a single ErnieControl in a singleton.

To run a report generation job, Ernie requires a report definition. The createDefinition method optionally takes an InputStream containing the report design, and requires a description and username identifying the definition.

```scala
  //Given a rptdesign file called "my_report.rptdesign"
  val designBytes = scala.xml.XML.loadFile("my_report.rptdesign").toString.getBytes
  val myDefEnt = ernie.createDefinition(Some(new ByteArrayInputStream(designBytes)), "test description", "testUser")
```

Pass the created definition's ID to the createJob function to initiate the job:

```scala
  val (jobId, jobStatus) = ernie.createJob(
    myDefEnt.getDefId, 
    com.ksmpartners.ernie.model.ReportType.PDF,
    None,
    Map.empty[String, String],
    "testUser")
```

The createJob function is defined as follows:

```scala

  /**
   * Create and start a report generation job.
   * @param defId an existing report definition/design
   * @param rptType the report output format
   * @param retentionPeriod optional override for default number of days to retain report output
   * @param reportParameters a set of BIRT Report Parameters corresponding to the parameters specified in the report definition.
   * @param userName username of the user creating the job
   * @throws AskTimeoutException if request times out
   * @return the generated job ID and a [[com.ksmpartners.ernie.model.JobStatus]]
   */
  def createJob(defId: String, rptType: model.ReportType, retentionPeriod: Option[Int], reportParameters: immutable.Map[String, String], userName: String): (Long, model.JobStatus)

```

Given a jobId, we can poll for completion:

```scala
  while (ernie.getJobStatus(jobId) != com.ksmpartners.ernie.model.JobStatus.COMPLETE) {
    thumbs.twiddle()
  }
```

And finally, retrieve the report output:

```scala
  val output:Option[InputStream] = ernie.getReportOutput(jobId) // If job is in progress or incomplete, returns None
```

Configuration
--------------------------------
- __ReportManager__: The ErnieBuilder requires either ``` withFileReportManager(...) ``` or ``` withMemoryReportManager() ``` to be called before build().
  The withMemoryReportManager configuration takes no parameters, but will not persist anything after the application exits. The withFileReportManager configuration requires that you specify three distinct directories, in which job metdata, definitions, and output will be respectively persisted:
  
```scala
   def withFileReportManager(jobDir: String, defDir: String, outputDir: String)
```
  
- __Workers__ (Optional): An Ernie Worker is an Akka Actor that has the ability to execute report generation requests (i.e. alongside other workers). Therefore, the number specified here will designate the _maximum number of concurrent report generation tasks_.

- __Timeout__ (Optional): The timeoutAfter function takes a FiniteDuration. Importing scala.concurrent.duration._ will allow this setting to be specified using Scala's duration DSL:

```scala
  import scala.concurrent.duration._
  import scala.util.Random._
  val listOfValidDurations:List[FiniteDuration] = List(5 minutes, 10 seconds, 2 hours, 35 milliseconds)
  val config = ErnieBuilder() 
    withMemoryReportManager() 
    timeoutAfter(shuffle(listOfValidDurations).head) 
    build()
```

- __Default retention days__ (Optional): If an incoming report request does not specify a number of days after which report output should expire, this value will be used. Note that expired report output remains available until purgeExpiredReports() is called.
- __Maximum retention days__ (Optional): The maximum allowed number of days for report retention.



