package com.miotech.kun.workflow.common.taskrun.filter;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.miotech.kun.workflow.core.model.common.Tag;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

@JsonDeserialize(builder = TaskRunSearchFilter.Builder.class)
public class TaskRunSearchFilter {
    private final List<Long> taskIds;

    private final TaskRunStatus status;

    private final OffsetDateTime dateFrom;

    private final OffsetDateTime dateTo;

    private final Integer pageNum;

    private final Integer pageSize;

    private final List<Tag> tags;

    public TaskRunSearchFilter(TaskRunSearchFilter.Builder builder) {
        this.taskIds = builder.taskIds;
        this.status = builder.status;
        this.dateFrom = builder.dateFrom;
        this.dateTo = builder.dateTo;
        this.pageNum = builder.pageNum;
        this.pageSize = builder.pageSize;
        this.tags = builder.tags;
    }

    public List<Long> getTaskIds() {
        return taskIds;
    }

    public TaskRunStatus getStatus() {
        return status;
    }

    public OffsetDateTime getDateFrom() {
        return dateFrom;
    }

    public OffsetDateTime getDateTo() {
        return dateTo;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder cloneBuilder() {
        return new Builder()
                .withTaskIds(taskIds)
                .withDateFrom(dateFrom)
                .withDateTo(dateTo)
                .withStatus(status)
                .withPageNum(pageNum)
                .withPageSize(pageSize)
                .withTags(tags);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskRunSearchFilter that = (TaskRunSearchFilter) o;
        return Objects.equals(taskIds, that.taskIds) &&
                status == that.status &&
                Objects.equals(dateFrom, that.dateFrom) &&
                Objects.equals(dateTo, that.dateTo) &&
                Objects.equals(pageNum, that.pageNum) &&
                Objects.equals(pageSize, that.pageSize) &&
                Objects.equals(tags, that.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskIds, status, dateFrom, dateTo, pageNum, pageSize);
    }

    @JsonPOJOBuilder
    public static final class Builder {
        private List<Long> taskIds;
        private TaskRunStatus status;
        private OffsetDateTime dateFrom;
        private OffsetDateTime dateTo;
        private Integer pageNum;
        private Integer pageSize;
        private List<Tag> tags;

        private Builder() {
        }

        public Builder withTaskIds(List<Long> taskIds) {
            this.taskIds = taskIds;
            return this;
        }

        public Builder withStatus(TaskRunStatus status) {
            this.status = status;
            return this;
        }

        public Builder withDateFrom(OffsetDateTime dateFrom) {
            this.dateFrom = dateFrom;
            return this;
        }

        public Builder withDateTo(OffsetDateTime dateTo) {
            this.dateTo = dateTo;
            return this;
        }

        public Builder withPageNum(Integer pageNum) {
            this.pageNum = pageNum;
            return this;
        }

        public Builder withPageSize(Integer pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public Builder withTags(List<Tag> tags) {
            this.tags = tags;
            return this;
        }

        public TaskRunSearchFilter build() {
            return new TaskRunSearchFilter(this);
        }
    }
}
