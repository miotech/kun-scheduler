package com.miotech.kun.workflow.client.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.miotech.kun.commons.utils.ArgumentCheckUtils;
import com.miotech.kun.workflow.client.CustomDateTimeSerializer;
import com.miotech.kun.workflow.core.model.common.Tag;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;

import java.time.OffsetDateTime;
import java.util.List;

@JsonDeserialize(builder = TaskRunSearchRequest.Builder.class)
public class TaskRunSearchRequest {
    private static final List<String> AVAILABLE_SORT_KEYS = Lists.newArrayList("id", "status", "startAt", "endAt");

    private final String name;

    private final int pageSize;

    private final int pageNum;

    private final List<Long> taskRunIds;

    private final List<Long> taskIds;

    private final List<Tag> tags;

    private final TaskRunStatus status;

    @JsonSerialize(using = CustomDateTimeSerializer.class)
    private final OffsetDateTime dateFrom;

    @JsonSerialize(using = CustomDateTimeSerializer.class)
    private final OffsetDateTime dateTo;

    /**
     * Consistent with {@link com.miotech.kun.workflow.common.taskrun.filter.TaskRunSearchFilter}
     * should be one of: "id", "status", "startAt", "endAt" or null
     * by default, null is equivalent to "startAt" as filter
     */
    private final String sortKey;

    /**
     * should be one of: "ASC", "DESC" or null
     */
    private final String sortOrder;

    // Only include cases that already started (cases with start time not null)
    private final Boolean includeStartedOnly;

    private TaskRunSearchRequest(Builder builder) {
        this.name = builder.name;
        this.pageSize = builder.pageSize;
        this.pageNum = builder.pageNum;
        this.taskRunIds = builder.taskRunIds;
        this.taskIds = builder.taskIds;
        this.tags = builder.tags;
        this.status = builder.status;
        this.dateFrom = builder.dateFrom;
        this.dateTo = builder.dateTo;
        this.sortKey = builder.sortKey;
        this.sortOrder = builder.sortOrder;
        this.includeStartedOnly = builder.includeStartedOnly;
    }

    public String getName() {
        return name;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getPageNum() {
        return pageNum;
    }

    public List<Long> getTaskRunIds() {
        return taskRunIds;
    }

    public List<Long> getTaskIds() {
        return taskIds;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public TaskRunStatus getStatus() {
        return status;
    }

    public OffsetDateTime getDateFrom() { return dateFrom; }

    public OffsetDateTime getDateTo() { return dateTo; }

    public String getSortKey() {
        return sortKey;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public Boolean getIncludeStartedOnly() {
        return includeStartedOnly;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private String name;
        private int pageSize;
        private int pageNum;
        private List<Long> taskRunIds;
        private List<Long> taskIds;
        private List<Tag> tags;
        private TaskRunStatus status;
        private OffsetDateTime dateFrom;
        private OffsetDateTime dateTo;
        private String sortKey;
        private String sortOrder;
        private Boolean includeStartedOnly;

        private Builder() {
        }

        public TaskRunSearchRequest build() {
            return new TaskRunSearchRequest(this);
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withPageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public Builder withPageNum(int pageNum) {
            this.pageNum = pageNum;
            return this;
        }

        public Builder withTaskRunIds(List<Long> taskRunIds) {
            this.taskRunIds = taskRunIds;
            return this;
        }

        public Builder withTaskIds(List<Long> taskIds) {
            this.taskIds = taskIds;
            return this;
        }

        public Builder withTags(List<Tag> tags) {
            this.tags = tags;
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

        public Builder withStatus(TaskRunStatus status) {
            this.status = status;
            return this;
        }

        public Builder withSortKey(String sortKey) {
            Preconditions.checkArgument(AVAILABLE_SORT_KEYS.contains(sortKey), "Invalid sort key: {}", sortKey);
            this.sortKey = sortKey;
            return this;
        }

        public Builder withSortOrder(String sortOrder) {
            ArgumentCheckUtils.checkSortOrder(sortOrder);
            this.sortOrder = sortOrder;
            return this;
        }

        public Builder withIncludeStartedOnly(Boolean includeStartedOnly) {
            this.includeStartedOnly = includeStartedOnly;
            return this;
        }
    }
}
