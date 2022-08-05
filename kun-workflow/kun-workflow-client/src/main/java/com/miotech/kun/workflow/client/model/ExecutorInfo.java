package com.miotech.kun.workflow.client.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.miotech.kun.workflow.core.model.resource.ResourceQueue;

import java.util.List;
import java.util.Map;

@JsonDeserialize(builder = ExecutorInfo.Builder.class)
public class ExecutorInfo {

    private String kind;
    private String name;
    private List<String> labels;
    private List<ResourceQueue> resourceQueues;
    private Map<String, ExecutorInfo> extraInfo;


    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public List<ResourceQueue> getResourceQueues() {
        return resourceQueues;
    }

    public void setResourceQueues(List<ResourceQueue> resourceQueues) {
        this.resourceQueues = resourceQueues;
    }

    public Map<String, ExecutorInfo> getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(Map<String, ExecutorInfo> extraInfo) {
        this.extraInfo = extraInfo;
    }

    public static final class Builder {
        private String kind;
        private String name;
        private List<String> labels;
        private List<ResourceQueue> resourceQueues;
        private Map<String, ExecutorInfo> extraInfo;

        private Builder() {
        }

        public static Builder anExecutorInfo() {
            return new Builder();
        }

        public Builder withKind(String kind) {
            this.kind = kind;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withLabels(List<String> labels) {
            this.labels = labels;
            return this;
        }

        public Builder withResourceQueues(List<ResourceQueue> resourceQueues) {
            this.resourceQueues = resourceQueues;
            return this;
        }

        public Builder withExtraInfo(Map<String, ExecutorInfo> extraInfo) {
            this.extraInfo = extraInfo;
            return this;
        }

        public ExecutorInfo build() {
            ExecutorInfo executorInfo = new ExecutorInfo();
            executorInfo.setKind(kind);
            executorInfo.setName(name);
            executorInfo.setLabels(labels);
            executorInfo.setResourceQueues(resourceQueues);
            executorInfo.setExtraInfo(extraInfo);
            return executorInfo;
        }
    }
}
