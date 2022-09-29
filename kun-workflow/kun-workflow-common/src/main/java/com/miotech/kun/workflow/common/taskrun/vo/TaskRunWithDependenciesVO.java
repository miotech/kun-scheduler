package com.miotech.kun.workflow.common.taskrun.vo;

import java.util.List;

public class TaskRunWithDependenciesVO {

    private List<TaskRunVO> taskRuns;

    private List<TaskRunDependencyVO> dependencies;

    public TaskRunWithDependenciesVO(List<TaskRunVO> taskRuns, List<TaskRunDependencyVO> dependencies) {
        this.taskRuns = taskRuns;
        this.dependencies = dependencies;
    }

    public List<TaskRunVO> getTaskRuns() {
        return taskRuns;
    }

    public void setTaskRuns(List<TaskRunVO> taskRuns) {
        this.taskRuns = taskRuns;
    }

    public List<TaskRunDependencyVO> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<TaskRunDependencyVO> dependencies) {
        this.dependencies = dependencies;
    }
}
