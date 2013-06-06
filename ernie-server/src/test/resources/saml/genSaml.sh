
#!/bin/sh

java -cp lib:$1 \
  com.ksmpartners.commons.security.SAML2.SAMLCreator -a test -expires 365 \
  -roles "read" \
  -rolesAttr ernieRole -uid readUser -uidAttr userName \
  -k $2 \
  -kpwd changeit -i Issuer -s Subject -file read.xml

java -cp lib:$1 \
  com.ksmpartners.commons.security.SAML2.SAMLCreator -a test -expires 365 \
  -roles "write" \
  -rolesAttr ernieRole -uid writeUser -uidAttr userName \
  -k $2 \
  -kpwd changeit -i Issuer -s Subject -file write.xml

java -cp lib:$1 \
  com.ksmpartners.commons.security.SAML2.SAMLCreator -a test -expires 365 \
  -roles "run" \
  -rolesAttr ernieRole -uid runUser -uidAttr userName \
  -k $2 \
  -kpwd changeit -i Issuer -s Subject -file run.xml

java -cp lib:$1 \
  com.ksmpartners.commons.security.SAML2.SAMLCreator -a test -expires 365 \
  -roles "read|write" \
  -rolesAttr ernieRole -uid readWriteUser -uidAttr userName \
  -k $2 \
  -kpwd changeit -i Issuer -s Subject -file read-write.xml

java -cp lib:$1 \
  com.ksmpartners.commons.security.SAML2.SAMLCreator -a test -expires 365 \
  -roles "read|run" \
  -rolesAttr ernieRole -uid readRunUser -uidAttr userName \
  -k $2 \
  -kpwd changeit -i Issuer -s Subject -file read-run.xml

java -cp lib:$1 \
  com.ksmpartners.commons.security.SAML2.SAMLCreator -a test -expires 365 \
  -roles "read|write|run" \
  -rolesAttr ernieRole -uid readWriteRunUser -uidAttr userName \
  -k $2 \
  -kpwd changeit -i Issuer -s Subject -file read-write-run.xml
