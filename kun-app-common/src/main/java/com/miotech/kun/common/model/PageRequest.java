package com.miotech.kun.common.model;

import lombok.Data;

@Data
public class PageRequest {
    protected Integer pageSize;

    protected Integer pageNum;

    public PageRequest(Integer pageSize, Integer pageNum) {
        this.pageSize = pageSize;
        this.pageNum = pageNum;
    }
}
