/*
 * Copyright 2017 Expedia, Inc.
 *
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 *
 */
package com.expedia.www.haystack.metrics.appenders.log4j;

import com.expedia.www.haystack.metrics.GraphiteConfig;
import com.expedia.www.haystack.metrics.GraphiteConfigImpl;
import com.expedia.www.haystack.metrics.MetricObjects;
import com.expedia.www.haystack.metrics.MetricPublishing;
import com.netflix.servo.monitor.Counter;
import com.netflix.servo.util.VisibleForTesting;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static org.apache.logging.log4j.Level.ERROR;
import static org.apache.logging.log4j.Level.FATAL;

/**
 * A log4j2 appender that sends an error count to a graphite endpoint.
 */
@Plugin(name = "EmitToGraphiteLog4jAppender", category = "Core", elementType = "appender")
public class EmitToGraphiteLog4jAppender extends AbstractAppender {
    @VisibleForTesting
    static final String NULL_STACK_TRACE_ELEMENT_MSG = "Null StackTraceElement found for LogEvent [%s] LoggerFqcn [%s]";
    @VisibleForTesting
    static final String SUBSYSTEM = "errors";
    @VisibleForTesting
    static final AtomicReference<MetricPublishing> METRIC_PUBLISHING = new AtomicReference<>(null);
    @VisibleForTesting
    static final Map<Integer, Counter> ERRORS_COUNTERS = new ConcurrentHashMap<>();

    @VisibleForTesting
    static Factory factory = new Factory();
    @VisibleForTesting
    static Logger logger = LogManager.getLogger(EmitToGraphiteLog4jAppender.class);

    private EmitToGraphiteLog4jAppender(String name) {
        super(name, null, null);
    }

    @PluginFactory
    static EmitToGraphiteLog4jAppender createAppender(
            @PluginAttribute(value = "name", defaultString = "EmitToGraphiteLog4jAppender") String name,
            @PluginAttribute(value = "address", defaultString = "haystack.local") String address,
            @PluginAttribute(value = "port", defaultInt = 2003) int port,
            @PluginAttribute(value = "pollintervalseconds", defaultInt = 60) int pollintervalseconds,
            @PluginAttribute(value = "queuesize", defaultInt = 10) int queuesize) {
        // Start metric publishing background thread if it isn't already running
        startMetricPublishingBackgroundThreadIfNotAlreadyStarted(address, port, pollintervalseconds, queuesize);
        return new EmitToGraphiteLog4jAppender(name);
    }

    @VisibleForTesting
    static void startMetricPublishingBackgroundThreadIfNotAlreadyStarted(
            String address, int port, int pollintervalseconds, int queuesize) {
        if (METRIC_PUBLISHING.compareAndSet(null, factory.createMetricPublishing())) {
            final GraphiteConfig graphiteConfig = new GraphiteConfigImpl(address, port, pollintervalseconds, queuesize);
            METRIC_PUBLISHING.get().start(graphiteConfig);
        }
    }

    /**
     * Counts a log event if and only if it is an ERROR or a FATAL.
     * @param logEvent the log event; if logEvent.getSource() returns null (as its JavaDoc says it can), then an error
     *                 will be logged, otherwise Servo counter that identifies the source of the error will be
     *                 incremented.
     */
    @Override
    public void append(LogEvent logEvent) {
        final Level level = logEvent.getLevel();
        if (isLevelSevereEnoughToCount(level)) {
            final StackTraceElement stackTraceElement = logEvent.getSource();
            // JavaDoc says it can be null, but it's unclear when that would happen; if it does, log an error, but note
            // that a null StackTraceElement during that logging will result in infinite recursion!
            if (stackTraceElement != null) {
                getCounter(level, stackTraceElement, stackTraceElement.hashCode()).increment();
            } else {
                logger.error(NULL_STACK_TRACE_ELEMENT_MSG, logEvent, logEvent.getLoggerFqcn()); // should never happen
            }
        }
    }

    @VisibleForTesting
    Counter getCounter(Level level, StackTraceElement stackTraceElement, int hashCode) {
        if (!ERRORS_COUNTERS.containsKey(hashCode)) {
            final String fullyQualifiedClassName = stackTraceElement.getClassName().replace('.', '-');
            final String lineNumber = Integer.toString(stackTraceElement.getLineNumber());
            final Counter counter = factory.createCounter(fullyQualifiedClassName, lineNumber, level.name());

            // It is possible but highly unlikely that two threads are in this if() block at the same time; if that
            // occurs, only one of the calls to ERRORS_COUNTERS.putIfAbsent(hashCode, counter) in the next line of code
            // will succeed, but the increment of the thread whose call did not succeed will not be lost, because the
            // value returned by this method will be the Counter put successfully by the other thread.
            ERRORS_COUNTERS.putIfAbsent(hashCode, counter);
        }
        return ERRORS_COUNTERS.get(hashCode);
    }

    @VisibleForTesting
    boolean isLevelSevereEnoughToCount(Level level) {
        return level == ERROR || level == FATAL;
    }

    @VisibleForTesting
    static class Factory {
        static MetricObjects metricObjects = new MetricObjects();

        Counter createCounter(String application, String className, String counterName) {
            return metricObjects.createAndRegisterCounter(SUBSYSTEM, application, className, counterName);
        }

        MetricPublishing createMetricPublishing() {
            return new MetricPublishing();
        }
    }
}
