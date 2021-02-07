package com.miotech.kun.dataplatform.common.backfill.vo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.dataplatform.model.backfill.Backfill;
import com.miotech.kun.workflow.client.model.TaskRun;
import com.miotech.kun.workflow.utils.JsonLongFieldDeserializer;

import java.time.OffsetDateTime;
import java.util.List;

@JsonDeserialize(builder = BackfillDetailVO.BackfillDetailVOBuilder.class)
public class BackfillDetailVO {
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = JsonLongFieldDeserializer.class)
    private final Long id;

    private final String name;

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = JsonLongFieldDeserializer.class)
    private final Long creator;

    private final OffsetDateTime createTime;

    private final OffsetDateTime updateTime;

    private final List<Long> taskRunIds;

    private final List<Long> workflowTaskIds;

    private final List<Long> taskDefinitionIds;

    private final List<TaskRun> taskRunList;

    private BackfillDetailVO(BackfillDetailVOBuilder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.creator = builder.creator;
        this.taskRunIds = builder.taskRunIds;
        this.workflowTaskIds = builder.workflowTaskIds;
        this.taskDefinitionIds = builder.taskDefinitionIds;
        this.taskRunList = builder.taskRunList;
        this.createTime = builder.createTime;
        this.updateTime = builder.updateTime;
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

    public OffsetDateTime getCreateTime() {
        return createTime;
    }

    public OffsetDateTime getUpdateTime() {
        return updateTime;
    }

    public List<Long> getTaskRunIds() {
        return taskRunIds;
    }

    public List<Long> getWorkflowTaskIds() {
        return workflowTaskIds;
    }

    public List<Long> getTaskDefinitionIds() {
        return taskDefinitionIds;
    }

    public List<TaskRun> getTaskRunList() {
        return taskRunList;
    }

    public static BackfillDetailVOBuilder newBuilder() {
        return new BackfillDetailVOBuilder();
    }

    public static BackfillDetailVO from(Backfill backfill, List<TaskRun> taskRuns) {
        return BackfillDetailVO.newBuilder()
                .withId(backfill.getId())
                .withName(backfill.getName())
                .withCreator(backfill.getCreator())
                .withTaskRunIds(backfill.getTaskRunIds())
                .withTaskDefinitionIds(backfill.getTaskDefinitionIds())
                .withWorkflowTaskIds(backfill.getWorkflowTaskIds())
                .withCreateTime(backfill.getCreateTime())
                .withUpdateTime(backfill.getUpdateTime())
                .withTaskRunList(taskRuns)
                .build();
    }

    @JsonPOJOBuilder
    public static final class BackfillDetailVOBuilder {
        private Long id;
        private String name;
        private Long creator;
        private OffsetDateTime createTime;
        private OffsetDateTime updateTime;
        private List<Long> taskRunIds;
        private List<Long> workflowTaskIds;
        private List<Long> taskDefinitionIds;
        private List<TaskRun> taskRunList;

        private BackfillDetailVOBuilder() {
        }

        public BackfillDetailVOBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public BackfillDetailVOBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public BackfillDetailVOBuilder withCreator(Long creator) {
            this.creator = creator;
            return this;
        }

        public BackfillDetailVOBuilder withCreateTime(OffsetDateTime createTime) {
            this.createTime = createTime;
            return this;
        }

        public BackfillDetailVOBuilder withUpdateTime(OffsetDateTime updateTime) {
            this.updateTime = updateTime;
            return this;
        }

        public BackfillDetailVOBuilder withTaskRunIds(List<Long> taskRunIds) {
            this.taskRunIds = taskRunIds;
            return this;
        }

        public BackfillDetailVOBuilder withWorkflowTaskIds(List<Long> workflowTaskIds) {
            this.workflowTaskIds = workflowTaskIds;
            return this;
        }

        public BackfillDetailVOBuilder withTaskDefinitionIds(List<Long> taskDefinitionIds) {
            this.taskDefinitionIds = taskDefinitionIds;
            return this;
        }

        public BackfillDetailVOBuilder withTaskRunList(List<TaskRun> taskRunList) {
            this.taskRunList = taskRunList;
            return this;
        }

        public BackfillDetailVO build() {
            return new BackfillDetailVO(this);
        }
    }
}
