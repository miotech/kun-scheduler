package com.miotech.kun.workflow.core.model.lineage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class UpstreamTaskBasicInformation {

    private Long datasetGid;

    private List<Long> taskIds;

    @JsonCreator
    public UpstreamTaskBasicInformation(@JsonProperty("datasetGid") Long datasetGid, @JsonProperty("taskIds") List<Long> taskIds) {
        this.datasetGid = datasetGid;
        this.taskIds = taskIds;
    }

    public Long getDatasetGid() {
        return datasetGid;
    }

    public void setDatasetGid(Long datasetGid) {
        this.datasetGid = datasetGid;
    }

    public List<Long> getTaskIds() {
        return taskIds;
    }

    public void setTaskIds(List<Long> taskIds) {
        this.taskIds = taskIds;
    }
}
