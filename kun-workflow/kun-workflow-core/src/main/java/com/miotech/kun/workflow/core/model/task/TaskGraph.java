package com.miotech.kun.workflow.core.model.task;

import com.miotech.kun.workflow.core.model.common.Tick;

import java.util.List;

public interface TaskGraph {
    public List<Task> tasksScheduledAt(Tick tick);
}
