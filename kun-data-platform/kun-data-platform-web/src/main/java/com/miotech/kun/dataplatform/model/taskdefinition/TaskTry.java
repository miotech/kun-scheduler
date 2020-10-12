package com.miotech.kun.dataplatform.model.taskdefinition;

import org.json.simple.JSONObject;

public class TaskTry {

    private final Long id;

    private final Long definitionId;

    private final Long workflowTaskId;

    private final Long workflowTaskRunId;

    private final Long creator;

    private final JSONObject taskConfig;

    private TaskTry(Builder builder) {
        this.id = builder.id;
        this.definitionId = builder.definitionId;
        this.workflowTaskId = builder.workflowTaskId;
        this.workflowTaskRunId = builder.workflowTaskRunId;
        this.creator = builder.creator;
        this.taskConfig = builder.taskConfig;
    }

    public static Builder newTaskTry() {
        return new Builder();
    }


    public Long getId() {
        return id;
    }

    public Long getDefinitionId() {
        return definitionId;
    }

    public Long getWorkflowTaskId() {
        return workflowTaskId;
    }

    public Long getCreator() {
        return creator;
    }

    public JSONObject getTaskConfig() {
        return taskConfig;
    }

    public Long getWorkflowTaskRunId() {
        return workflowTaskRunId;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private Long id;
        private Long definitionId;
        private Long workflowTaskId;
        private Long workflowTaskRunId;
        private Long creator;
        private JSONObject taskConfig;

        private Builder() {
        }

        public TaskTry build() {
            return new TaskTry(this);
        }

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withDefinitionId(Long definitionId) {
            this.definitionId = definitionId;
            return this;
        }

        public Builder withWorkflowTaskId(Long workflowTaskId) {
            this.workflowTaskId = workflowTaskId;
            return this;
        }

        public Builder withWorkflowTaskRunId(Long workflowTaskRunId) {
            this.workflowTaskRunId = workflowTaskRunId;
            return this;
        }

        public Builder withCreator(Long creator) {
            this.creator = creator;
            return this;
        }

        public Builder withTaskConfig(JSONObject taskConfig) {
            this.taskConfig = taskConfig;
            return this;
        }
    }
}
