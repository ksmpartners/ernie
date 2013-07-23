![alt text](https://github.com/ernie-reporting/raw/master/logo-small.png "Ernie Logo")

Ernie Report Generator
============================================
Ernie is a [high performance](#4) wrapper for the [Eclipse BIRT Report Engine](http://www.eclipse.org/birt/phoenix/). Developers can integrate Ernie using an embedded [Java or Scala API](#2), or deploy a [servlet](#3) that exposes Ernie's features as a RESTful web service.

All reports generated using Ernie are based on report definitions created using the [Eclipse BIRT Designer](http://www.eclipse.org/birt/phoenix/intro/intro03.php). 

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

Then, find API documentation in:

* ernie-api/target/site/scaladocs

* ernie-engine/target/site/scaladocs

* ernie-gatling/target/site/scaladocs

* ernie-server/target/site/scaladocs

* ernie-model/target/site/apidocs

* ernie-util/target/site/scaladocs

* ernie-java-api/target/site/apidocs

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

Ernie server <a id="3"></a>
-------------------------------

To build a WAR using SBT:

