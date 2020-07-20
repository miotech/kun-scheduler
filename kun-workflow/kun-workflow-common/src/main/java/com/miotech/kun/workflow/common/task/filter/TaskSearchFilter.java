package com.miotech.kun.workflow.common.task.filter;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.miotech.kun.workflow.core.model.common.Tag;

import java.util.List;

@JsonDeserialize(builder = TaskSearchFilter.TaskSearchFilterBuilder.class)
public class TaskSearchFilter {
    private final String name;
    private final List<Tag> tags;
    private final Integer pageNum;
    private final Integer pageSize;

    private TaskSearchFilter(String name, List<Tag> tags, Integer pageNum, Integer pageSize) {
        this.name = name;
        this.tags = tags;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public String getName() {
        return name;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public static TaskSearchFilterBuilder newBuilder() {
        return new TaskSearchFilterBuilder();
    }

    public TaskSearchFilterBuilder cloneBuilder() {
        return new TaskSearchFilterBuilder()
                .withName(name)
                .withPageNum(pageNum)
                .withPageSize(pageSize)
                .withTags(tags);
    }

    @JsonPOJOBuilder
    public static final class TaskSearchFilterBuilder {
        private String name;
        private List<Tag> tags;
        private Integer pageNum;
        private Integer pageSize;

        private TaskSearchFilterBuilder() {
        }

        public TaskSearchFilterBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public TaskSearchFilterBuilder withTags(List<Tag> tags) {
            this.tags = tags;
            return this;
        }

        public TaskSearchFilterBuilder withPageNum(Integer pageNum) {
            this.pageNum = pageNum;
            return this;
        }

        public TaskSearchFilterBuilder withPageSize(Integer pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public TaskSearchFilter build() {
            return new TaskSearchFilter(name, tags, pageNum, pageSize);
        }
    }
}
