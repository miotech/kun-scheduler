package com.miotech.kun.workflow.core.execution;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.miotech.kun.metadata.core.model.dataset.DataStore;

import java.io.Serializable;
import java.util.List;

public class TaskAttemptReport implements Serializable {
    public transient static final TaskAttemptReport BLANK = TaskAttemptReport.newBuilder()
            .withInlets(ImmutableList.of())
            .withOutlets(ImmutableList.of())
            .build();

    private final List<DataStore> inlets;
    private final List<DataStore> outlets;
    private static final long serialVersionUID = 1604031071000l;

    @JsonCreator
    public TaskAttemptReport(
            @JsonProperty("inlets") List<DataStore> inlets,
            @JsonProperty("outlets") List<DataStore> outlets) {
        this.inlets = ImmutableList.copyOf(inlets);
        this.outlets = ImmutableList.copyOf(outlets);
    }

    public List<DataStore> getInlets() {
        return inlets;
    }

    public List<DataStore> getOutlets() {
        return outlets;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder implements Serializable{
        private List<DataStore> inlets;
        private List<DataStore> outlets;

        private Builder() {
        }

        public Builder withInlets(List<DataStore> inlets) {
            this.inlets = inlets;
            return this;
        }

        public Builder withOutlets(List<DataStore> outlets) {
            this.outlets = outlets;
            return this;
        }

        public TaskAttemptReport build() {
            return new TaskAttemptReport(inlets, outlets);
        }
    }
}
