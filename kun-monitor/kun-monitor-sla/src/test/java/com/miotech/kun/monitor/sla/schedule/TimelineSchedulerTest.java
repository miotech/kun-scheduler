package com.miotech.kun.monitor.sla.schedule;

import com.miotech.kun.monitor.sla.MonitorSlaTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TimelineSchedulerTest extends MonitorSlaTestBase {

    @SpyBean
    private TimelineScheduler timelineScheduler;

    @Test
    public void testJobRun() {
        await().atMost(Duration.ofMinutes(2)).untilAsserted(() -> verify(timelineScheduler, times(1)).execute());
    }

}
