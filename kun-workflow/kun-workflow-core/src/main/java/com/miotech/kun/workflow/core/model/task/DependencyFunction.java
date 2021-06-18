package com.miotech.kun.workflow.core.model.task;

import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;

import java.util.List;

public interface DependencyFunction {
    public TaskRun resolveDependency(Task self, Long upstreamTaskId, Tick tick, List<TaskRun> others);

    public String toFunctionType();
}
