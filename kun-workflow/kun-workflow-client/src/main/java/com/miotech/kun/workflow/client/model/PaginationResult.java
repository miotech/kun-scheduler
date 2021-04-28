package com.miotech.kun.workflow.client.model;

import java.util.List;

public class PaginationResult<T> {
    private int pageSize;

    private int pageNum;

    private int totalCount;

    private List<T> records;

    public PaginationResult() {
    }

    public PaginationResult(int pageSize, int pageNum, int totalCount, List<T> records) {
        this.pageSize = pageSize;
        this.pageNum = pageNum;
        this.totalCount = totalCount;
        this.records = records;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getPageNum() {
        return pageNum;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public List<T> getRecords() {
        return records;
    }
}
