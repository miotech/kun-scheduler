package com.miotech.kun.dataplatform.model.taskdefview;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskDefinition;
import com.miotech.kun.workflow.utils.JsonLongFieldDeserializer;

import java.time.OffsetDateTime;
import java.util.List;

@JsonDeserialize(builder = TaskDefinitionView.TaskDefinitionViewBuilder.class)
public class TaskDefinitionView {
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = JsonLongFieldDeserializer.class)
    private final Long id;

    private final String name;

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = JsonLongFieldDeserializer.class)
    private final Long creator;

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = JsonLongFieldDeserializer.class)
    private final Long lastModifier;

    private final OffsetDateTime createTime;

    private final OffsetDateTime updateTime;

    private final List<TaskDefinition> includedTaskDefinitions;

    private TaskDefinitionView(TaskDefinitionViewBuilder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.creator = builder.creator;
        this.lastModifier = builder.lastModifier;
        this.createTime = builder.createTime;
        this.updateTime = builder.updateTime;
        this.includedTaskDefinitions = ImmutableList.copyOf(builder.includedTaskDefinitions);
    }

    public static TaskDefinitionViewBuilder newBuilder() {
        return new TaskDefinitionViewBuilder();
    }

    public TaskDefinitionViewBuilder cloneBuilder() {
        TaskDefinitionViewBuilder builder = new TaskDefinitionViewBuilder();
        builder.id = this.id;
        builder.name = this.name;
        builder.creator = this.creator;
        builder.createTime = this.createTime;
        builder.updateTime = this.updateTime;
        builder.lastModifier = this.lastModifier;
        builder.includedTaskDefinitions = Lists.newArrayList(this.includedTaskDefinitions);
        return builder;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getCreator() {
        return creator;
    }

    public Long getLastModifier() {
        return lastModifier;
    }

    public OffsetDateTime getCreateTime() {
        return createTime;
    }

    public OffsetDateTime getUpdateTime() {
        return updateTime;
    }

    public List<TaskDefinition> getIncludedTaskDefinitions() {
        return includedTaskDefinitions;
    }

    @JsonPOJOBuilder
    public static final class TaskDefinitionViewBuilder {
        private Long id;
        private String name;
        private Long creator;
        private Long lastModifier;
        private OffsetDateTime createTime;
        private OffsetDateTime updateTime;
        private List<TaskDefinition> includedTaskDefinitions;

        private TaskDefinitionViewBuilder() {
        }

        public TaskDefinitionViewBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public TaskDefinitionViewBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public TaskDefinitionViewBuilder withCreator(Long creator) {
            this.creator = creator;
            return this;
        }

        public TaskDefinitionViewBuilder withLastModifier(Long lastModifier) {
            this.lastModifier = lastModifier;
            return this;
        }

        public TaskDefinitionViewBuilder withCreateTime(OffsetDateTime createTime) {
            this.createTime = createTime;
            return this;
        }

        public TaskDefinitionViewBuilder withUpdateTime(OffsetDateTime updateTime) {
            this.updateTime = updateTime;
            return this;
        }

        public TaskDefinitionViewBuilder withIncludedTaskDefinitions(List<TaskDefinition> includedTaskDefinitions) {
            this.includedTaskDefinitions = includedTaskDefinitions;
            return this;
        }

        public TaskDefinitionView build() {
            return new TaskDefinitionView(this);
        }
    }
}
