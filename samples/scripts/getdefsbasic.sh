#!/bin/sh
echo "#########################	ERNIE-3 List report definitions	######################### "
echo "As a Client Application Developer, I want to be able to retrieve a list of all of the"
echo "report definitions known to the server."
echo ""
echo ""
wget --header "Accept: application/vnd.ksmpartners.ernie+json" --http-user=adamc --http-password=pass http://localhost:8080/defs
