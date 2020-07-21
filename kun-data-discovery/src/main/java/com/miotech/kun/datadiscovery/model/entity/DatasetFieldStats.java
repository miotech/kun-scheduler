package com.miotech.kun.datadiscovery.model.entity;

import lombok.Data;

/**
 * @author: Jie Chen
 * @created: 2020/7/21
 */
@Data
public class DatasetFieldStats {

    private Long notNullCount;

    private Long distinctCount;

    private Long statsDate;
}