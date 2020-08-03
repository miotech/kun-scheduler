package com.miotech.kun.workflow.common.task.vo;

import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.task.TaskDependency;

import java.util.List;

public class TaskDAGVO {
    private final List<Task> nodes;

    private final List<TaskDependency> edges;

    public TaskDAGVO(List<Task> nodes, List<TaskDependency> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public List<Task> getNodes() {
        return nodes;
    }

    public List<TaskDependency> getEdges() {
        return edges;
    }
}
