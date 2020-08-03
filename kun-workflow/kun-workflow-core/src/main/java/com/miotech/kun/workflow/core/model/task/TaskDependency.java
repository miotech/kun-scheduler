package com.miotech.kun.workflow.core.model.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.workflow.utils.JsonLongFieldDeserializer;

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

    @JsonIgnore
    public DependencyFunction getDependencyFunction() {
        return dependencyFunc;
    }

    @JsonProperty("dependencyFunc")
    public String getDependencyFunctionType() {
        return dependencyFunc.toFunctionType();
    }
}
