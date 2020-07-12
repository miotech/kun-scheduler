package com.miotech.kun.workflow.client.model;

public class OperatorSearchRequest {

    private final String name;

    private final int pageSize;

    private final int pageNum;

    private OperatorSearchRequest(Builder builder) {
        this.name = builder.name;
        this.pageSize = builder.pageSize;
        this.pageNum = builder.pageNum;
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

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private String name;
        private int pageSize;
        private int pageNum;

        private Builder() {
        }

        public OperatorSearchRequest build() {
            return new OperatorSearchRequest(this);
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
    }
}
