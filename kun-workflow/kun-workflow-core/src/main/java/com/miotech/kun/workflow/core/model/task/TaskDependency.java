package com.miotech.kun.workflow.core.model.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.workflow.utils.JsonLongFieldDeserializer;

@JsonDeserialize(builder = TaskDependency.TaskDependencyBuilder.class)
public class TaskDependency {
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = JsonLongFieldDeserializer.class)
    private final Long upstreamTaskId;

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = JsonLongFieldDeserializer.class)
    private final Long downstreamTaskId;

    private final DependencyFunction dependencyFunc;

    public TaskDependency(Long upstreamTaskId, Long downstreamTaskId, DependencyFunction dependencyFunc) {
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

    public static TaskDependencyBuilder newBuilder() { return new TaskDependencyBuilder(); }

    @JsonIgnore
    public DependencyFunction getDependencyFunction() {
        return dependencyFunc;
    }

    @JsonProperty("dependencyFunc")
    public String getDependencyFunctionType() {
        return dependencyFunc.toFunctionType();
    }


    public static final class TaskDependencyBuilder {
        private Long upstreamTaskId;
        private Long downstreamTaskId;
        private DependencyFunction dependencyFunc;

        private TaskDependencyBuilder() {
        }

        public TaskDependencyBuilder withUpstreamTaskId(Long upstreamTaskId) {
            this.upstreamTaskId = upstreamTaskId;
            return this;
        }

        public TaskDependencyBuilder withDownstreamTaskId(Long downstreamTaskId) {
            this.downstreamTaskId = downstreamTaskId;
            return this;
        }

        @JsonIgnore
        public TaskDependencyBuilder withDependencyFunc(DependencyFunction dependencyFunc) {
            this.dependencyFunc = dependencyFunc;
            return this;
        }

        public TaskDependency build() {
            return new TaskDependency(upstreamTaskId, downstreamTaskId, dependencyFunc);
        }
    }
}
