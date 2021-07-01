package com.miotech.kun.workflow.core.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.metadata.core.model.dataset.DataStore;

import java.util.List;

public class LineageEvent extends Event {
    private long taskId;
    private List<DataStore> inlets;
    private List<DataStore> outlets;

//    public LineageEvent(@JsonProperty("taskId") long taskId,
//                        @JsonProperty("inlets") List<DataStore> inlets,
//                        @JsonProperty("outlets") List<DataStore> outlets) {
//        this.taskId = taskId;
//        this.inlets = inlets;
//        this.outlets = outlets;
//    }

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

//    public LineageEvent(long timestamp, long taskId, List<DataStore> inlets, List<DataStore> outlets) {
//        super(timestamp);
//        this.taskId = taskId;
//        this.inlets = inlets;
//        this.outlets = outlets;
//    }

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


    public static final class LineageEventBuilder {
        private long taskId;
        private List<DataStore> inlets;
        private List<DataStore> outlets;
        private long timestamp;

        private LineageEventBuilder() {
        }

        public static LineageEventBuilder aLineageEvent() {
            return new LineageEventBuilder();
        }

        public LineageEventBuilder withTaskId(long taskId) {
            this.taskId = taskId;
            return this;
        }

        public LineageEventBuilder withInlets(List<DataStore> inlets) {
            this.inlets = inlets;
            return this;
        }

        public LineageEventBuilder withOutlets(List<DataStore> outlets) {
            this.outlets = outlets;
            return this;
        }

        public LineageEventBuilder withTimestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public LineageEvent build() {
            return new LineageEvent(timestamp, taskId, inlets, outlets);
        }
    }
}
