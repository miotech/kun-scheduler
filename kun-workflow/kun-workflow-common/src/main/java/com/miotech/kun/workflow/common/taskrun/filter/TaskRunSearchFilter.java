package com.miotech.kun.workflow.common.taskrun.filter;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;

import java.time.OffsetDateTime;
import java.util.List;

@JsonDeserialize(builder = TaskRunSearchFilter.Builder.class)
public class TaskRunSearchFilter {
    private List<Long> taskIds;

    private TaskRunStatus status;

    private OffsetDateTime dateFrom;

    private OffsetDateTime dateTo;

    private Integer pageNum;

    private Integer pageSize;

    public TaskRunSearchFilter(List<Long> taskIds, TaskRunStatus status, OffsetDateTime dateFrom, OffsetDateTime dateTo, Integer pageNum, Integer pageSize) {
        this.taskIds = taskIds;
        this.status = status;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public List<Long> getTaskIds() {
        return taskIds;
    }

    public void setTaskIds(List<Long> taskIds) {
        this.taskIds = taskIds;
    }

    public TaskRunStatus getStatus() {
        return status;
    }

    public void setStatus(TaskRunStatus status) {
        this.status = status;
    }

    public OffsetDateTime getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(OffsetDateTime dateFrom) {
        this.dateFrom = dateFrom;
    }

    public OffsetDateTime getDateTo() {
        return dateTo;
    }

    public void setDateTo(OffsetDateTime dateTo) {
        this.dateTo = dateTo;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
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
                .withPageSize(pageSize);
    }

    @JsonPOJOBuilder
    public static final class Builder {
        private List<Long> taskIds;
        private TaskRunStatus status;
        private OffsetDateTime dateFrom;
        private OffsetDateTime dateTo;
        private Integer pageNum;
        private Integer pageSize;

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

        public TaskRunSearchFilter build() {
            return new TaskRunSearchFilter(taskIds, status, dateFrom, dateTo, pageNum, pageSize);
        }
    }
}
