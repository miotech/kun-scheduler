package com.miotech.kun.datadiscover.model;

import lombok.Data;

/**
 * @author: JieChen
 * @created: 6/12/20
 */
@Data
public class PageInfo {

    private Integer pageNumber;

    private Integer pageSize;

    private Long totalCount;
}
