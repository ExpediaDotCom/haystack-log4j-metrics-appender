package com.expedia.www.haystack.metrics.appenders.log4j;

import com.netflix.servo.monitor.Counter;
import static org.apache.logging.log4j.Level.ERROR;

class StartUpMetric {
    static final int METRIC_VALUE = -1;
    static final String LINE_NUMBER_OF_EMIT_START_UP_METRIC_METHOD = Integer.toString(
            new Throwable().getStackTrace()[0].getLineNumber() + 2);
    void emit(EmitToGraphiteLog4jAppender.Factory factory) {
        final String fullyQualifiedClassName = EmitToGraphiteLog4jAppender.changePeriodsToDashes(
                StartUpMetric.class.getName());
        final Counter counter = factory.createCounter(
                fullyQualifiedClassName, LINE_NUMBER_OF_EMIT_START_UP_METRIC_METHOD, ERROR.toString());
        counter.increment(METRIC_VALUE);
    }
}
