package com.miotech.kun.workflow.common.graph;

import com.google.common.collect.ImmutableList;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.task.TaskDependency;
import com.miotech.kun.workflow.core.model.task.TaskGraph;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DirectTaskGraph implements TaskGraph {
    private final List<Task> tasks;
    private final AtomicBoolean consumed;

    public DirectTaskGraph(Task... tasks) {
        this(Arrays.asList(tasks));
    }

    public DirectTaskGraph(List<Task> tasks) {
        validateTasks(tasks);
        this.tasks = ImmutableList.copyOf(tasks);
        this.consumed = new AtomicBoolean(false);
    }

    @Override
    public List<Task> tasksScheduledAt(Tick tick) {
        if (consumed.compareAndSet(false, true)) {
            return resolveMutualDependencies(tasks);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public void updateTasksNextExecutionTick(Tick tick, List<Task> scheduledTasks) {
        return;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    private List<Task> resolveMutualDependencies(List<Task> tasks) {
        Map<Long, Task> lookupTable = tasks.stream().collect(
                Collectors.toMap(Task::getId, Function.identity()));

        List<Task> result = new ArrayList<>(tasks.size());
        for (Task task : tasks) {
            List<TaskDependency> modifiedDependencies = task.getDependencies().stream()
                    .filter(d -> lookupTable.containsKey(d.getUpstreamTaskId()))
                    .collect(Collectors.toList());
            result.add(task.cloneBuilder().withDependencies(modifiedDependencies).build());
        }

        return result;
    }

    private void validateTasks(List<Task> tasks) {
        for (Task t : tasks) {
            if (t.getId() == null) {
                throw new IllegalArgumentException("Task to build DirectTaskGraph must have id. task=" + t);
            }
        }
    }
}
