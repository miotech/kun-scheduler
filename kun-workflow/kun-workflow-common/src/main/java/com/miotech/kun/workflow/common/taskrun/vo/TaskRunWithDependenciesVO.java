package com.miotech.kun.workflow.common.taskrun.vo;

import java.util.List;

public class TaskRunWithDependenciesVO {

    private TaskRunVO taskRun;

    private List<TaskRunVO> upstreamTaskRuns;

    private List<TaskRunVO> downstreamTaskRuns;


    public TaskRunWithDependenciesVO(TaskRunVO taskRun, List<TaskRunVO> upstreamTaskRuns, List<TaskRunVO> downstreamTaskRuns) {
        this.taskRun = taskRun;
        this.upstreamTaskRuns = upstreamTaskRuns;
        this.downstreamTaskRuns = downstreamTaskRuns;
    }

    public TaskRunVO getTaskRun() {
        return taskRun;
    }

    public void setTaskRun(TaskRunVO taskRun) {
        this.taskRun = taskRun;
    }

    public List<TaskRunVO> getUpstreamTaskRuns() {
        return upstreamTaskRuns;
    }

    public void setUpstreamTaskRuns(List<TaskRunVO> upstreamTaskRuns) {
        this.upstreamTaskRuns = upstreamTaskRuns;
    }

    public List<TaskRunVO> getDownstreamTaskRuns() {
        return downstreamTaskRuns;
    }

    public void setDownstreamTaskRuns(List<TaskRunVO> downstreamTaskRuns) {
        this.downstreamTaskRuns = downstreamTaskRuns;
    }
}
