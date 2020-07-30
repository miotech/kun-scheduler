package com.miotech.kun.workflow.common.taskrun.vo;

import java.util.List;

public class TaskRunDAGVO {
    private final List<TaskRunVO> nodes;

    private final List<TaskRunDependencyVO> edges;

    public TaskRunDAGVO(List<TaskRunVO> nodes, List<TaskRunDependencyVO> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public List<TaskRunVO> getNodes() {
        return nodes;
    }

    public List<TaskRunDependencyVO> getEdges() {
        return edges;
    }
}
