package com.miotech.kun.datadashboard.model.entity;

import lombok.Data;

/**
 * @author: Jie Chen
 * @created: 2020/9/17
 */
@Data
public class DatasetRowCountChange {

    String datasetName;

    String database;

    String dataSource;

    Long rowChange;

    Long rowCount;

    Float rowChangeRatio;
}
