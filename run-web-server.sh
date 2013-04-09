#!/bin/sh

mvn jetty:run -f ernie-server/pom.xml \
  -Dernie.props="./ernie-server/src/main/resources/props/default.props"
