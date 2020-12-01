package com.miotech.kun.common.model;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;

public class PageResult<T> {
    private final Integer pageSize;

    private final Integer pageNumber;

    private final Integer totalCount;

    private final List<T> records;

    public PageResult(Integer pageSize, Integer pageNumber, Integer totalCount, List<T> records) {
        this.pageSize = pageSize;
        this.pageNumber = pageNumber;
        this.totalCount = totalCount;
        this.records = ImmutableList.copyOf(records);
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public List<T> getRecords() {
        return records;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PageResult<?> that = (PageResult<?>) o;
        return Objects.equals(pageSize, that.pageSize) &&
                Objects.equals(pageNumber, that.pageNumber) &&
                Objects.equals(totalCount, that.totalCount) &&
                Objects.equals(records, that.records);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageSize, pageNumber, totalCount, records);
    }
}
