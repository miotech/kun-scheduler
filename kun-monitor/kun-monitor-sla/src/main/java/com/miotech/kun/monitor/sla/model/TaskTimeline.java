package com.miotech.kun.monitor.sla.model;

import java.time.OffsetDateTime;

public class TaskTimeline {

    private final Long id;

    private final Long taskRunId;

    private final Long definitionId;

    private final Integer level;

    private final String deadline;

    private final Long rootDefinitionId;

    private final OffsetDateTime createdAt;

    private final OffsetDateTime updatedAt;

    public TaskTimeline(Long id, Long taskRunId, Long definitionId, Integer level, String deadline, Long rootDefinitionId,
                        OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.taskRunId = taskRunId;
        this.definitionId = definitionId;
        this.level = level;
        this.deadline = deadline;
        this.rootDefinitionId = rootDefinitionId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getTaskRunId() {
        return taskRunId;
    }

    public Long getDefinitionId() {
        return definitionId;
    }

    public Integer getLevel() {
        return level;
    }

    public String getDeadline() {
        return deadline;
    }

    public Long getRootDefinitionId() {
        return rootDefinitionId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private Long id;
        private Long taskRunId;
        private Long definitionId;
        private Integer level;
        private String deadline;
        private Long rootDefinitionId;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;

        private Builder() {
        }

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withTaskRunId(Long taskRunId) {
            this.taskRunId = taskRunId;
            return this;
        }

        public Builder withDefinitionId(Long definitionId) {
            this.definitionId = definitionId;
            return this;
        }

        public Builder withLevel(Integer level) {
            this.level = level;
            return this;
        }

        public Builder withDeadline(String deadline) {
            this.deadline = deadline;
            return this;
        }

        public Builder withRootDefinitionId(Long rootDefinitionId) {
            this.rootDefinitionId = rootDefinitionId;
            return this;
        }

        public Builder withCreatedAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder withUpdatedAt(OffsetDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public TaskTimeline build() {
            return new TaskTimeline(id, taskRunId, definitionId, level, deadline, rootDefinitionId, createdAt, updatedAt);
        }
    }
}
