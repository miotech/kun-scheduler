package com.miotech.kun.dataquality.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.dataquality.model.ValidateSqlStatus;
import lombok.Data;

/**
 * @author: Jie Chen
 * @created: 2020/7/17
 */
@Data
public class ValidateSqlResult {

    private Integer validateStatus;

    @JsonProperty("validateMessage")
    private String message;

    public static ValidateSqlResult success() {
        return buildResult(ValidateSqlStatus.SUCCESS, "");
    }

    public static ValidateSqlResult failed(String errorMessage) {
        return buildResult(ValidateSqlStatus.FAILED, errorMessage);
    }

    private static ValidateSqlResult buildResult(ValidateSqlStatus status,
                                                 String message) {
        ValidateSqlResult result = new ValidateSqlResult();
        result.setValidateStatus(status.getFlag());
        result.setMessage(message);
        return result;
    }
}
