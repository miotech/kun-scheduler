package com.miotech.kun.workflow.client.model;

import java.util.List;

public class TaskWithDependencies {

    private List<Task> tasks;

    private List<TaskDependency> dependencies;

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public List<TaskDependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<TaskDependency> dependencies) {
        this.dependencies = dependencies;
    }
}
