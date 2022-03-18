package com.miotech.kun.dataquality.web.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.dataquality.web.model.ValidateSqlStatus;
import lombok.Data;

import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/7/17
 */
@Data
public class ValidateMetricsResult {

    private Integer validateStatus;

    private List<DatasetBasic> relatedTables;

    @JsonProperty("validateMessage")
    private String message;

    public static ValidateMetricsResult success() {
        return buildResult(ValidateSqlStatus.SUCCESS, null, "");
    }

    public static ValidateMetricsResult success(List<DatasetBasic> relatedTables) {
        return buildResult(ValidateSqlStatus.SUCCESS, relatedTables, "");
    }

    public static ValidateMetricsResult failed(String errorMessage) {
        return buildResult(ValidateSqlStatus.FAILED, null, errorMessage);
    }

    private static ValidateMetricsResult buildResult(ValidateSqlStatus status,
                                                     List<DatasetBasic> relatedTables,
                                                     String message) {
        ValidateMetricsResult result = new ValidateMetricsResult();
        result.setValidateStatus(status.getFlag());
        result.setRelatedTables(relatedTables);
        result.setMessage(message);
        return result;
    }

    public boolean isSuccess() {
        ValidateSqlStatus status  = ValidateSqlStatus.convert(validateStatus);
        return status == null ? false : status.equals(ValidateSqlStatus.SUCCESS);
    }

}
