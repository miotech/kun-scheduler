package com.miotech.kun.metadata.core.model.datasource;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = DataSourceSearchFilter.Builder.class)
public class DataSourceSearchFilter {

    private final String name;

    private final int pageNum;

    private final int pageSize;

    public DataSourceSearchFilter(String name, int pageNum, int pageSize) {
        this.name = name;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public String getName() {
        return name;
    }

    public int getPageNum() {
        return pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @JsonPOJOBuilder
    public static final class Builder {
        private String name;
        private int pageNum;
        private int pageSize;

        private Builder() {
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withPageNum(int pageNum) {
            this.pageNum = pageNum;
            return this;
        }

        public Builder withPageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public DataSourceSearchFilter build() {
            return new DataSourceSearchFilter(name, pageNum, pageSize);
        }
    }
}
