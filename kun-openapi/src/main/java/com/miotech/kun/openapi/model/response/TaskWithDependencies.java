package com.miotech.kun.openapi.model.response;

import java.util.List;

public class TaskWithDependencies {
    private TaskVO task;

    private List<TaskVO> upstreamTasks;

    private List<TaskVO> downstreamTasks;

    public TaskWithDependencies(TaskVO task, List<TaskVO> upstreamTasks, List<TaskVO> downstreamTasks) {
        this.task = task;
        this.upstreamTasks = upstreamTasks;
        this.downstreamTasks = downstreamTasks;
    }

    public TaskVO getTask() {
        return task;
    }

    public void setTask(TaskVO task) {
        this.task = task;
    }

    public List<TaskVO> getUpstreamTasks() {
        return upstreamTasks;
    }

    public void setUpstreamTasks(List<TaskVO> upstreamTasks) {
        this.upstreamTasks = upstreamTasks;
    }

    public List<TaskVO> getDownstreamTasks() {
        return downstreamTasks;
    }

    public void setDownstreamTasks(List<TaskVO> downstreamTasks) {
        this.downstreamTasks = downstreamTasks;
    }
}
