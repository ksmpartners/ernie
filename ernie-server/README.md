Ernie server
=================================
This package is a Java servlet that uses [ernie-api](../ernie-api) and the [Lift web framework](http://liftweb.net/) to expose all of Ernie's features as a RESTful web service.

The following README will provide an overview of how to configure, test, deploy, and interact with ernie-server.

Configuration
------------------------
The configuration for ernie-server is specified through Java runtime parameters. For instance, running the servlet in a Jetty container through Maven:

```
  mvn jetty:run -f ernie-server/pom.xml \
    -Dernie.props="./ernie-server/src/main/resources/props/default.props"

```

The ernie.props argument specifies the location of the properties file to use. Valid property names are as follows:

- __rpt.def.dir__
  _The directory in which definitions and their metadata are stored. Required. Must be different from output.dir and jobs.dir._
- __output.dir__
  _The directory in which report output and metadata is stored. Required. Must be different from rpt.def.dir and jobs.dir._
- __jobs.dir__
  _The directory in which job metadata is stored. Required. Must be different from rpt.def.dir and output.dir._
- __worker.count__
  _An Ernie Worker is an Akka Actor that has the ability to execute report generation requests (i.e. alongside other workers). Therefore, the number specified here will designate the maximum number of concurrent report generation tasks._
- __swagger.docs__
  _Either true or false. Determines whether Swagger UI is enabled._
- __retention.period.default__
  _If an incoming report request does not specify a number of days after which report output should expire, this value will be used. Note that expired report output remains available until purgeExpiredReports() is called._
- __retention.period.maximum__
  _The maximum allowed number of days for report retention._

Authentication and authorization
--------------------------
Ernie supports HTTP Basic, SAML, or no authentication/authorization. 

__SAML__

Use the keystore.location and authentication.mode system properties. Again, an example with Maven/Jetty:

```
  mvn jetty:run -f ernie-server/pom.xml \
    -Dernie.props="./ernie-server/src/main/resources/props/default.props" \
    -Dkeystore.location="./ernie-server/src/test/resources/keystore.jks" \
    -Dauthentication.mode="SAML"
```

TODO: Elaborate on required SAML tokens.

__Basic__

You will need to modify the ernie-server source code. Modify the variable ``` DispatchRestAPI.basicAuthentication ``` to produce roles given a username and password.

The basicAuthentication variable contains a PartialFunction[(String, String, Req), Boolean]. It should:

- Attempt to authenticate the user
- If successful, populate userRoles RequestVar with roles (see [[com.ksmpartners.ernie.server.filter.SAMLConstants.]]).
- Return the result of authentication as a Boolean

For example:

```scala

 basicAuthentication = ({
       case (user:String, pass:String, req:Req) =>
         MyUserCollection.getUser(user, pass).map( u => {
           userRoles(u.getRoles)
           true
         }) getOrElse false
     })

```

Then, specify the authentication.mode system property:

```
  mvn jetty:run -f ernie-server/pom.xml \
    -Dernie.props="./ernie-server/src/main/resources/props/default.props" \
    -Dauthentication.mode="BASIC"
```





