package com.miotech.kun.workflow;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.miotech.kun.workflow.common.graph.DatabaseTaskGraph;
import com.miotech.kun.workflow.core.Scheduler;
import com.miotech.kun.workflow.core.model.task.TaskGraph;

public class SchedulerModule extends AbstractModule {

    @Override
    public void configure() {
        bind(EventBus.class).toInstance(new EventBus());
        bind(Scheduler.class).to(LocalScheduler.class);
        bind(TaskGraph.class).to(DatabaseTaskGraph.class);
    }

}
