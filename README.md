![alt text](https://github.com/ernie-reporting/raw/master/logo-small.png "Ernie Logo")

Ernie Report Generator
============================================
Ernie is a [high performance](#4) wrapper for the [Eclipse BIRT Report Engine](http://www.eclipse.org/birt/phoenix/). Developers can integrate Ernie using an embedded [Java or Scala API](#2), or deploy a [servlet](#3) that exposes Ernie's features as a RESTful web service.

All reports generated using Ernie use a report definition created using by the [Eclipse BIRT Designer](http://www.eclipse.org/birt/phoenix/intro/intro03.php). 

Ernie provides endpoints for:

*	 managing these report definitions and their metadata

*	 creating a report generation job based on a definition

*	 interrogating the status and metadata of a job

*	 managing job output

*	 retrieving lists and catalogs of report generation jobs and definitions

*	specifying a date for output expiration and purging expired output

Prerequisites and setup <a id="1"></a>
----------------------
__Ernie requires the following software to be installed:__

1. [Apache Maven 3](http://maven.apache.org/) or [SBT 0.12.4 or better](http://www.scala-sbt.org/)
1. Java 1.6
1. Scala 2.10.1

__To build Ernie with Maven:__

* Without dependencies: 
``` mvn clean install ```
* With dependencies: 
``` mvn clean compile assembly:single ```
* Building a deployable WAR of ernie-server

        cd ernie-server
        mvn clean install

__To build Ernie with SBT:__

* Without dependencies: 
``` sbt compile ```
* With dependencies:
``` sbt assembly ```
* Building a deployable WAR of ernie-server

        clean
        project ernie-server
        package

__To generate ScalaDocs and JavaDocs for all Ernie projects:__

``` mvn scala:doc javadoc:javadoc ```

Then, find API documentation in /target/site/scaladocs for the Scala projects (ernie-api, ernie-engine, ernie-server, ernie-gatling, ernie-util) and /target/site/apidocs for the Java projects (ernie-model, ernie-java-api).

Eclipse BIRT
----------------------
It is highly recommended that users of Ernie build report definitions with the Eclipse BIRT Designer. You can get a standalone binary [here](http://www.eclipse.org/downloads/download.php?file=/birt/downloads/drops/R-R1-4_3_0-201306131152/birt-rcp-report-designer-4_3_0.zip). The BIRT designer will allow you to connect to any JDBC data source and build rich

Embedded API <a id="2"></a>
---------------
Ernie-api and ernie-java-api are, respectively, Scala and Java interfaces for Ernie-engine. If you are integrating these libraries as standalone jars (with dependencies), please note that they do not include the __BIRT Report Runtime jar which is required to be in your classpath__. You can download the BIRT runtime [here](http://download.eclipse.org/birt/downloads/) and find the requisite jar in ReportEngine/lib/org.eclipse.birt.runtime....jar.

The embedded APIs provide a builder pattern for configuring Ernie and an engine object that takes a built configuration and produces an interface for interacting with Ernie. For example, using ernie-api:

```scala
val engine = ErnieEngine(api.ErnieBuilder()
	.withMemoryReportManager()
	.withDefaultRetentionDays(7)
	.withMaxRetentionDays(14)
	.withWorkers(100)
.build())
val ernie = e.start
ernie.getDefinitionList
```

Or, using ernie-java-api:

```java
ErnieController ernie = new ErnieController();
ernie.configure(
	new ErnieConfig.Builder(
		new com.ksmpartners.ernie.api.MemoryReportManager()
	)
	.withWorkers(10)
	.build()
);
try {
	ernie.start();
	ernie.getDefinitionList();
```
	

For more information on the embedded API configuration and usage, see the [ernie-api](ernie-api) and [ernie-java-api](ernie-java-api) documentation.

Ernie server <a id="3"></a>
-------------------------------

The ernie-server package exposes a RESTful interface to Ernie using the Lift web framework. 

__Features__

* SAML and Basic authentication with simple, role-based authorization

* Swagger JSON specification and Swagger UI

* d

* 


Unit test suite  <a id="4"></a>
--------------------------

Gatling performance testing <a id="5"></a>
---------------------------



