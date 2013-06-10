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
curl -v -X POST -d '{"defId":"'$1'","rptType":"'$rptType'","retentionDays":'$3',"reportParameters":null}' -H "Content-type: application/json" --header "Accept: application/vnd.ksmpartners.ernie+json" --header "Authorization: SAML tVdbc7JMEr73V1i+lynDQVGxolXDyYCCQRGjN1scRkA5yQxC/PWLmFgxm7yb3arviuqe7qef7p5D\
84SsKKSHACGY4SCJm2UUxmhYa0etPIuHiYUCNIytCKIhdoZLoM6G9CM5tD5cWu8+JRq1fIzTIUEU\
RfFYdB6TzCNokqSIV3W2dHwYWa2mLIxagfsv1iUHDNPvtPuW2213O123zbIU2+6xvV5nZ7GVQFbG\
COVQjhG2Yjxq0STVaZPdNt0zqMGQYodk95Fm2W2racIMVUwqk0eyNX665lQ7Z+Pr54m4Uz65aLgM\
vNjCeQbf+bs/8ScJkiUqGxcF3p/WzRe6crxLapG34iQOHCsMztalJCrEfuI2QeglWYD96MfCUOQF\
uA1Lp+1Q3fhPi7in9kugO4YZstrIt6h3rAXcwQzGDmyuFvKo9ee3xa+djcyK0S7JInQv/m+MYHyC\
YZJCt40+Ensn93vAb2o1foLOUI6dMEfBCWqXHZpaDkTv/YTO76GaLxncBeUsQNU+K9GFHfGZ3hfx\
Wg0h8CDC/0+LPrXnCmJaYQ7HeqQQK01k+sflXp5I8lo9rWeRQ6R7dVQT+GxcK27NvYpftuVtG109\
DFd+zumHVX8e03Y/xmj5JgkDb1s+M16Eg408BTR18LczmzsyplJYS2nrbRwpWnjeeffSpXkTzeNO\
us9WUtmXHHWTc5MH0+zbqbVUzOXk0MuVc3rOOFch85iWlxTT8zbUCkMiLfRtsDquJ8V8txPcE1mk\
glVMgzXDePNgKiuhe5R6hZsO6Ohs6qNbOp/4X1Kawrdbeq8MyQoWtm4Cf7mQdtU5xHCsyjKvCzwP\
rAPPiVDjSFQI+kaZJlvZPzka0EWR00GxPYszFRwmgFqJnK/ypqmW4hksOE8zOeAZ/EHzG/YkjOyO\
kltrsZQEsLwuOgZPab4dL3xZ1NDmVfFVvSh4byOYuj4VC81w191SFMD8ao8MjmSLxnbNGCtyUAoG\
mF0XVIN7XoRORy+efUdTBa9QDZFSjUM5N1RmfdEZte7tpttzsaqjosHrdbSJWCjm6iwaKifWqfCc\
OtVpCVnr7cmJmHRjiCuVk69plupsRUu5OxE9nS59JxJLQQDTxpVNYgBKo+y9qKugewUr1Wejw4V2\
qBnGWdRUgGo9KFXRnZhnV+C5+Aw0zjsc/UMwYQuSa1QFlkCV+r4qsrc5VGURQbbUHWLVZyYcDfDU\
tydLJg2C2SFSzkcRpb1uJpS2qjyUQvYGSVEjjtSKme8eGouDgCyNCLwDOJmUk7+KnjHrLUJrJ704\
q9e+Xgr+lvPRRmJTu8DqOeiCLspZPy5lf7WA9kSMyumaPSrapqEg0kyYB9nbl7m4jvdxbHrPBqIe\
oiOjbwzQF3vbt9mAB4UIgHGfFtD1OitP5zovBbFq6OeXqfFibAI2T23pNczAVC2wtKPn6+7babJe\
n0KncLTlVokMZQAWJ3MzV0VTs5HRnUvOLqeJF7hJgBsIoOFM3giJ3ub7wauipLm+WIq9F6wfuVwQ\
n/f7aK4rZGYS2SCaU1sePy/8dMucN4u9v+BZyNv7kjUzySpJReAbhLQr1oEXZWtyQ+qj65H6ekxu\
yutBIj4fsbsj+PGqLnN7Dx38IV5uXVloStWlaOGfBwbqkao1gdve1aZ1zHesC0Zr/C58PNRX4PGH\
+CUsn8RucHlpUVNLMAcrUPi3AaEymsfzDOwwzGq77rd2xAc+yLEfL3FVoQjGuFmLf51DGIrctu68\
K4oYlvg7HR9Ww1N1dY//Ol85Q+diB6/9+SjEtzjfLd4rb7nc+GCcBXaO4c8rzUsPRi2YxQFcJCFs\
/YdFfS/fxr/gv89/7aAuogOroREFQ/yWwst7O0QVYOy1xhm03Bvvuyj/fOyier7hT8G/qn8qVl4N\
xdf9/I/z3SMnwfj3hImfG098+QsY/xs=" http://localhost:8080/jobs/
