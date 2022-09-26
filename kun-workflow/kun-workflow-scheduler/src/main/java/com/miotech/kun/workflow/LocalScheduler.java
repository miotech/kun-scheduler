package com.miotech.kun.workflow;

import com.miotech.kun.workflow.core.Scheduler;
import com.miotech.kun.workflow.core.model.task.TaskRunEnv;
import com.miotech.kun.workflow.core.model.task.TaskGraph;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.scheduler.TaskManager;
import com.miotech.kun.workflow.scheduler.TaskSpawner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.OffsetDateTime;
import java.util.List;

@Singleton
public class LocalScheduler implements Scheduler {
    private static final Logger logger = LoggerFactory.getLogger(LocalScheduler.class);

    @Inject
    private TaskSpawner taskSpawner;

    @Inject
    private TaskManager taskManager;

    @Override
    public void schedule(TaskGraph graph) {
        logger.info("schedule a graph {}", graph);
        taskSpawner.schedule(graph);
    }

    @Override
    public List<TaskRun> run(TaskGraph graph, TaskRunEnv env) {
        logger.info("run graph {} with env {}", graph, env);
        return taskSpawner.run(graph, env);
    }

    @Override
    public List<TaskRun> run(TaskGraph graph, TaskRunEnv env, OffsetDateTime scheduleTime) {
        logger.info("run graph {} with env {}", graph, env);
        return taskSpawner.run(graph, env, scheduleTime);
    }

    @Override
    public boolean rerun(TaskRun taskRun) {
        return taskSpawner.rerun(taskRun);
    }

    @Override
    public boolean skip(TaskRun taskRun) {
        return taskManager.skip(taskRun);
    }

    @Override
    public void trigger() {
        //do nothing
    }

    @Override
    public boolean removeDependency(Long taskRunId, List<Long> upstreamTaskRunIds) {
        return taskManager.batchRemoveDependency(taskRunId, upstreamTaskRunIds);
    }

    @Override
    public boolean abort(Long taskRunId) {
        return taskManager.abort(taskRunId);
    }
}
