package com.miotech.kun.workflow.core;

import com.miotech.kun.workflow.core.model.common.Environment;
import com.miotech.kun.workflow.core.model.task.TaskGraph;

public interface Scheduler {
    public void schedule(TaskGraph graph);

    public void run(TaskGraph graph, Environment environment);
}
