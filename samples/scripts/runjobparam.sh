#!/bin/sh
echo "#########################	ERNIE-4 Run reports asynchronously	######################### "
echo "As a Client Application Developer, I want to be able to run a report asynchronously, passing"
echo "in the values of required parameters, if any. A request to run a report will create a \"job\""
echo ""
echo "                    	+ERNIE-8 Specify output retention at job creation	"
echo "As a Client Application Developer, I want to be able to specify an optional, default amount of"
echo "time that the report output should be retained for future download when requesting that the"
echo "report be run."
echo ""
echo ""
rptType=$(echo $2 | tr '[:lower:]' '[:upper:]') 
curl -v -X POST -d '{"defId":"'$1'","rptType":"'$rptType'","retentionDays":'$3',"reportParameters":{"MinQuantityInStock":"477"}}' -H "Content-type: application/json" --header "Accept: application/vnd.ksmpartners.ernie+json" --header "Authorization: SAML vVZZk6JIEH73VxjOo2FzeLVGa0QBhYqCoojHywZHCSiHUiDor1+E1m17umdm92GfiEwyv/ryqso3\
rHku3QUYozByAr+ceq6Pu7m2V4lDvxto2MFdX/MQ7kZGdwHESZd+Ibva3aXy7pPiXsWOomOXIJIk\
eUnqL0FoETRJUsRanCwMG3lapTziehXH/KujI71ZN+kaSdGdWsMg2zW90zFqbZPa1Q1EafX2zRjj\
GI18HGl+1KvQJFWvka0a2Vaodpcku1TjpU42tpWyikKcMclMXshK/62IKXcO+8XnjXhSvpm4u3As\
X4viEL3zN7/jTxJkh8hsTOxYPyoPX2SO/F2Qi6zmB75jaK5z1W4pEVFkB2YZuFYQOpHtfZsYirwB\
11Bq1Ayq4f+oEM/U/hDoiWGItRq2Neoda452KES+gcrL+ahX+fGnyc+dlVDz8S4IPfws/jtGyD8j\
Nzgis4bvgb2T+3PAL3LVf0NGd+QbboydM5JuHXrUDITf64mMP4cqz0K0c9KJg7M+S/GNHfGR3iex\
yAbnWAhH/6VEH8pTgKiaG6N+Sz44B9zgq8t4XV0fXLZtI3pgzFD90MsJfDTOFY/iFuKntny0UeHh\
Y44PxsMxkfLemXZXSnO44pdDklaVtTGar4R0qCft8cE+iXHH31+OEoPQcZEOTkEzOojHzSsU206i\
j/aepV6HanPMtegzlCfimpNmwnSWEK9ieCXJalOWWhOHo6qyttfJlqVJOIBwbaqhptieCSTPm1La\
nJKuCDWtgTez6bC9WzjVK3GWT5sw6D3C+cD/FtIYXR7hrZtkh9Mi7SGwtwtpl81hhPriaMTKHMsC\
7cAyEEkMiRNO3gjjYDuyz4YEZAgZGSTbK5yI4DAA1BIytsiqqpjCK5gzlqQywFLYg2SX9IHr6XUh\
1lYw5TmwKH4aCktJtu7P7RGU8GYt2KKcJKy14VRZHsNEUsxVI4UcmBb2WGHITlLarprKknxNOQVM\
ih+iwgznrlGXk6FtSCJnJaICKVE5pFNFbK5uOiXXXR66PeOLMk5KrJyfNoCJoC6vUBEZmIfCMuJY\
pnmsrbZnw2seNwpcisyoCDMVJ0uaj80BtGQ6tQ0PphwHxqWCTaAASqL0PZRF0CjAUnGo1BlXdyVF\
uUJJBDjXg1SE5kC9mhzL+FcgMdbhZB+cQSchmVKWYB5koe+zJFubQ5YWCMKFbBDLdnPA0CAa2/pg\
0Tw6zuTgCdcTxMdWI+RSXRSqKRdeEAkl4kQtm9NdtTQ/cFiTCMc6gLNKGfEaWsqkNXe1HT8zluu2\
nHL2lrHxhu8c9SQSr04DNHDcsf10ZC/nSB9ALx2vOidB2pQETKpBszqy9mkMV/7e91VrqGCq6p2a\
8kYBbdjaXiavLEggAMpzWECW86gsmaln/b4sydfZWJkpG6cTH3V+7YZgLCYRv6Onq8blPFitzq6R\
GNJiK3iK8ArmZ3UzFaEq6VhpTHljF9PEDG0CYDocKBmDC8HT23j/uhaEYyzPF7A1i+QTE3NwuN97\
U1kgQ5UIX7Px2bLRcG4ft83rZr6352wHsfo+7aghr6WkwLElgt8lK8fywhW5IeVeMVKfx+ShLAaJ\
+DhiTyN4f1UXsb5HRnQXb7fuiCvz2aWoRd8vDNQLlWscs7bLTfMz37FuGJX+u3B/qAvg/l38dCwb\
+KZze2lxWQoiBmWg6FcLQmY09ach2EUozO0aX9oRd3wQR7a/iLIMeciPyrn4yz2k0WlvK0/eGcUI\
pdFXOtbNlqfs6u7/cr8yusbNDhX1uSfiS5yvfj4rH7E8+ERR6OhxhL7/U77VoFdBoe+geeCiyk8W\
+b38WP+c3+9/NSdPooGypRE73ehyRLf3toszQN+q9MPYf9B+OqT/k/o7unG2lhYd9X+wXeJ/dsvf\
Mya+zz3xaRHv/w0=" http://localhost:8080/jobs
