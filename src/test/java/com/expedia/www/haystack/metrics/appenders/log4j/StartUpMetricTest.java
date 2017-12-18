package com.expedia.www.haystack.metrics.appenders.log4j;

import com.netflix.servo.monitor.Counter;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.expedia.www.haystack.metrics.appenders.log4j.StartUpMetric.LINE_NUMBER_OF_EMIT_START_UP_METRIC_METHOD;
import static com.expedia.www.haystack.metrics.appenders.log4j.StartUpMetric.METRIC_VALUE;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StartUpMetricTest {
    private static final String FULLY_QUALIFIED_CLASS_NAME = EmitToGraphiteLog4jAppender.changePeriodsToDashes(
            StartUpMetric.class.getName());

    @Mock
    private EmitToGraphiteLog4jAppender.Factory mockFactory;

    @Mock
    private Counter mockCounter;

    private StartUpMetric startUpMetric;

    @Before
    public void setUp() {
        startUpMetric = new StartUpMetric();
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(mockFactory, mockCounter);
    }

    @Test
    public void testEmit() {
        when(mockFactory.createCounter(anyString(), anyString(), anyString())).thenReturn(mockCounter);

        startUpMetric.emit(mockFactory);

        verify(mockFactory).createCounter(
                FULLY_QUALIFIED_CLASS_NAME, LINE_NUMBER_OF_EMIT_START_UP_METRIC_METHOD, Level.ERROR.toString());
        verify(mockCounter).increment(METRIC_VALUE);
    }
}
