package com.miotech.kun.datadashboard.model.entity;

import lombok.Data;

/**
 * @author: Jie Chen
 * @created: 2020/9/19
 */
@Data
public class ColumnMetrics {

    String datasetName;

    String columnName;

    Long columnNullCount;

    Long columnDistinctCount;

    Long totalRowCount;
}
