package com.miotech.kun.dataplatform.model.backfill;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.google.common.collect.ImmutableList;
import com.miotech.kun.workflow.utils.JsonLongFieldDeserializer;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

@JsonDeserialize(builder = Backfill.BackFillBuilder.class)
public class Backfill {
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = JsonLongFieldDeserializer.class)
    private final Long id;

    private final String name;

    private final List<Long> taskRunIds;

    private final List<Long> workflowTaskIds;

    private final List<Long> taskDefinitionIds;

    private final OffsetDateTime createTime;

    private final OffsetDateTime updateTime;

    private final Long creator;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Long> getTaskRunIds() {
        return taskRunIds;
    }

    public List<Long> getTaskDefinitionIds() {
        return taskDefinitionIds;
    }

    public List<Long> getWorkflowTaskIds() {
        return workflowTaskIds;
    }

    public OffsetDateTime getCreateTime() {
        return createTime;
    }

    public OffsetDateTime getUpdateTime() {
        return updateTime;
    }

    public Long getCreator() {
        return creator;
    }

    private Backfill(BackFillBuilder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.taskRunIds = ImmutableList.copyOf(builder.taskRunIds);
        this.taskDefinitionIds = ImmutableList.copyOf(builder.taskDefinitionIds);
        this.workflowTaskIds = ImmutableList.copyOf(builder.workflowTaskIds);
        this.createTime = builder.createTime;
        this.updateTime = builder.updateTime;
        this.creator = builder.creator;
    }

    public static BackFillBuilder newBuilder() {
        return new BackFillBuilder();
    }

    public BackFillBuilder cloneBuilder() {
        BackFillBuilder builder = new BackFillBuilder();
        builder.id = this.id;
        builder.name = this.name;
        builder.taskDefinitionIds = this.taskDefinitionIds;
        builder.taskRunIds = this.taskRunIds;
        builder.workflowTaskIds = this.workflowTaskIds;
        builder.createTime = this.createTime;
        builder.updateTime = this.updateTime;
        builder.creator = this.creator;
        return builder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Backfill that = (Backfill) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(taskRunIds, that.taskRunIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, taskRunIds);
    }

    @JsonPOJOBuilder
    public static final class BackFillBuilder {
        private Long id;
        private String name;
        private List<Long> taskRunIds;
        private List<Long> workflowTaskIds;
        private List<Long> taskDefinitionIds;
        private OffsetDateTime createTime;
        private OffsetDateTime updateTime;
        private Long creator;

        private BackFillBuilder() {
        }

        public BackFillBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public BackFillBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public BackFillBuilder withTaskRunIds(List<Long> taskRunIds) {
            this.taskRunIds = taskRunIds;
            return this;
        }

        public BackFillBuilder withWorkflowTaskIds(List<Long> workflowTaskIds) {
            this.workflowTaskIds = workflowTaskIds;
            return this;
        }

        public BackFillBuilder withCreateTime(OffsetDateTime createTime) {
            this.createTime = createTime;
            return this;
        }

        public BackFillBuilder withUpdateTime(OffsetDateTime updateTime) {
            this.updateTime = updateTime;
            return this;
        }

        public BackFillBuilder withCreator(Long creator) {
            this.creator = creator;
            return this;
        }

        public BackFillBuilder withTaskDefinitionIds(List<Long> taskDefinitionIds) {
            this.taskDefinitionIds = taskDefinitionIds;
            return this;
        }

        public Backfill build() {
            return new Backfill(this);
        }
    }
}
