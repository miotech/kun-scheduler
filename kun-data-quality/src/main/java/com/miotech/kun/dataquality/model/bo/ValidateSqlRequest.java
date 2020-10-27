package com.miotech.kun.dataquality.model.bo;

import lombok.Data;

/**
 * @author: Jie Chen
 * @created: 2020/7/19
 */
@Data
public class ValidateSqlRequest {
    String sqlText;

    Long datasetId;
}
