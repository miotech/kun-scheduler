package com.miotech.kun.workflow;

import com.miotech.kun.workflow.core.Scheduler;
import com.miotech.kun.workflow.core.model.common.Environment;
import com.miotech.kun.workflow.core.model.task.TaskGraph;
import com.miotech.kun.workflow.scheduler.TaskSpawner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
public class LocalScheduler implements Scheduler {
    private static final Logger logger = LoggerFactory.getLogger(LocalScheduler.class);

    @Inject
    private TaskSpawner taskSpawner;

    @Override
    public void schedule(TaskGraph graph) {
        checkNotNull(graph, "graph should not be null.");

        logger.info("schedule a graph {}", graph);
        taskSpawner.add(graph);
    }

    @Override
    public void run(TaskGraph graph, Environment environment) {
        // TODO: implement this method
    }
}
