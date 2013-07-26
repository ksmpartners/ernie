  GNU nano 2.2.6                                                              File: getjob.sh                                                                                                                                  

#!/bin/sh
echo "#######################  ERNIE-59 Check job with no credentials   ######################### "
echo "As a Client Application Developer, I want to be able to interrogate the status of a job: one"
echo "of (pending, in progress, complete, failed). If an access is attempted without credentials"
echo "it should notify the user that the access was unauthorized (HTTP 401)."
echo ""
echo ""
wget --header "Accept: application/vnd.ksmpartners.ernie+json" http://localhost:8080/jobs/$1/status

