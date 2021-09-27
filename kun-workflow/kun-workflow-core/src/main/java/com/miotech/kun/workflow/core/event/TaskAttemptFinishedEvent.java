package com.miotech.kun.workflow.core.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.commons.pubsub.event.PublicEvent;
import com.miotech.kun.metadata.core.model.dataset.DataStore;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;

import java.util.List;

public class TaskAttemptFinishedEvent extends PublicEvent {
    private final Long attemptId;

    private final Long taskId;

    private final Long taskRunId;

    private final TaskRunStatus finalStatus;

    private final List<DataStore> inlets;

    private final List<DataStore> outlets;


    public TaskAttemptFinishedEvent(Long attemptId,Long taskId,Long taskRunId, TaskRunStatus finalStatus, List<DataStore> inlets, List<DataStore> outlets) {
        this.attemptId = attemptId;
        this.taskId = taskId;
        this.taskRunId = taskRunId;
        this.finalStatus = finalStatus;
        this.inlets = inlets;
        this.outlets = outlets;
    }

    @JsonCreator
    public TaskAttemptFinishedEvent(@JsonProperty("timestamp") long timestamp,
                                    @JsonProperty("attemptId") Long attemptId,
                                    @JsonProperty("taskId") Long taskId,
                                    @JsonProperty("taskRunId") Long taskRunId,
                                    @JsonProperty("finalStatus") TaskRunStatus finalStatus,
                                    @JsonProperty("inlets") List<DataStore> inlets,
                                    @JsonProperty("outlets") List<DataStore> outlets) {
        super(timestamp);
        this.attemptId = attemptId;
        this.taskId = taskId;
        this.taskRunId = taskRunId;
        this.finalStatus = finalStatus;
        this.inlets = inlets;
        this.outlets = outlets;
    }

    public Long getAttemptId() {
        return attemptId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public Long getTaskRunId() {
        return taskRunId;
    }

    public TaskRunStatus getFinalStatus() {
        return finalStatus;
    }

    public List<DataStore> getInlets() {
        return inlets;
    }

    public List<DataStore> getOutlets() {
        return outlets;
    }


    @Override
    public String toString() {
        return "TaskAttemptFinishedEvent{" +
                "attemptId=" + attemptId +
                ", taskId=" + taskId +
                ", taskRunId=" + taskRunId +
                ", finalStatus=" + finalStatus +
                ", inlets=" + inlets +
                ", outlets=" + outlets +
                '}';
    }
}
