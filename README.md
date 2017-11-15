# haystack-log4j-metrics-appender
A log4j appender that sends an error count to a graphite endpoint

## Overview
To facilitate counting errors and alarming when they occur, this package contains a
[Log4j](https://en.wikipedia.org/wiki/Log4j) 2
[Appender](https://logging.apache.org/log4j/2.x/log4j-core/apidocs/org/apache/logging/log4j/core/Appender.html)
that sends a [Graphite](https://graphiteapp.org/)
[plaintext protocol](http://graphite.readthedocs.io/en/latest/feeding-carbon.html#the-plaintext-protocol)
message to a Graphite endpoint. The intent of this Appender is to make it easier to alarm on error messages,
using the [Grafana](https://grafana.com/) [alert notifications](http://docs.grafana.org/alerting/notifications/),
or a similar metrics-based mechanism.

### Metric Message Format
Graphite plain text protocol messages are of the form `<name> <value> <timestamp>` where `<name>` is typically a
period-delimited String, `<value>` is a number (either integer or floating point) and `<timestamp>` is a Linux epoch
style number (milliseconds since January 1, 1970, midnight GMT). The format of the `<name>` created by
[EmitToGraphiteLog4jAppender](https://github.com/ExpediaDotCom/haystack-log4j-metrics-appender/blob/master/src/main/java/com/expedia/www/haystack/metrics/appenders/log4j/EmitToGraphiteLog4jAppender.java)
is used in the data.influxdb.templates array [here](https://github.com/ExpediaDotCom/haystack/blob/master/deployment/k8s/addons/1.6/monitoring/influxdb.yaml#L91)
and is of the following format:

```haystack.errors.<fully-qualified-class-name>.<server>.<lineNumber>.<ERROR_TYPE>_<suffix>```

where 
* `<fully-qualified-class-name>` is something like `com-foo-MyClass` (`com.foo` is the package, `MyClass` is the name
of the class, and all the periods have been replaced by hyphens).
* `<server>` is the name of the server where the error occurred.
* `<lineNumber>` is the line number in MyClass.java or MyClass.scala where the call to log the error was made.
* `ERROR_TYPE` is either `ERROR` or `FATAL`.
* `<suffix>` is TODO list the metric suffixes added by Servo.

### Releases
1. Decide what kind of version bump is necessary, based on [Semantic Versioning](http://semver.org/) conventions.
In the items below, the version number you select will be referred to as `x.y.z`.
2. Update the [pom.xml](https://github.com/ExpediaDotCom/haystack-log4j-metrics-appender/blob/master/pom.xml), 
changing the version element to `<version>x.y.z-SNAPSHOT</version>`. Note the `-SNAPSHOT` suffix.
3. Make your code changes, including unit tests. This package requires 100% unit test code coverage for the build to 
succeed.
4. Update the
[ReleaseNotes.md]((https://github.com/ExpediaDotCom/haystack-log4j-metrics-appender/blob/master/ReleaseNotes.md))
file with details of your changes.
5. Create a pull request with your changes.
6. Ask for a review of the pull request; when it is approved, the Travis CI build will upload the resulting jar file
to the [SonaType Staging Repository](https://oss.sonatype.org/#stagingRepositories).
7. Tag the build with the version number: from a command line, executed in the root directory of the project:
```
git tag x.y.z
git push --tags
```
This will cause the jar file to be released to the 
[SonaType Release Repository](https://oss.sonatype.org/#nexus-search;quick~haystack-log4j-metrics-appender).