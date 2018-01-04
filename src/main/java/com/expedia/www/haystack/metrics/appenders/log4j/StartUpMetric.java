package com.expedia.www.haystack.metrics.appenders.log4j;

import com.expedia.www.haystack.metrics.MetricObjects;
import com.netflix.servo.monitor.Counter;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static com.expedia.www.haystack.metrics.appenders.log4j.EmitToGraphiteLog4jAppender.changePeriodsToDashes;
import static org.apache.logging.log4j.Level.ERROR;

class StartUpMetric {
    private static final int METRIC_VALUE = 0;
    private static final long INITIAL_DELAY_MILLIS = 0L;
    private static final int INTERVAL_MINUTES = 1;
    private static final String FULLY_QUALIFIED_CLASS_NAME = changePeriodsToDashes(
            StartUpMetric.class.getName());
    private final Timer timer;
    private final Counter counter;

    StartUpMetric(Timer timer, EmitToGraphiteLog4jAppender.Factory factory, MetricObjects metricObjects) {
        this.timer = timer;
        counter = factory.createCounter(metricObjects,
                FULLY_QUALIFIED_CLASS_NAME, LINE_NUMBER_OF_EMIT_START_UP_METRIC_METHOD, ERROR.toString());
    }

    void start() {
        timer.scheduleAtFixedRate(
                new TimerTask() {
                    public void run() {
                        emit();
                    }
                },
                INITIAL_DELAY_MILLIS,
                TimeUnit.MINUTES.toMillis(INTERVAL_MINUTES));
    }

    void stop() {
        timer.cancel();
    }

    static final String LINE_NUMBER_OF_EMIT_START_UP_METRIC_METHOD = Integer.toString(
            new Throwable().getStackTrace()[0].getLineNumber() + 2);
    void emit() {
        counter.increment(METRIC_VALUE);
    }
}
