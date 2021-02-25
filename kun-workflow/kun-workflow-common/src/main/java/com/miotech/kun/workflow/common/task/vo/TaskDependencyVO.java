package com.miotech.kun.workflow.common.task.vo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.workflow.core.model.task.DependencyLevel;
import com.miotech.kun.workflow.utils.JsonLongFieldDeserializer;

@JsonDeserialize(builder = TaskDependencyVO.Builder.class)
public class TaskDependencyVO {
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = JsonLongFieldDeserializer.class)
    private final Long upstreamTaskId;

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = JsonLongFieldDeserializer.class)
    private final Long downstreamTaskId;

    private final String dependencyFunc;

    private final String dependencyLevel;


    public TaskDependencyVO(Long upstreamTaskId, Long downstreamTaskId, String dependencyFunc, String dependencyLevel) {
        this.upstreamTaskId = upstreamTaskId;
        this.downstreamTaskId = downstreamTaskId;
        this.dependencyFunc = dependencyFunc;
        this.dependencyLevel = dependencyLevel;
    }

    public Long getUpstreamTaskId() {
        return upstreamTaskId;
    }

    public Long getDownstreamTaskId() {
        return downstreamTaskId;
    }

    public String getDependencyFunction() {
        return dependencyFunc;
    }

    public String getDependencyLevel() {
        return dependencyLevel;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private Long upstreamTaskId;
        private Long downstreamTaskId;
        private String dependencyFunc;
        private String dependencyLevel;

        private Builder() {
        }

        public Builder withUpstreamTaskId(Long upstreamTaskId) {
            this.upstreamTaskId = upstreamTaskId;
            return this;
        }

        public Builder withDownstreamTaskId(Long downstreamTaskId) {
            this.downstreamTaskId = downstreamTaskId;
            return this;
        }

        public Builder withDependencyFunc(String dependencyFunc) {
            this.dependencyFunc = dependencyFunc;
            return this;
        }

        public Builder withDependencyLevel(String dependencyLevel) {
            this.dependencyLevel = dependencyLevel;
            return this;
        }

        public TaskDependencyVO build() {
            return new TaskDependencyVO(upstreamTaskId, downstreamTaskId, dependencyFunc, dependencyLevel);
        }
    }
}
