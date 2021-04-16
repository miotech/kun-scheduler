package com.miotech.kun.dataplatform.model.commit;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskPayload;

@JsonDeserialize(builder = TaskSnapshot.Builder.class)
public class TaskSnapshot {

    private final String name;

    private final String taskTemplateName;

    private final TaskPayload taskPayload;

    private final Long owner;

    public TaskSnapshot(String name,
                        String taskTemplateName,
                        TaskPayload taskPayload,
                        Long owner) {
        this.name = name;
        this.taskTemplateName = taskTemplateName;
        this.taskPayload = taskPayload;
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public String getTaskTemplateName() {
        return taskTemplateName;
    }

    public TaskPayload getTaskPayload() {
        return taskPayload;
    }

    public Long getOwner() {
        return owner;
    }

    public static Builder newBuilder() { return new Builder(); }

    @JsonPOJOBuilder
    public static final class Builder {
        private String name;
        private String taskTemplateName;
        private TaskPayload taskPayload;
        private Long owner;

        private Builder() {
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withTaskTemplateName(String taskTemplateName) {
            this.taskTemplateName = taskTemplateName;
            return this;
        }

        public Builder withTaskPayload(TaskPayload taskPayload) {
            this.taskPayload = taskPayload;
            return this;
        }

        public Builder withOwner(Long owner) {
            this.owner = owner;
            return this;
        }

        public TaskSnapshot build() {
            return new TaskSnapshot(name, taskTemplateName, taskPayload, owner);
        }
    }
}
