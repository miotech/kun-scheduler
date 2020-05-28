package com.miotech.kun.workflow.common.operator.filter;

public class OperatorSearchFilter {
    private final String keyword;

    private final Integer pageNum;

    private final Integer pageSize;

    private OperatorSearchFilter(String keyword, Integer pageNum, Integer pageSize){
        this.keyword = keyword;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public String getKeyword() {
        return keyword;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public static OperatorSearchFilterBuilder newBuilder() {
        return new OperatorSearchFilterBuilder();
    }

    public OperatorSearchFilterBuilder cloneBuilder() {
        return new OperatorSearchFilterBuilder()
                .withKeyword(keyword)
                .withPageNum(pageNum)
                .withPageSize(pageSize);
    }

    public static final class OperatorSearchFilterBuilder {
        private String keyword;
        private Integer pageNum;
        private Integer pageSize;

        private OperatorSearchFilterBuilder() {
        }

        public static OperatorSearchFilterBuilder anOperatorSearchFilter() {
            return new OperatorSearchFilterBuilder();
        }

        public OperatorSearchFilterBuilder withKeyword(String keyword) {
            this.keyword = keyword;
            return this;
        }

        public OperatorSearchFilterBuilder withPageNum(Integer pageNum) {
            this.pageNum = pageNum;
            return this;
        }

        public OperatorSearchFilterBuilder withPageSize(Integer pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public OperatorSearchFilter build() {
            return new OperatorSearchFilter(this.keyword, this.pageNum, this.pageSize);
        }
    }
}
