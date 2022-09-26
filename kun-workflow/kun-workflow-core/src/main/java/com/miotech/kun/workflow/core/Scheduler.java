package com.miotech.kun.workflow.core;

import com.miotech.kun.workflow.core.model.task.TaskRunEnv;
import com.miotech.kun.workflow.core.model.task.TaskGraph;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;

import java.time.OffsetDateTime;
import java.util.List;

public interface Scheduler {
    /**
     * 对一个Graph进行定时调度。
     * @param graph
     */
    public void schedule(TaskGraph graph);

    /**
     * 立刻运行一个Graph。
     * @param graph
     * @param context
     */
    public List<TaskRun> run(TaskGraph graph, TaskRunEnv context);

    /**
     * Run a task graph instantly at the scheduleTime
     * @param graph
     * @param context
     * @param scheduleTime
     * @return
     */
    public List<TaskRun> run(TaskGraph graph, TaskRunEnv context, OffsetDateTime scheduleTime);

    /**
     * 重新运行一个taskRun
     */
    public boolean rerun(TaskRun taskRun);

    /**
     * skip a taskrun
     */
    public boolean skip(TaskRun taskRun);

    /**
     * trigger runnable taskRun to start
     */
    public void trigger();

    /**
     * remove taskrun's selected upstream taskrun dependency
     */
    public boolean removeDependency(Long taskRunId, List<Long> upstreamTaskRunIds);

    /**
     * abort a taskrun
     * @param taskRunId
     * @return
     */
    public boolean abort(Long taskRunId);
}
