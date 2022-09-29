package com.miotech.kun.openapi.model.response;

import java.util.List;

public class TaskVOWithDependencies {

    private List<TaskVO> tasks;

    private List<TaskDependencyVO> dependencies;


    public TaskVOWithDependencies(List<TaskVO> tasks, List<TaskDependencyVO> dependencies) {
        this.tasks = tasks;
        this.dependencies = dependencies;
    }

    public List<TaskVO> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskVO> tasks) {
        this.tasks = tasks;
    }

    public List<TaskDependencyVO> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<TaskDependencyVO> dependencies) {
        this.dependencies = dependencies;
    }
}
