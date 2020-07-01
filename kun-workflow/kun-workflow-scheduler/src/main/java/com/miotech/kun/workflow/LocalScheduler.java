package com.miotech.kun.workflow;

import com.miotech.kun.workflow.core.Scheduler;
import com.miotech.kun.workflow.core.model.task.RunTaskContext;
import com.miotech.kun.workflow.core.model.task.TaskGraph;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.scheduler.TaskSpawner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class LocalScheduler implements Scheduler {
    private static final Logger logger = LoggerFactory.getLogger(LocalScheduler.class);

    @Inject
    private TaskSpawner taskSpawner;

    @Override
    public void schedule(TaskGraph graph) {
        logger.info("schedule a graph {}", graph);
        taskSpawner.schedule(graph);
    }

    @Override
    public List<TaskRun> run(TaskGraph graph, RunTaskContext context) {
        logger.info("run graph {} with variables {}", graph, context);
        return taskSpawner.run(graph, context);
    }
}
