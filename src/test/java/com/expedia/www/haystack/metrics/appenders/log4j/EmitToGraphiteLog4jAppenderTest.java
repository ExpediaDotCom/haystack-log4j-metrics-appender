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
import com.expedia.www.haystack.metrics.appenders.log4j.EmitToGraphiteLog4jAppender.Factory;
import com.google.common.collect.Sets;
import com.netflix.servo.monitor.Counter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LogEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Random;
import java.util.Set;

import static com.expedia.www.haystack.metrics.appenders.log4j.EmitToGraphiteLog4jAppender.ERRORS_COUNTERS;
import static com.expedia.www.haystack.metrics.appenders.log4j.EmitToGraphiteLog4jAppender.METRIC_PUBLISHING;
import static com.expedia.www.haystack.metrics.appenders.log4j.EmitToGraphiteLog4jAppender.NULL_STACK_TRACE_ELEMENT_MSG;
import static com.expedia.www.haystack.metrics.appenders.log4j.EmitToGraphiteLog4jAppender.SUBSYSTEM;
import static org.apache.logging.log4j.Level.ERROR;
import static org.apache.logging.log4j.Level.FATAL;
import static org.apache.logging.log4j.Level.WARN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EmitToGraphiteLog4jAppenderTest {
    private static final Random RANDOM = new Random();
    private static final Class<EmitToGraphiteLog4jAppenderTest> CLASS = EmitToGraphiteLog4jAppenderTest.class;
    private static final String FULLY_QUALIFIED_CLASS_NAME = CLASS.getName().replace('.', '-');
    private static final String APPENDER_NAME = RANDOM.nextLong() + "APPENDER_NAME";
    private static final String HOST = RANDOM.nextLong() + "HOST";
    private static final String METHOD_NAME = RANDOM.nextLong() + "METHOD_NAME";
    private static final String FILE_NAME = RANDOM.nextLong() + "FILE_NAME";
    private static final int LINE_NUMBER = RANDOM.nextInt(Integer.MAX_VALUE);
    private static final int PORT = RANDOM.nextInt(Integer.MAX_VALUE);
    private static final int POLL_INTERVAL_SECONDS = RANDOM.nextInt(Integer.MAX_VALUE);
    private static final int QUEUE_SIZE = RANDOM.nextInt(Integer.MAX_VALUE);
    private static final GraphiteConfig GRAPHITE_CONFIG = new GraphiteConfigImpl(
            HOST, PORT, POLL_INTERVAL_SECONDS, QUEUE_SIZE);
    private static final String S_LINE_NUMBER = Integer.toString(LINE_NUMBER);
    private static final String COUNTER_NAME = ERROR.name();
    private static final int NUMBER_OF_ITERATIONS_IN_TESTS = RANDOM.nextInt(Byte.MAX_VALUE) + 2;

    @Mock
    private LogEvent mockLogEvent;

    @Mock
    private Factory mockFactory;
    private Factory realFactory;

    @Mock
    private Counter mockCounter;
    
    @Mock
    private MetricObjects mockMetricObjects;
    private MetricObjects realMetricObjects;

    @Mock
    private MetricPublishing mockMetricPublishing;

    @Mock
    private Logger mockLogger;
    private Logger realLogger;

    private StackTraceElement stackTraceElement;
    private EmitToGraphiteLog4jAppender emitToGraphiteLog4jAppender;
    private int expectedTimesForCreateMetricPublishing = 1;

    @Before
    public void setUp() {
        stubOutStaticDependencies();
        METRIC_PUBLISHING.set(null);
        ERRORS_COUNTERS.clear();
        stackTraceElement = new StackTraceElement(FULLY_QUALIFIED_CLASS_NAME, METHOD_NAME, FILE_NAME, LINE_NUMBER);
        when(mockFactory.createMetricPublishing()).thenReturn(mockMetricPublishing);
        emitToGraphiteLog4jAppender = EmitToGraphiteLog4jAppender.createAppender(
                APPENDER_NAME, HOST, PORT, POLL_INTERVAL_SECONDS, QUEUE_SIZE);
    }

    private void stubOutStaticDependencies() {
        realFactory = EmitToGraphiteLog4jAppender.factory;
        EmitToGraphiteLog4jAppender.factory = mockFactory;
        realMetricObjects = Factory.metricObjects;
        Factory.metricObjects = mockMetricObjects;
        realLogger = EmitToGraphiteLog4jAppender.logger;
        EmitToGraphiteLog4jAppender.logger = mockLogger;
    }

    @After
    public void tearDown() {
        restoreStaticDependencies();
        verify(mockFactory, times(expectedTimesForCreateMetricPublishing)).createMetricPublishing();
        verify(mockMetricPublishing).start(GRAPHITE_CONFIG);
        verifyNoMoreInteractions(mockLogEvent, mockFactory, mockCounter, mockMetricObjects, mockMetricPublishing,
                mockLogger);
    }

    private void restoreStaticDependencies() {
        EmitToGraphiteLog4jAppender.factory = realFactory;
        Factory.metricObjects = realMetricObjects;
        EmitToGraphiteLog4jAppender.logger = realLogger;
    }

    @Test
    public void testAppendLevelNotSevereEnoughToCount() {
        when(mockLogEvent.getLevel()).thenReturn(WARN);

        emitToGraphiteLog4jAppender.append(mockLogEvent);

        verify(mockLogEvent).getLevel();
    }

    @Test
    public void testAppendNullStackTraceElement() {
        when(mockLogEvent.getLevel()).thenReturn(ERROR);
        when(mockFactory.createCounter(anyString(), anyString(), anyString())).thenReturn(mockCounter);
        final String className = EmitToGraphiteLog4jAppender.class.getName();
        when(mockLogEvent.getLoggerFqcn()).thenReturn(className);

        emitToGraphiteLog4jAppender.append(mockLogEvent);

        verify(mockLogEvent).getLevel();
        verify(mockLogEvent).getSource();
        verify(mockLogEvent).getLoggerFqcn();
        verify(mockLogger).error(NULL_STACK_TRACE_ELEMENT_MSG, mockLogEvent, className);
    }

    @Test
    public void testAppendNonNullStackTraceElement() {
        when(mockLogEvent.getLevel()).thenReturn(ERROR);
        when(mockLogEvent.getSource()).thenReturn(stackTraceElement);
        when(mockFactory.createCounter(anyString(), anyString(), anyString())).thenReturn(mockCounter);
        final int hashCode = stackTraceElement.hashCode();

        emitToGraphiteLog4jAppender.append(mockLogEvent);

        verify(mockLogEvent).getLevel();
        verify(mockLogEvent).getSource();
        assertSame(mockCounter, ERRORS_COUNTERS.get(hashCode));
        verify(mockFactory).createCounter(FULLY_QUALIFIED_CLASS_NAME, S_LINE_NUMBER, COUNTER_NAME);
        verify(mockCounter).increment();
    }

    @Test
    public void testGetCounterAlreadyExists() {
        when(mockFactory.createCounter(anyString(), anyString(), anyString())).thenReturn(mockCounter);
        final int hashCode = stackTraceElement.hashCode();

        final Counter counter = emitToGraphiteLog4jAppender.getCounter(ERROR, stackTraceElement, hashCode);
        assertSame(counter, emitToGraphiteLog4jAppender.getCounter(ERROR, stackTraceElement, hashCode));

        assertEquals(1, ERRORS_COUNTERS.size());
        assertSame(mockCounter, ERRORS_COUNTERS.get(hashCode));
        verify(mockFactory).createCounter(FULLY_QUALIFIED_CLASS_NAME, S_LINE_NUMBER, COUNTER_NAME);
    }

    @Test
    public void testIsLevelSevereEnoughToCount() {
        final Set<Level> levelsThatAreSevereEnoughToCount = Sets.newHashSet(ERROR, FATAL);
        final Level[] levels = Level.values();
        for(final Level level : levels) {
            assertEquals(levelsThatAreSevereEnoughToCount.contains(level),
                    emitToGraphiteLog4jAppender.isLevelSevereEnoughToCount(level));
        }
    }
    
    @Test
    public void testFactoryCreateCounter() {
        when(mockMetricObjects.createAndRegisterCounter(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(mockCounter);

        final Counter counter = realFactory.createCounter(FULLY_QUALIFIED_CLASS_NAME, S_LINE_NUMBER, COUNTER_NAME);

        assertSame(mockCounter, counter);
        verify(mockMetricObjects).createAndRegisterCounter(
                SUBSYSTEM, FULLY_QUALIFIED_CLASS_NAME, S_LINE_NUMBER, COUNTER_NAME);
    }

    @Test
    public void testEndToEndFunctionalBehavior() {
        EmitToGraphiteLog4jAppender.factory = realFactory;
        when(mockMetricObjects.createAndRegisterCounter(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(mockCounter);

        final int lineNumberOfThisNewThrowable = new Throwable().getStackTrace()[0].getLineNumber();
        final String lineNumberOfTheLoggerDotErrorCall = Integer.toString(lineNumberOfThisNewThrowable + 3);
        for(int i = 0; i < NUMBER_OF_ITERATIONS_IN_TESTS; i++) {
            LogManager.getLogger(EmitToGraphiteLog4jAppenderTest.class).error("Test");
        }

        verify(mockMetricObjects).createAndRegisterCounter(
                SUBSYSTEM, FULLY_QUALIFIED_CLASS_NAME, lineNumberOfTheLoggerDotErrorCall, COUNTER_NAME);
        verify(mockCounter, times(NUMBER_OF_ITERATIONS_IN_TESTS)).increment();
    }

    @Test
    public void testStartMetricPublishingBackgroundThread() {
        for(int i = 0; i < NUMBER_OF_ITERATIONS_IN_TESTS; i++) {
            EmitToGraphiteLog4jAppender.startMetricPublishingBackgroundThreadIfNotAlreadyStarted(
                    HOST, PORT, POLL_INTERVAL_SECONDS, QUEUE_SIZE);
        }

        expectedTimesForCreateMetricPublishing = NUMBER_OF_ITERATIONS_IN_TESTS + 1;
    }
}
