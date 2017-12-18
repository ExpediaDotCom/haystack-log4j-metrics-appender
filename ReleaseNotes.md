# Release Notes

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