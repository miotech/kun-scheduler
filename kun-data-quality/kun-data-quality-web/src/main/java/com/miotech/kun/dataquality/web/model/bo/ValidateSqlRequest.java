package com.miotech.kun.dataquality.web.model.bo;

import com.miotech.kun.dataquality.web.model.entity.DataQualityRule;
import lombok.Data;

import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/7/19
 */
@Data
public class ValidateSqlRequest {
    String sqlText;

    Long datasetId;

    List<DataQualityRule> validateRules;

}
