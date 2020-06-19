package com.miotech.kun.workflow;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.workflow.common.graph.DatabaseTaskGraph;
import com.miotech.kun.workflow.core.Scheduler;
import com.miotech.kun.workflow.core.model.task.TaskGraph;
import com.miotech.kun.workflow.scheduler.SchedulerClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class SchedulerManager {
    private static final Logger logger = LoggerFactory.getLogger(SchedulerManager.class);

    @Inject
    private Scheduler scheduler;

    @Inject
    private TaskGraph taskGraph;

    @Inject
    private SchedulerClock schedulerClock;

    public void start() {
        logger.info("Start SchedulerManager using LocalScheduler");
        schedulerClock.start();
        scheduler.schedule(taskGraph);
    }
}
