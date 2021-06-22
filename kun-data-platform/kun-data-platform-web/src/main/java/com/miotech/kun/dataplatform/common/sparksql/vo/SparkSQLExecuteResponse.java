package com.miotech.kun.dataplatform.common.sparksql.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder(builderClassName = "Builder")
public class SparkSQLExecuteResponse {

    private String msg;

    private String errorMsg;

    private long total;

    private boolean hasNext;

    private List<String> columnNames;

    private List<List<String>> records;

}
