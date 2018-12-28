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

import com.expedia.www.haystack.metrics.MetricObjects;
import com.netflix.servo.monitor.Counter;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

//import static com.expedia.www.haystack.metrics.appenders.log4j.StartUpMetric.LINE_NUMBER_OF_EMIT_START_UP_METRIC_METHOD;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StartUpMetricTest {
    private static final Random RANDOM = new Random();
    private static final String SUBSYSTEM = RANDOM.nextLong() + "SUBSYSTEM";
    private static final String FULLY_QUALIFIED_CLASS_NAME = EmitToGraphiteLog4jAppender.changePeriodsToDashes(
            StartUpMetric.class.getName());

    @Mock
    private EmitToGraphiteLog4jAppender.Factory mockFactory;

    @Mock
    private Timer mockTimer;

    @Mock
    private Counter mockCounter;

    @Mock
    private MetricObjects mockMetricObjects;

    private StartUpMetric startUpMetric;

    @Before
    public void setUp() {
        when(mockFactory.createCounter(any(MetricObjects.class), anyString(), anyString(), /*anyString(), */anyString()))
                .thenReturn(mockCounter);
        startUpMetric = new StartUpMetric(SUBSYSTEM, mockTimer, mockFactory, mockMetricObjects);
    }

    @After
    public void tearDown() {
        verify(mockFactory).createCounter(mockMetricObjects,
                SUBSYSTEM, FULLY_QUALIFIED_CLASS_NAME, /*LINE_NUMBER_OF_EMIT_START_UP_METRIC_METHOD, */Level.ERROR.toString());
        verifyNoMoreInteractions(mockFactory, mockTimer, mockCounter, mockMetricObjects);
    }

    @Test
    public void testStart() {
        startUpMetric.start();

        final ArgumentCaptor<TimerTask> argumentCaptor = ArgumentCaptor.forClass(TimerTask.class);
        verify(mockTimer).scheduleAtFixedRate(argumentCaptor.capture(), eq(0L), eq(300000L));
        final TimerTask timerTask = argumentCaptor.getValue();
        timerTask.run();
        verify(mockCounter).increment(0);
    }

    @Test
    public void testEmit() {
        startUpMetric.emit();

        verify(mockFactory).createCounter(mockMetricObjects,
                SUBSYSTEM, FULLY_QUALIFIED_CLASS_NAME, /*LINE_NUMBER_OF_EMIT_START_UP_METRIC_METHOD, */Level.ERROR.toString());
        verify(mockCounter).increment(0);
    }

    @Test
    public void testStop() {
        startUpMetric.stop();

        verify(mockTimer).cancel();
    }
}
