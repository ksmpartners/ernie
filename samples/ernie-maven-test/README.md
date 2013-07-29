Place a rptdesign in this directory.

Ernie should be installed to your local maven repository.

To run the test:

```
   $ mvn scala:console
   scala> test.ErnieTest.cj("my_def.rptdesign", Map.empty[String, String])

```