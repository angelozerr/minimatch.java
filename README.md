# minimatch.java

[![Build Status](https://secure.travis-ci.org/angelozerr/minimatch.java.png)](http://travis-ci.org/angelozerr/minimatch.java)

Port of Node.js' [minimatch](https://github.com/isaacs/minimatch) to Java.

# Usage

```java
import minimatch.Minimatch;
...

boolean result = Minimatch.minimatch("bar.foo", "*.foo"); // return true

boolean result = Minimatch.minimatch("js/test.js", "**/**.js"); // return true

boolean result = Minimatch.minimatch("js/test.html", "**/**.js"); // return false
```
# Build

See cloudbees job: https://opensagres.ci.cloudbees.com/job/minimatch/

# Structure

The basic structure of the project is given in the following way:

* `src/main/java/` Java sources of minimatch.java. 
* `src/test/java/` JUnit tests of minimatch.java.
* `html/` html samples which use JavaScript minimatch. 
