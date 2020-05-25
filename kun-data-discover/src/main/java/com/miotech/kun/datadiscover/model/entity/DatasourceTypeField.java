package com.miotech.kun.datadiscover.model.entity;

import lombok.Data;

/**
 * @author: JieChen
 * @created: 6/12/20
 */
@Data
public class DatasourceTypeField {

    private Long id;

    private Long typeId;

    private String name;

    private Integer sequenceOrder;

    private String format;
}
