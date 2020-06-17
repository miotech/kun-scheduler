package com.miotech.kun.workflow.common.graph;

import com.google.common.collect.ImmutableList;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.task.TaskGraph;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class DirectTaskGraph implements TaskGraph {
    private final List<Task> tasks;
    private final AtomicBoolean consumed;

    public DirectTaskGraph(List<Task> tasks) {
        this.tasks = ImmutableList.copyOf(tasks);
        this.consumed = new AtomicBoolean(false);
    }

    @Override
    public List<Task> tasksScheduledAt(Tick tick) {
        if (consumed.compareAndSet(false, true)) {
            return tasks;
        } else {
            return Collections.emptyList();
        }
    }
}
