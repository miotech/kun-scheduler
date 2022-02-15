package com.miotech.kun.workflow.core.model.lineage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.workflow.core.model.task.Task;

import java.util.List;

public class UpstreamTaskInformation {

    private Long datasetGid;

    private List<Task> tasks;

    @JsonCreator
    public UpstreamTaskInformation(@JsonProperty("datasetGid") Long datasetGid, @JsonProperty("tasks") List<Task> tasks) {
        this.datasetGid = datasetGid;
        this.tasks = tasks;
    }

    public Long getDatasetGid() {
        return datasetGid;
    }

    public void setDatasetGid(Long datasetGid) {
        this.datasetGid = datasetGid;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }
}
