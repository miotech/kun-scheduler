package com.miotech.kun.workflow.client.model;

import java.util.List;

public class TaskDAG {

    private List<Task> nodes;

    private List<TaskDependency> edges;

    public List<Task> getNodes() {
        return nodes;
    }

    public void setNodes(List<Task> nodes) {
        this.nodes = nodes;
    }

    public List<TaskDependency> getEdges() {
        return edges;
    }

    public void setEdges(List<TaskDependency> edges) {
        this.edges = edges;
    }
}
