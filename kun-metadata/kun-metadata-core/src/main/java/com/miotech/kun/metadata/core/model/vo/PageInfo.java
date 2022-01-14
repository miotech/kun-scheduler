package com.miotech.kun.metadata.core.model.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PageInfo {

    private Integer pageNumber = 1;

    private Integer pageSize = 25;

    private Integer totalCount;

    public PageInfo() {
    }

    @JsonCreator
    public PageInfo(@JsonProperty("pageNumber") Integer pageNumber, @JsonProperty("pageSize") Integer pageSize,
                    @JsonProperty("totalCount") Integer totalCount) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalCount = totalCount;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }
}
