package com.miotech.kun.common.task.filter;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class TaskSearchFilter {
    private final String name;
    private final List<Pair<String, String>> tags;
    private final Integer pageNum;
    private final Integer pageSize;

    private TaskSearchFilter(String name, List<Pair<String, String>> tags, Integer pageNum, Integer pageSize) {
        this.name = name;
        this.tags = tags;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public String getName() {
        return name;
    }

    public List<Pair<String, String>> getTags() {
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

    public static final class TaskSearchFilterBuilder {
        private String name;
        private List<Pair<String, String>> tags;
        private Integer pageNum;
        private Integer pageSize;

        private TaskSearchFilterBuilder() {
        }

        public static TaskSearchFilterBuilder aTaskSearchFilter() {
            return new TaskSearchFilterBuilder();
        }

        public TaskSearchFilterBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public TaskSearchFilterBuilder withTags(List<Pair<String, String>> tags) {
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
