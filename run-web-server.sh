#!/bin/sh

mvn jetty:run -f ernie-server/pom.xml \
  -Dernie.props="./ernie-server/src/main/resources/props/default.props" \
  -Dkeystore.location="./ernie-server/src/test/resources/keystore.jks" \
  -Dauthentication.mode="none"
