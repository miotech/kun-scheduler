package com.miotech.kun.workflow.common.task.vo;

import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.task.TaskDependency;

import java.util.List;

public class TaskWithDependenciesVO {

    private List<Task> tasks;

    private List<TaskDependency> dependencies;

    public TaskWithDependenciesVO(List<Task> tasks, List<TaskDependency> dependencies) {
        this.tasks = tasks;
        this.dependencies = dependencies;
    }

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
