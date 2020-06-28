package com.miotech.kun.datadiscovery.model;

import lombok.Data;

/**
 * @author: Jie Chen
 * @created: 6/12/20
 */
@Data
public class PageInfo {

    private Integer pageNumber = 1;

    private Integer pageSize = 25;

    private Long totalCount;
}
