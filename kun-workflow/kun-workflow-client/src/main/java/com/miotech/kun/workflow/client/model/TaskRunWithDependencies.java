package com.miotech.kun.workflow.client.model;

import java.util.List;

public class TaskRunWithDependencies {

    private TaskRun taskRun;

    private List<TaskRun> upstreamTaskRuns;

    private List<TaskRun> downstreamTaskRuns;

    public TaskRun getTaskRun() {
        return taskRun;
    }

    public void setTaskRun(TaskRun taskRun) {
        this.taskRun = taskRun;
    }

    public List<TaskRun> getUpstreamTaskRuns() {
        return upstreamTaskRuns;
    }

    public void setUpstreamTaskRuns(List<TaskRun> upstreamTaskRuns) {
        this.upstreamTaskRuns = upstreamTaskRuns;
    }

    public List<TaskRun> getDownstreamTaskRuns() {
        return downstreamTaskRuns;
    }

    public void setDownstreamTaskRuns(List<TaskRun> downstreamTaskRuns) {
        this.downstreamTaskRuns = downstreamTaskRuns;
    }
}
