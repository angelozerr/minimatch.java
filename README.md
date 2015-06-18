# minimatch.java

[![Build Status](https://secure.travis-ci.org/angelozerr/minimatch.java.png)](http://travis-ci.org/angelozerr/minimatch.java)

Port of Node.js' [minimatch](https://github.com/isaacs/minimatch) to Java.

# Usage

```java
import minimatch.Minimatch;
...

boolean result = Minimatch.minimatch("bar.foo", "*.foo", options); // return true

boolean result = Minimatch.minimatch("js/test.js", "**/**.js", options); // return true

boolean result = Minimatch.minimatch("js/test.html", "**/**.js", options); // return false
```

# Structure

The basic structure of the project is given in the following way:

* `src/main/java/` Java sources of minimatch.java. 
* `src/test/java/` JUnit tests of minimatch.java.
* `html/` html samples which use JavaScript minimatch. 