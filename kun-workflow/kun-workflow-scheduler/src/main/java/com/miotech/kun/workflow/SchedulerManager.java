package com.miotech.kun.workflow;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.workflow.common.tick.TickDao;
import com.miotech.kun.workflow.core.Scheduler;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.task.TaskGraph;
import com.miotech.kun.workflow.scheduler.SchedulerClock;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;

@Singleton
public class SchedulerManager {
    private static final Logger logger = LoggerFactory.getLogger(SchedulerManager.class);

    @Inject
    private Scheduler scheduler;

    @Inject
    private TaskGraph taskGraph;

    @Inject
    private SchedulerClock schedulerClock;

    @Inject
    private TickDao tickDao;

    private final Long RECOVER_LIMIT = 30l;

    public void start() {
        logger.info("Start SchedulerManager using LocalScheduler");
        Tick checkPoint = tickDao.getLatestCheckPoint();
        if (checkPoint != null) {
            OffsetDateTime now = DateTimeUtils.now();
            OffsetDateTime recoverStartTime = now.plusMinutes(0 - RECOVER_LIMIT);
            OffsetDateTime checkPointTime = checkPoint.toOffsetDateTime();
            if(checkPointTime.compareTo(recoverStartTime) > 0){
                recoverStartTime = checkPointTime;
            }
            logger.info("start clock checkPoint = {}", recoverStartTime);
            schedulerClock.start(recoverStartTime);
        } else {
            logger.info("start clock");
            schedulerClock.start();
        }
        scheduler.schedule(taskGraph);
    }
}
