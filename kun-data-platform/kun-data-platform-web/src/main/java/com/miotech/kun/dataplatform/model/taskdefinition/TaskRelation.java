package com.miotech.kun.dataplatform.model.taskdefinition;

import java.time.OffsetDateTime;

public class TaskRelation {
    private final Long upstreamTaskId;

    private final Long downstreamTaskId;

    private final OffsetDateTime createdAt;

    private final OffsetDateTime updatedAt;

    public TaskRelation(Long upstreamTaskId, Long downstreamTaskId, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.upstreamTaskId = upstreamTaskId;
        this.downstreamTaskId = downstreamTaskId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getUpstreamId() {
        return upstreamTaskId;
    }

    public Long getDownstreamId() {
        return downstreamTaskId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public static Builder newBuilder() { return new Builder(); }

    public static final class Builder {
        private Long upstreamTaskId;
        private Long downstreamTaskId;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;

        private Builder() {
        }

        public Builder withUpstreamId(Long upstreamTaskId) {
            this.upstreamTaskId = upstreamTaskId;
            return this;
        }

        public Builder withDownstreamId(Long downstreamTaskId) {
            this.downstreamTaskId = downstreamTaskId;
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

        public TaskRelation build() {
            return new TaskRelation(upstreamTaskId, downstreamTaskId, createdAt, updatedAt);
        }
    }
}
