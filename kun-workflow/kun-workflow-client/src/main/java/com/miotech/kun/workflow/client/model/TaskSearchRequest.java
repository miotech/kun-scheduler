package com.miotech.kun.workflow.client.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.miotech.kun.workflow.core.model.common.Tag;

import java.util.List;

@JsonDeserialize(builder = TaskSearchRequest.Builder.class)
public class TaskSearchRequest {
    private final String name;
    private final List<Tag> tags;
    private final Integer pageNum;
    private final Integer pageSize;

    private TaskSearchRequest(String name, List<Tag> tags, Integer pageNum, Integer pageSize) {
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

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder cloneBuilder() {
        return new Builder()
                .withName(name)
                .withPageNum(pageNum)
                .withPageSize(pageSize)
                .withTags(tags);
    }

    @JsonPOJOBuilder
    public static final class Builder {
        private String name;
        private List<Tag> tags;
        private Integer pageNum;
        private Integer pageSize;

        private Builder() {
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withTags(List<Tag> tags) {
            this.tags = tags;
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

        public TaskSearchRequest build() {
            return new TaskSearchRequest(name, tags, pageNum, pageSize);
        }
    }
}