#!/bin/sh
echo "#########################		ERNIE-72 Delete job service returns 403 response code for unauthorized users	######################### "
echo "As a Client Application Developer, I want to be able to delete all report output from the server for "
echo "a specific job on demand."
echo ""
echo "An unauthorized user receives an HTTP 403 response code (Forbidden) when attempting access over HTTP/S "
echo "To be authorized to use this service, a user must be in the write role."
echo ""
echo ""
curl -v -X DELETE --header "Accept: application/vnd.ksmpartners.ernie+json" --header "Authorization: SAML tVbZkqpIEH33KwzvY4fN4m60RhRQ7qAgovgywVICyqIUCPr1g9Aabd/uOz0TMU9EJpmnTp6sJd+w\
5rl0F2CMwsgJ/HLquT7u5t5eJQ79bqBhB3d9zUO4GxndJeBnXfqV7Gr3lMp7Top7FTuKjl2CSJLk\
Nam9BqFF0CRJERt+tjRs5GmV8pjrVRzzL6S3Wzt6V6uaNYSq9RalV3UdNatUjUIU3dBQW7sFYxyj\
sY8jzY96FZqkalWyUaVaMkl36Va33nylaq1tpaygEGdMspBXstJ/K2rKk8N+8XkjnpxvJu4uHcvX\
ojhE7/zN7/iTBNkhshgTO9avyiMXmWN/F+Qmq/mB7xia61y1myQ8iuzALAPXCkInsr1vhaHIG3AV\
pUbVoOr+rwrxTO2HQE8MQ6xVsa1R71gS2qEQ+QYqr6Rxr/Lrp+LnyXKo+XgXhB5+Nv8dI+SfkRsc\
kVnF98Leyf0c8Aut+m/I6I59w42xc0bCbYceNQPh934i4+dQ5UWIdk46c3C2z1J8Y0d8pPfJLNTg\
HAvh6L+06EN7ChBFc2PUH47oBPvmC16maqvNXzw30FWN1KfjpJcT+BicOx7NLcxP2/KxjYqMgWuf\
oIV9qK7D/YbFimqchs1ZWkeYOs4F0Q4l0305bYNNg7rsTrRHvtCniXgctGrNuq52wHxkRaPDRFbH\
x9Tp6JLf2LvBeQXbgjScT2VS35w3W/3MyoqOt/7aTBICwiVgTuIunDJ0HOtLxUGkF+73fk0bSYaw\
0Eb60VVkPIgEGKz3CX9Or/px3HuU84H/raQpujzK2zTIDqdF2sNgbxfSLjuHEerz4zErciwLtAPL\
QCQwJE44UZ1Mg+3YPhsCECFkRJBsr3DGg8MQUCvI2DyrKHwKr0BiLEFhgCWzB8Eu6UPX02uTWFvD\
dMCBZfHTkFlKsHVfssdQwOpmYvNikrCWyimiOIWJIJvrego5MC/iscyQnaS0XTfkFdlOORnMih+8\
zIwk16iJycg2BJ6zEl6GFC8f0rnMN9Y3n5z7Lg/fnvF5ESclVsxXG8JkoqyuUOYZmJfCMvxUpAdY\
W2/Phtc4qjJc8cy4KDPlZyt6EJtDaIl0ahseTDkOTEsFm0AGlEDpeyjyoF6ApfxIrjGu7gqyfIUC\
D3DuBykPzaFyNTmW8a9AYKzDyT44w05CMqVM4AHISt9nIlvqIZMFgnApGsSq1RgyNIimtj5cNo6O\
Mzt4k+sJ4mOzHnKpzk9eUi68IBIKxIlaNea7l5J04LAmEI51AGeFMuINtORZU3K13WBhrDYtMeXs\
LWNjddA56knEX506qOO4Y/vp2F5JSB9CL52uO6eJoJYmmFSCxsvY2qcxXPt731eskYypF+/UEFUZ\
tGBze5m1WZBAAOTnsoAo5lVZIlNbJMSqJF4XU3khq04nPuqDjRuCKZ9Egx09X9cv5+F6fXaNxBCW\
24knT9pAOivqnIeKoGO5Ph8Yu5gmFkgNgOlwoGQML8SA3sb79mYyOcaitITNRSSemJiDo/3em4sT\
MlSIsO3NqS0bjST7uG1cVWlvS2wHsfo+7SjhQEvJCceWiMEuWTuWF65JlRR7xZH6fEwezuIgER+P\
2NMRvL+qy1jfIyO6m7dbd8yVB9mlqEXfDwzUK5V7HLO6y0PzNd+xbhiV/rtxf6gL4P7d/LQsG/im\
c3tpcVkIIgZloOhPA0IWNPfnIdhFKMzj6l/GEXd8EEe2v4wyhTzkR+Xc/OMcUmvS28pTdkYxQmn0\
lY91s+Epu7r7f5yvjK5xi0NFf+5CfInz1c9n56OWB58oCh09jtD3f8q3HvQqKPQdJAUuqvwWkd/L\
j/HP+ef5r+rkIhooGxqx040uR3R7b7s4A/StSj9Emvng/bRK/zf3d3zjbC4tttT/Tve21M/pEt8r\
T3waw/t/Aw==" http://localhost:8080/jobs/$1/result

