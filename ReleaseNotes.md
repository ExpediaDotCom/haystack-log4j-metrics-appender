# Release Notes

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