package com.miotech.kun.workflow.core.model.lineage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.workflow.core.model.lineage.node.DatasetInfo;

import java.util.Set;

public class TaskOutlets {

    private final Set<DatasetInfo> outlets;

    @JsonCreator
    public TaskOutlets(@JsonProperty("outlets") Set<DatasetInfo> outlets) {
        this.outlets = outlets;
    }

    public Set<DatasetInfo> getOutlets() {
        return outlets;
    }

}
