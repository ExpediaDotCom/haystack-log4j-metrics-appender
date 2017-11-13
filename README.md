# haystack-log4j-metrics-appender
A log4j appender that sends an error count to a graphite endpoint

## Overview
To facilitate counting errors and alarming when they occur, this package contains a
[Log4j](https://en.wikipedia.org/wiki/Log4j)
[Appender](https://logging.apache.org/log4j/1.2/apidocs/org/apache/log4j/Appender.html)
that sends a [Graphite](https://graphiteapp.org/)
[plaintext protocol](http://graphite.readthedocs.io/en/latest/feeding-carbon.html#the-plaintext-protocol)
message to a Graphite endpoint. The intent of this Appender is to make it easier to alarm on error messages,
using the [Grafana](https://grafana.com/) [alert notifications](http://docs.grafana.org/alerting/notifications/),
or a similar metrics-based mechanism.

### Metric Message Format
Graphite plain text protocol messages are of the form "<name> <value> <timestamp>" where <name> is typically a
period-delimited String, <value> is a number (either integer or floating point) and <timestamp> is a Linux epoch
style number (milliseconds since January 1, 1970, midnight GMT). The format of the <name> created by
[EmitToGraphiteLog4jAppender](TODO put the link to EmitToGraphiteLog4jAppender here) is (TODO explain, put in link
to haystack repository where the template is configured).