package com.miotech.kun.workflow.core.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.metadata.core.model.dataset.DataStore;

import java.util.List;

public class LineageEvent extends PrivateEvent {
    private long taskId;
    private List<DataStore> inlets;
    private List<DataStore> outlets;

    @JsonCreator
    public LineageEvent(@JsonProperty("timestamp") long timestamp,
                        @JsonProperty("taskId") long taskId,
                        @JsonProperty("inlets") List<DataStore> inlets,
                        @JsonProperty("outlets") List<DataStore> outlets) {
        super(timestamp);
        this.taskId = taskId;
        this.inlets = inlets;
        this.outlets = outlets;
    }
    public LineageEvent(long taskId, List<DataStore> inlets, List<DataStore> outlets) {
        this.taskId = taskId;
        this.inlets = inlets;
        this.outlets = outlets;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public List<DataStore> getInlets() {
        return inlets;
    }

    public void setInlets(List<DataStore> inlets) {
        this.inlets = inlets;
    }

    public List<DataStore> getOutlets() {
        return outlets;
    }

    public void setOutlets(List<DataStore> outlets) {
        this.outlets = outlets;
    }

}
