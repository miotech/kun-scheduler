package com.miotech.kun.workflow.common.task.vo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
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

    public TaskDependencyVO(Long upstreamTaskId, Long downstreamTaskId, String dependencyFunc) {
        this.upstreamTaskId = upstreamTaskId;
        this.downstreamTaskId = downstreamTaskId;
        this.dependencyFunc = dependencyFunc;
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

    public static Builder newBuilder() { return new Builder(); }

    public static final class Builder {
        private Long upstreamTaskId;
        private Long downstreamTaskId;
        private String dependencyFunc;

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

        public TaskDependencyVO build() {
            return new TaskDependencyVO(upstreamTaskId, downstreamTaskId, dependencyFunc);
        }
    }
}
