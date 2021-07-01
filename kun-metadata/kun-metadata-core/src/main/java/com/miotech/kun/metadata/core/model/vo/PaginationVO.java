package com.miotech.kun.metadata.core.model.vo;

import java.util.List;

public class PaginationVO<T> {

    private int pageNum;

    private int pageSize;

    private int totalCount;

    private List<T> records;

    public PaginationVO() {
    }

    public PaginationVO(int pageNum, int pageSize, int totalCount, List<T> records) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.totalCount = totalCount;
        this.records = records;
    }

    public int getPageNum() {
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

}
