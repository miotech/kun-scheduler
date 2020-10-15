package com.miotech.kun.workflow.core.execution;

import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;

import java.io.Serializable;
import java.time.OffsetDateTime;

public class TaskAttemptMsg implements Serializable,Cloneable {
    private long taskAttemptId;
    private long workerId;
    private long taskRunId;
    private TaskRunStatus taskRunStatus;
    private OffsetDateTime startAt;
    private OffsetDateTime endAt;
    private OperatorReport operatorReport;
//    private TaskAttemptReport taskAttemptReport;

    private static final long serialVersionUID = 1603849040000l;

//    public TaskAttemptReport getTaskAttemptReport() {
//        return taskAttemptReport;
//    }

    public long getTaskAttemptId() {
        return taskAttemptId;
    }

    public TaskRunStatus getTaskRunStatus() {
        return taskRunStatus;
    }

    public OffsetDateTime getStartAt() {
        return startAt;
    }

    public OffsetDateTime getEndAt() {
        return endAt;
    }

    public OperatorReport getOperatorReport() {
        return operatorReport;
    }

    public void setOperatorReport(OperatorReport operatorReport) {
        this.operatorReport = operatorReport;
    }

    public void setTaskAttemptId(long taskAttemptId) {
        this.taskAttemptId = taskAttemptId;
    }

    public void setTaskRunStatus(TaskRunStatus taskRunStatus) {
        this.taskRunStatus = taskRunStatus;
    }

    public void setStartAt(OffsetDateTime startAt) {
        this.startAt = startAt;
    }

    public void setEndAt(OffsetDateTime endAt) {
        this.endAt = endAt;
    }

    public long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(long workerId) {
        this.workerId = workerId;
    }

//    public void setTaskAttemptReport(TaskAttemptReport taskAttemptReport) {
//        this.taskAttemptReport = taskAttemptReport;
//    }

    public long getTaskRunId() {
        return taskRunId;
    }

    public void setTaskRunId(long taskRunId) {
        this.taskRunId = taskRunId;
    }

    public boolean isSuccess(){
        return taskRunStatus != null ? taskRunStatus.isSuccess():false;
    }

    @Override
    public TaskAttemptMsg clone(){
        try {
            TaskAttemptMsg newTaskAttemptMsg = (TaskAttemptMsg)super.clone();
            return newTaskAttemptMsg;
        }catch (CloneNotSupportedException e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return "TaskAttemptMsg{" +
                "taskAttemptId=" + taskAttemptId +
                ", workerId=" + workerId +
                ", taskRunId=" + taskRunId +
                ", taskRunStatus=" + taskRunStatus +
                ", startAt=" + startAt +
                ", endAt=" + endAt +
                '}';
    }
}
