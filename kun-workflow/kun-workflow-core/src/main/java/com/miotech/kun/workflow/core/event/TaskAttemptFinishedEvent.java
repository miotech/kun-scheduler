package com.miotech.kun.workflow.core.event;

import com.miotech.kun.metadata.core.model.DataStore;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;

import java.util.List;

public class TaskAttemptFinishedEvent extends Event {
    private final Long attemptId;

    private final TaskRunStatus finalStatus;

    private final List<DataStore> inlets;

    private final List<DataStore> outlets;

    private final List<Long> inDataSetIds;

    private final List<Long> outDataSetIds;

    public TaskAttemptFinishedEvent(Long attemptId, TaskRunStatus finalStatus, List<DataStore> inlets, List<DataStore> outlets,
                                    List<Long> inDataSetIds,List<Long> outDataSetIds) {
        this.attemptId = attemptId;
        this.finalStatus = finalStatus;
        this.inlets = inlets;
        this.outlets = outlets;
        this.inDataSetIds = inDataSetIds;
        this.outDataSetIds = outDataSetIds;
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

    public List<Long> getInDataSetIds() {
        return inDataSetIds;
    }

    public List<Long> getOutDataSetIds() {
        return outDataSetIds;
    }

    @Override
    public String toString() {
        return "TaskAttemptFinishedEvent{" +
                "attemptId=" + attemptId +
                ", finalStatus=" + finalStatus +
                ", inlets=" + inlets +
                ", outlets=" + outlets +
                ", inDataSetIds=" + inDataSetIds +
                ", outDataSetIds=" + outDataSetIds +
                '}';
    }
}
