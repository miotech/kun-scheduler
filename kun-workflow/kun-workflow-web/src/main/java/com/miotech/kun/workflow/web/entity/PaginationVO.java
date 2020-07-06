package com.miotech.kun.workflow.web.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.List;
import java.util.Objects;

@JsonDeserialize(builder = PaginationVO.PaginationVOBuilder.class)
public class PaginationVO<T> {

    private final int pageNumber;

    private final int pageSize;

    private final int totalCount;

    private final List<T> records;

    private PaginationVO(PaginationVOBuilder<T> builder) {
        this.pageNumber = builder.pageNumber;
        this.pageSize = builder.pageSize;
        this.totalCount = builder.totalCount;
        this.records = builder.records;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalCount() {
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
                .withPageNumber(pageNumber)
                .withPageSize(pageSize)
                .withTotalCount(totalCount)
                .withRecords(records);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaginationVO<?> that = (PaginationVO<?>) o;
        return pageNumber == that.pageNumber &&
                pageSize == that.pageSize &&
                totalCount == that.totalCount &&
                Objects.equals(records, that.records);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageNumber, pageSize, totalCount, records);
    }

    @JsonPOJOBuilder
    public static final class PaginationVOBuilder<T> {
        private int pageNumber;
        private int pageSize;
        private int totalCount;
        private List<T> records;

        private PaginationVOBuilder() {
        }

        public PaginationVOBuilder<T> withPageNumber(int pageNumber) {
            this.pageNumber = pageNumber;
            return this;
        }

        public PaginationVOBuilder<T> withPageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public PaginationVOBuilder<T> withTotalCount(int totalCount) {
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
