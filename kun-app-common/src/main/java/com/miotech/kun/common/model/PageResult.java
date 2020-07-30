package com.miotech.kun.common.model;

import java.util.List;

public class PageResult<T> {
    private Integer pageSize;

    private Integer pageNum;

    private Long totalCount;

    private List<T> records;
}
