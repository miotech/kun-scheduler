package com.miotech.kun.dataplatform.common.sparksql.vo;

import lombok.Data;

@Data
public class SparkSQLExecuteRequest {

    private String sql;

    private int pageNum;

    private int pageSize;

}
