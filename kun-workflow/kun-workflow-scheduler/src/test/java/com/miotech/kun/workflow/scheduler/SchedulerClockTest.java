package com.miotech.kun.workflow.scheduler;

import com.google.common.eventbus.EventBus;
import com.miotech.kun.workflow.core.event.Event;
import com.miotech.kun.workflow.core.event.TickEvent;
import com.miotech.kun.workflow.testing.event.EventCollector;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class SchedulerClockTest extends SchedulerTestBase {
    @Inject
    private SchedulerClock schedulerClock;

    @Inject
    private EventBus eventBus;

    private EventCollector eventCollector;

    @Override
    protected void configuration() {
        super.configuration();
    }

    @Before
    public void setUp() {
        eventCollector = new EventCollector();
        eventBus.register(eventCollector);
    }

    @Test
    public void testEmitTick() {
        // process
        schedulerClock.start();
        await().atMost(120, TimeUnit.SECONDS).until(() ->
                eventCollector.getEvents().size() == 2);

        // verify
        List<Event> results = eventCollector.getEvents();
        assertThat(results.size(), is(2));

        TickEvent tick1 = (TickEvent) results.get(0);
        TickEvent tick2 = (TickEvent) results.get(1);

        long diff = Math.abs(tick2.getTick().toEpochSecond() - tick2.getTimestamp() / 1000);
        assertThat(diff, lessThanOrEqualTo(1L));
        long rem = tick2.getTimestamp() % 60000;
        assertThat(rem, lessThan(1000L));
        long duration = tick2.getTimestamp() - tick1.getTimestamp();
        assertThat(duration, allOf(greaterThan(59000L), lessThan(61000L)));
    }
}