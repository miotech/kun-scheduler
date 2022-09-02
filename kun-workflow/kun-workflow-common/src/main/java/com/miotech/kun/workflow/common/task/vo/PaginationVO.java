package com.miotech.kun.workflow.common.task.vo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.List;
import java.util.Objects;

@JsonDeserialize(builder = PaginationVO.PaginationVOBuilder.class)
public class PaginationVO<T> {

    private final Integer pageNum;

    private final Integer pageSize;

    private final Integer totalCount;

    private final List<T> records;

    private PaginationVO(PaginationVOBuilder<T> builder) {
        this.pageNum = builder.pageNum;
        this.pageSize = builder.pageSize;
        this.totalCount = builder.totalCount;
        this.records = builder.records;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public List<T> getRecords() {
        return records;
    }

    public static <T> PaginationVOBuilder<T> newBuilder() {
        return new PaginationVOBuilder<>();
    }

    public PaginationVOBuilder<T> cloneBuilder() {
        return new PaginationVOBuilder<T>()
                .withPageNum(pageNum)
                .withPageSize(pageSize)
                .withTotalCount(totalCount)
                .withRecords(records);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaginationVO<?> that = (PaginationVO<?>) o;
        return pageNum == that.pageNum &&
                pageSize == that.pageSize &&
                totalCount == that.totalCount &&
                Objects.equals(records, that.records);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageNum, pageSize, totalCount, records);
    }

    @JsonPOJOBuilder
    public static final class PaginationVOBuilder<T> {
        private Integer pageNum;
        private Integer pageSize;
        private Integer totalCount;
        private List<T> records;

        private PaginationVOBuilder() {
        }

        public PaginationVOBuilder<T> withPageNum(Integer pageNum) {
            this.pageNum = pageNum;
            return this;
        }

        public PaginationVOBuilder<T> withPageSize(Integer pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public PaginationVOBuilder<T> withTotalCount(Integer totalCount) {
            this.totalCount = totalCount;
            return this;
        }

        public PaginationVOBuilder<T> withRecords(List<T> records) {
            this.records = records;
            return this;
        }

        public PaginationVO<T> build() {
            return new PaginationVO<>(this);
        }
    }
}
