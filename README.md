![alt text](https://github.com/ernie-reporting/raw/master/logo-small.png "Ernie Logo")

Ernie Report Generator
============================================
Ernie is a [high performance](#4) wrapper for the [Eclipse BIRT Report Engine](http://www.eclipse.org/birt/phoenix/). Developers can integrate Ernie using an embedded [Java or Scala API](#2), or deploy a [servlet](#3) that exposes Ernie's features as a RESTful web service.

Ernie is designed to be used in conjunction with the [Eclipse BIRT Designer](http://www.eclipse.org/birt/phoenix/intro/intro03.php). All reports generated using Ernie are based on report definitions created using the designer. 

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

Embedded API <a id="2"></a>
---------------
Ernie-api and ernie-java-api are, respectively, Scala and Java interfaces for Ernie-engine. Here are some instructions for integrating Ernie for various common environments:

*

Ernie server <a id="3"></a>
-------------------------------

To build a WAR using SBT:

