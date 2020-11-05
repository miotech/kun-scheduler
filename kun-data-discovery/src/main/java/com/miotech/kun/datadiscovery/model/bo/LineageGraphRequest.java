package com.miotech.kun.datadiscovery.model.bo;

import lombok.Data;

/**
 * @author: Jie Chen
 * @created: 2020/10/20
 */
@Data
public class LineageGraphRequest {

    Long datasetGid;

    String direction = "BOTH";

    Integer depth = 1;
}
