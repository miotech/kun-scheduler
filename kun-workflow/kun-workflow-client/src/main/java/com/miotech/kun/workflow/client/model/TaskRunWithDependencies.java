package com.miotech.kun.workflow.client.model;

import java.util.List;

public class TaskRunWithDependencies {

    private List<TaskRun> taskRuns;

    private List<TaskRunDependency> dependencies;

    public List<TaskRun> getTaskRuns() {
        return taskRuns;
    }

    public void setTaskRuns(List<TaskRun> taskRuns) {
        this.taskRuns = taskRuns;
    }

    public List<TaskRunDependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<TaskRunDependency> dependencies) {
        this.dependencies = dependencies;
    }
}
