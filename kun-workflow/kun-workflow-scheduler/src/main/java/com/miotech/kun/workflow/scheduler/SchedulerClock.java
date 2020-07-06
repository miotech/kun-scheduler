package com.miotech.kun.workflow.scheduler;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.miotech.kun.workflow.core.event.TickEvent;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.OffsetDateTime;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Singleton
public class SchedulerClock {
    private static final Logger logger = LoggerFactory.getLogger(SchedulerClock.class);

    private final ScheduledExecutorService timer;

    private final EventBus eventBus;

    @Inject
    public SchedulerClock(EventBus eventBus) {
        this.eventBus = eventBus;
        this.timer = new ScheduledThreadPoolExecutor(1,
                new ThreadFactoryBuilder().setNameFormat("kun-scheduler-clock").build());
    }

    public void start() {
        long initDelay = 60000 - System.currentTimeMillis() % 60000 + 10;
        timer.scheduleAtFixedRate(this::emitTick, initDelay, 60000, TimeUnit.MILLISECONDS);
        logger.debug("Initial delay is {} milliseconds.", initDelay);
    }

    private void emitTick() {
        OffsetDateTime now = DateTimeUtils.now();
        try {
            TickEvent event = new TickEvent(new Tick(now));
            logger.debug("Post TickEvent to TaskFactory. event={}", event);
            eventBus.post(event);
        } catch (Exception e) {
            logger.error("Failed to emit TickEvent. Continue. now={}", now, e);
        }
    }
}
