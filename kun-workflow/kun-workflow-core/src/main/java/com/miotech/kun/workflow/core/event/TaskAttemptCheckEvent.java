package com.miotech.kun.workflow.core.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.commons.pubsub.event.PublicEvent;
import com.miotech.kun.metadata.core.model.dataset.DataStore;

import java.util.List;

public class TaskAttemptCheckEvent extends PublicEvent {

    private final Long taskAttemptId;
    private final Long taskRunId;
    private final List<DataStore> inlets;
    private final List<DataStore> outlets;
    private final List<Long> inDataSetIds;
    private final List<Long> outDataSetIds;

    public TaskAttemptCheckEvent(Long taskAttemptId, Long taskRunId, List<DataStore> inlets, List<DataStore> outlets, List<Long> inDataSetIds, List<Long> outDataSetIds) {
        this.taskAttemptId = taskAttemptId;
        this.taskRunId = taskRunId;
        this.inlets = inlets;
        this.outlets = outlets;
        this.inDataSetIds = inDataSetIds;
        this.outDataSetIds = outDataSetIds;
    }

    @JsonCreator
    public TaskAttemptCheckEvent(@JsonProperty("timestamp") long timestamp,
                                 @JsonProperty("attemptId") Long taskAttemptId,
                                 @JsonProperty("taskRunId") Long taskRunId,
                                 @JsonProperty("inlets") List<DataStore> inlets,
                                 @JsonProperty("outlets") List<DataStore> outlets,
                                 @JsonProperty("inDataSetIds") List<Long> inDataSetIds,
                                 @JsonProperty("outDataSetIds") List<Long> outDataSetIds) {
        super(timestamp);
        this.taskAttemptId = taskAttemptId;
        this.taskRunId = taskRunId;
        this.inlets = inlets;
        this.outlets = outlets;
        this.inDataSetIds = inDataSetIds;
        this.outDataSetIds = outDataSetIds;
    }

    public Long getTaskAttemptId() {
        return taskAttemptId;
    }

    public Long getTaskRunId() {
        return taskRunId;
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
}
