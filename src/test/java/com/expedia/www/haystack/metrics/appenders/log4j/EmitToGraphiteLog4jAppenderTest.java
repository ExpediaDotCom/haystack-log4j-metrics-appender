/*
 * Copyright 2018 Expedia, Inc.
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
import com.expedia.www.haystack.metrics.appenders.log4j.EmitToGraphiteLog4jAppender.Configuration;
import com.expedia.www.haystack.metrics.appenders.log4j.EmitToGraphiteLog4jAppender.Factory;
import com.google.common.collect.Sets;
import com.netflix.servo.monitor.Counter;
import org.apache.logging.log4j.Level;
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
import java.util.Timer;

import static com.expedia.www.haystack.metrics.appenders.log4j.EmitToGraphiteLog4jAppender.ERRORS_COUNTERS;
import static com.expedia.www.haystack.metrics.appenders.log4j.EmitToGraphiteLog4jAppender.ERRORS_METRIC_GROUP;
import static com.expedia.www.haystack.metrics.appenders.log4j.EmitToGraphiteLog4jAppender.NULL_STACK_TRACE_ELEMENT_MSG;
import static com.expedia.www.haystack.metrics.appenders.log4j.EmitToGraphiteLog4jAppender.changePeriodsToDashes;
import static com.expedia.www.haystack.metrics.appenders.log4j.EmitToGraphiteLog4jAppender.createAppender;
import static org.apache.logging.log4j.Level.ERROR;
import static org.apache.logging.log4j.Level.FATAL;
import static org.apache.logging.log4j.Level.WARN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EmitToGraphiteLog4jAppenderTest {
    private static final Random RANDOM = new Random();
    private static final Class<EmitToGraphiteLog4jAppenderTest> CLASS = EmitToGraphiteLog4jAppenderTest.class;
    private static final String FULLY_QUALIFIED_CLASS_NAME = CLASS.getName().replace('.', '-');
    private static final String APPENDER_NAME = RANDOM.nextLong() + "APPENDER_NAME";
    private static final String SUBSYSTEM = RANDOM.nextLong() + "SUBSYSTEM";
    private static final String HOST = RANDOM.nextLong() + "HOST";
    private static final String METHOD_NAME = RANDOM.nextLong() + "METHOD_NAME";
    private static final String FILE_NAME = RANDOM.nextLong() + "FILE_NAME";
    private static final int LINE_NUMBER = RANDOM.nextInt(Integer.MAX_VALUE);
    private static final int PORT = RANDOM.nextInt(Integer.MAX_VALUE);
    private static final int POLL_INTERVAL_SECONDS = RANDOM.nextInt(Integer.MAX_VALUE);
    private static final int QUEUE_SIZE = RANDOM.nextInt(Integer.MAX_VALUE);
    private static final boolean SEND_AS_RATE = RANDOM.nextBoolean();
    //private static final String S_LINE_NUMBER = Integer.toString(LINE_NUMBER);
    private static final String KEY = changePeriodsToDashes(FULLY_QUALIFIED_CLASS_NAME)/* + ':' + S_LINE_NUMBER*/;
    private String COUNTER_NAME = ERROR.name();
    private static final Configuration CONFIGURATION =
            new Configuration(HOST, PORT, POLL_INTERVAL_SECONDS, QUEUE_SIZE, SEND_AS_RATE);
    private static final GraphiteConfig GRAPHITE_CONFIG =
            new GraphiteConfigImpl(HOST, PORT, POLL_INTERVAL_SECONDS, QUEUE_SIZE, SEND_AS_RATE);


    @Mock
    private LogEvent mockLogEvent;

    @Mock
    private Factory mockFactory;
    private Factory realFactory;

    @Mock
    private Counter mockCounter;
    
    @Mock
    private MetricObjects mockMetricObjects;

    @Mock
    private MetricPublishing mockMetricPublishing;

    @Mock
    private EmitToGraphiteLog4jAppender mockEmitToGraphiteLog4jAppender;

    @Mock
    private StartUpMetric mockStartUpMetric;

    @Mock
    private Timer mockTimer;

    @Mock
    private Logger mockLogger;
    private Logger realLogger;

    private StackTraceElement stackTraceElement;
    private EmitToGraphiteLog4jAppender emitToGraphiteLog4jAppender;

    @Before
    public void setUp() {
        stubOutStaticDependencies();
        ERRORS_COUNTERS.clear();
        stackTraceElement = new StackTraceElement(FULLY_QUALIFIED_CLASS_NAME, METHOD_NAME, FILE_NAME, LINE_NUMBER);
        emitToGraphiteLog4jAppender = new EmitToGraphiteLog4jAppender(SUBSYSTEM, APPENDER_NAME,
                mockMetricPublishing, mockMetricObjects, mockFactory, CONFIGURATION, mockStartUpMetric);
    }

    private void stubOutStaticDependencies() {
        realLogger = EmitToGraphiteLog4jAppender.logger;
        EmitToGraphiteLog4jAppender.logger = mockLogger;
        realFactory = EmitToGraphiteLog4jAppender.staticFactory;
        EmitToGraphiteLog4jAppender.staticFactory = mockFactory;
    }

    @After
    public void tearDown() {
        restoreStaticDependencies();
        verifyNoMoreInteractions(mockLogEvent, mockFactory, mockCounter, mockMetricObjects, mockMetricPublishing,
                mockEmitToGraphiteLog4jAppender, mockStartUpMetric, mockLogger, mockTimer);
    }

    private void restoreStaticDependencies() {
        EmitToGraphiteLog4jAppender.logger = realLogger;
        EmitToGraphiteLog4jAppender.staticFactory = realFactory;
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
        when(mockFactory.createCounter(any(MetricObjects.class), anyString(), anyString(), /*anyString(), */anyString()))
                .thenReturn(mockCounter);
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
        when(mockFactory.createCounter(any(MetricObjects.class), anyString(), anyString(), /*anyString(), */anyString()))
                .thenReturn(mockCounter);

        emitToGraphiteLog4jAppender.append(mockLogEvent);

        verify(mockLogEvent).getLevel();
        verify(mockLogEvent).getSource();
        assertSame(mockCounter, ERRORS_COUNTERS.get(KEY));
        verify(mockFactory).createCounter(
                mockMetricObjects, SUBSYSTEM, FULLY_QUALIFIED_CLASS_NAME, /*S_LINE_NUMBER, */COUNTER_NAME);
        verify(mockCounter).increment();
    }

    @Test
    public void testGetCounterAlreadyExists() {
        when(mockFactory.createCounter(any(MetricObjects.class), anyString(), anyString(), /*anyString(), */anyString()))
                .thenReturn(mockCounter);

        final Counter counter = emitToGraphiteLog4jAppender.getCounter(ERROR, stackTraceElement);
        assertSame(counter, emitToGraphiteLog4jAppender.getCounter(ERROR, stackTraceElement));

        assertEquals(1, ERRORS_COUNTERS.size());
        assertSame(mockCounter, ERRORS_COUNTERS.get(KEY));
        verify(mockFactory).createCounter(
                mockMetricObjects, SUBSYSTEM, FULLY_QUALIFIED_CLASS_NAME, /*S_LINE_NUMBER, */COUNTER_NAME);
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
        when(mockMetricObjects.createAndRegisterResettingCounter(
                anyString(), anyString(), anyString(), /*anyString(), */anyString())).thenReturn(mockCounter);

        final Counter counter = realFactory.createCounter(
                mockMetricObjects, SUBSYSTEM, FULLY_QUALIFIED_CLASS_NAME, /*S_LINE_NUMBER, */COUNTER_NAME);

        assertSame(mockCounter, counter);
        verify(mockMetricObjects).createAndRegisterResettingCounter(
                ERRORS_METRIC_GROUP, SUBSYSTEM, FULLY_QUALIFIED_CLASS_NAME, /*S_LINE_NUMBER, */COUNTER_NAME);
    }

    @Test
    public void testStart() {
        emitToGraphiteLog4jAppender.start();

        verify(mockMetricPublishing).start(GRAPHITE_CONFIG);
        verify(mockStartUpMetric).start();
    }

    @Test
    public void testStopNullStartUpMetric() {
        emitToGraphiteLog4jAppender.stop();

        assertTrue(emitToGraphiteLog4jAppender.isStopped());
        verify(mockMetricPublishing).stop();
        verify(mockStartUpMetric).stop();
    }

    @Test
    public void testStopNonNullStartUpMetric() {
        emitToGraphiteLog4jAppender.setStartUpMetric(mockStartUpMetric);
        emitToGraphiteLog4jAppender.stop();

        assertTrue(emitToGraphiteLog4jAppender.isStopped());
        verify(mockMetricPublishing).stop();
        verify(mockStartUpMetric).stop();
    }

    @Test
    public void testCreateAppender() {
        when(mockFactory.createEmitToGraphiteLog4jAppender(anyString(), anyString()))
                .thenReturn(mockEmitToGraphiteLog4jAppender);
        when(mockFactory.createStartUpMetric(anyString(), any(MetricObjects.class), any(Timer.class)))
                .thenReturn(mockStartUpMetric);
        when(mockFactory.createConfiguration(anyString(), anyInt(), anyInt(), anyInt(), anyBoolean()))
                .thenReturn(CONFIGURATION);

        final EmitToGraphiteLog4jAppender appender
                = createAppender(SUBSYSTEM, APPENDER_NAME, HOST, PORT, POLL_INTERVAL_SECONDS, QUEUE_SIZE, SEND_AS_RATE);

        assertSame(mockEmitToGraphiteLog4jAppender, appender);
        verify(mockFactory).createConfiguration(HOST, PORT, POLL_INTERVAL_SECONDS, QUEUE_SIZE, SEND_AS_RATE);
        verify(mockFactory).createEmitToGraphiteLog4jAppender(SUBSYSTEM, APPENDER_NAME);
        verify(mockFactory).createStartUpMetric(eq(SUBSYSTEM), any(MetricObjects.class), any(Timer.class));
        verify(mockEmitToGraphiteLog4jAppender).setStartUpMetric(mockStartUpMetric);
    }

    @Test
    public void testConfigurationConstructor() {
        assertEquals(HOST, CONFIGURATION.host);
        assertEquals(PORT, CONFIGURATION.port);
        assertEquals(POLL_INTERVAL_SECONDS, CONFIGURATION.pollintervalseconds);
        assertEquals(QUEUE_SIZE, CONFIGURATION.queuesize);
        assertEquals(SEND_AS_RATE, CONFIGURATION.sendasrate);
    }
}
