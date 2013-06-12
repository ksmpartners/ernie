rm *.json
./getapi.sh jobs.json
./getapi.sh defs.json
./getapi.sh api.json
mv api.json resources.json
./bin/php-ernie-api.sh
./bin/java-ernie-api.sh
./bin/scala-ernie-api.sh
./bin/ernie-docs.sh
