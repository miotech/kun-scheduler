package com.miotech.kun.workflow.core;

import com.miotech.kun.workflow.core.model.task.TaskRunEnv;
import com.miotech.kun.workflow.core.model.task.TaskGraph;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;

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
     * 重新运行一个taskRun
     */
    public boolean rerun(TaskRun taskRun);
}
