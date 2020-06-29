package com.miotech.kun.workflow.core.event;

import com.miotech.kun.workflow.core.model.lineage.DataStore;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;

import java.util.List;

public class TaskAttemptFinishedEvent extends Event {
    private final Long attemptId;

    private final TaskRunStatus finalStatus;

    private final List<DataStore> inlets;

    private final List<DataStore> outlets;

    public TaskAttemptFinishedEvent(Long attemptId, TaskRunStatus finalStatus, List<DataStore> inlets, List<DataStore> outlets) {
        this.attemptId = attemptId;
        this.finalStatus = finalStatus;
        this.inlets = inlets;
        this.outlets = outlets;
    }

    public Long getAttemptId() {
        return attemptId;
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
}
