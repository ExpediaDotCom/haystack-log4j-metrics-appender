# Release Notes

## 1.0.7 / 2018-10-16 Use com.fasterxml.jackson.* 2.9.8
Fixed security vulnerability

## 1.0.6 / 2017-12-28 Remove line number from ERROR_COUNTER metric
See https://github.com/ExpediaDotCom/haystack-log4j-metrics-appender/issues/30

## 1.0.4 / 2018-10-16 Use com.fasterxml.jackson.* 2.9.7
Fixed security vulnerability CVE-2018-7489

## 1.0.3 / 2018-06-22 Change haystack-metrics version to 2.0.1

## 1.0.2 / 2018-04-13 Use haystack-metrics 2.0.0
Deployment issues with SonaType; hopefully fixed with a new version (no code changes).

## 1.0.1 / 2018-04-12 Use haystack-metrics 2.0.0
This new version of haystack-metrics removes support for Servo's StatsTimer.
Change StartUpMetric interval to 5 minutes.

## 1.0.0 / 2018-04-09 Use haystack-metrics 1.0.0
This new version of haystack-metrics provides access to Servo's StatsTimer and BucketTimer. These two new Timer types
are not used by haystack-log4j-metrics-appender.

## 0.1.13 / 2018-03-06 Change suggesting polling interval to 5 minutes
Change the suggested polling interval, documented in log4j2-test.yaml, to 5 minutes. No functional changes.

## 0.1.12 / 2018-03-06 Use new haystack-metrics 0.10.0
This new version of haystack-metrics counts that number of times that the metrics polling thread was started
with a static variable instead of an instance variable.

## 0.1.11 / 2018-03-05 Use new haystack-metrics 0.9.0
This new version of haystack-metrics counts that number of times that the metrics polling thread was started, so that it
can avoid shutting down that thread prematurely. This change was necessitated by the behavior of log4j2 when starting
up.

## 0.1.10 / 2018-03-05 Use log4j2 lifecycle properly
The factory method that creates the appender was starting the metrics polling thread, which is incorrect; as a result of
this bug, the start up metric was never active for long, as log4j stops and then restarts the appender during start-up.
The fix is to make EmitToGraphiteLog4jAppender.start() start both the metrics polling thread and the start up metric,
and to make EmitToGraphiteLog4jAppender.stop() shut them both down.

## 0.1.9 / 2018-03-01 Use new haystack-metrics 0.8.0
This new version of haystack-metrics has an additional informational log message when the metrics polling starts.

## 0.1.8 / 2018-02-22 Use new haystack-metrics 0.7.0
This new version of haystack-metrics performs environment variable substitution for environment variables prefixed by
the typical ${ and suffixed by the typical }. This was needed to allow the specification of GRAPHITE_HOST by
environment variables.

## 0.1.7 / 2018-02-08 Use new haystack-metrics 0.6.0
This new version of haystack-metrics ignores IllegalStateException when shutting down the poller.
(the IllegalStateException can happen if the poller has not been started.)

## 0.1.6 / 2018-01-25 Upgrade the version of log4j2
Upgraded from 2.9.1 to 2.10.0; also made the versions of jackson-databind and jackson-dataformat-yaml always match.

## 0.1.5 / 2018-01-10 Use new haystack-metrics API for error counter
so that the individual applications' error metrics can be identified as specific to each application

## 0.1.4 / 2018-01-04 Emit an ERROR metric, with a count of 0, every minute
The writing of a metric to show that the appender is working now occurs in a background thread every minute;
the value of the metric thus emitted will be 0. When an error occurs, the value of the metric will be greater than 0,
and the tags of the metric will be different than what was emitted by the background thread.

## 0.1.3 / 2017-12-18 Separate polling thread per appender; emit start up metric; call close() when shutting down
As part of an effort to be sure that the connection to the metrics database is closed when the appender is
no longer being used, each appender will have its own polling thread, and close() will be called appropriately.
This version also includes the writing of a start up metric to show that the appender is working, since it
doesn't emit any metrics until an error occurs.

## 0.1.2 / 2017-12-02 Upgrade metrics to 0.2.7
This resulted in the renaming of variables and methods to refer to "host" instead of "address"
and is done to have a consistent name across all of Haystack.

## 0.1.1 / 2017-12-02 Upgrade metrics to 0.2.6

## 0.1.0 / 2017-11-20 Initial release to SonaType Nexus Repository