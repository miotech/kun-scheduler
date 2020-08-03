package com.miotech.kun.workflow.client.model;

import java.util.List;

public class TaskRunDAG {

    private List<TaskRun> nodes;

    private List<TaskRunDependency> edges;

    public List<TaskRun> getNodes() {
        return nodes;
    }

    public void setNodes(List<TaskRun> nodes) {
        this.nodes = nodes;
    }

    public List<TaskRunDependency> getEdges() {
        return edges;
    }

    public void setEdges(List<TaskRunDependency> edges) {
        this.edges = edges;
    }
}
