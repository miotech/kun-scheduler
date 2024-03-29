package com.miotech.kun.workflow.client.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.miotech.kun.commons.utils.ArgumentCheckUtils;
import com.miotech.kun.commons.utils.CustomDateTimeSerializer;
import com.miotech.kun.workflow.core.model.common.Tag;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

@JsonDeserialize(builder = TaskRunSearchRequest.Builder.class)
public class TaskRunSearchRequest {
    private static final List<String> AVAILABLE_SORT_KEYS = Lists.newArrayList("id", "status", "startAt", "endAt", "createdAt", "updatedAt");

    private final String name;

    private final int pageSize;

    private final int pageNum;

    private final List<Long> taskRunIds;

    private final List<Long> taskIds;

    private final List<Tag> tags;

    private final Set<TaskRunStatus> status;

    private final List<String> scheduleTypes;

    @JsonSerialize(using = CustomDateTimeSerializer.class)
    private final OffsetDateTime dateFrom;

    @JsonSerialize(using = CustomDateTimeSerializer.class)
    private final OffsetDateTime dateTo;

    @JsonSerialize(using = CustomDateTimeSerializer.class)
    private final OffsetDateTime endBefore;

    @JsonSerialize(using = CustomDateTimeSerializer.class)
    private final OffsetDateTime endAfter;

    /**
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
        this.endBefore = builder.endBefore;
        this.endAfter = builder.endAfter;
        this.sortKey = builder.sortKey;
        this.sortOrder = builder.sortOrder;
        this.includeStartedOnly = builder.includeStartedOnly;
        this.scheduleTypes = builder.scheduleTypes;
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

    public Set<TaskRunStatus> getStatus() {
        return status;
    }

    public OffsetDateTime getDateFrom() {
        return dateFrom;
    }

    public OffsetDateTime getDateTo() {
        return dateTo;
    }

    public OffsetDateTime getEndBefore() {
        return endBefore;
    }

    public OffsetDateTime getEndAfter() {
        return endAfter;
    }

    public String getSortKey() {
        return sortKey;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public Boolean getIncludeStartedOnly() {
        return includeStartedOnly;
    }


    public List<String> getScheduleTypes() {
        return scheduleTypes;
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
        private Set<TaskRunStatus> status;
        private OffsetDateTime dateFrom;
        private OffsetDateTime dateTo;
        private OffsetDateTime endBefore;
        private OffsetDateTime endAfter;
        private String sortKey;
        private String sortOrder;
        private Boolean includeStartedOnly;
        private List<String> scheduleTypes;

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

        public Builder withEndBefore(OffsetDateTime endBefore) {
            this.endBefore = endBefore;
            return this;
        }

        public Builder withEndAfter(OffsetDateTime endAfter) {
            this.endAfter = endAfter;
            return this;
        }

        public Builder withStatus(Set<TaskRunStatus> status) {
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

        public Builder withScheduleTypes(List<String> scheduleTypes) {
            this.scheduleTypes = scheduleTypes;
            return this;
        }
    }
}
