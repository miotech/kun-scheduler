package com.miotech.kun.workflow.core.model.lineage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class UpstreamTaskInformation {

    private Long datasetGid;

    private List<TaskInformation> taskInfos;

    @JsonCreator
    public UpstreamTaskInformation(@JsonProperty("datasetGid") Long datasetGid, @JsonProperty("taskInfos") List<TaskInformation> taskInfos) {
        this.datasetGid = datasetGid;
        this.taskInfos = taskInfos;
    }

    public Long getDatasetGid() {
        return datasetGid;
    }

    public List<TaskInformation> getTaskInfos() {
        return taskInfos;
    }

    public static class TaskInformation {
        private Long id;

        private String name;

        private String description;

        private String scheduleMethod;

        @JsonCreator
        public TaskInformation(@JsonProperty("id") Long id, @JsonProperty("name") String name,
                               @JsonProperty("description") String description, @JsonProperty("scheduleMethod") String scheduleMethod) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.scheduleMethod = scheduleMethod;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getScheduleMethod() {
            return scheduleMethod;
        }

        public void setScheduleMethod(String scheduleMethod) {
            this.scheduleMethod = scheduleMethod;
        }
    }

}
