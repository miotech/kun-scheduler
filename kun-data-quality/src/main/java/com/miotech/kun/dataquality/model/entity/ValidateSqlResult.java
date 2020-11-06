package com.miotech.kun.dataquality.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.dataquality.model.ValidateSqlStatus;
import lombok.Data;

import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/7/17
 */
@Data
public class ValidateSqlResult {

    private Integer validateStatus;

    private List<DatasetBasic> relatedTables;

    @JsonProperty("validateMessage")
    private String message;

    public static ValidateSqlResult success(List<DatasetBasic> relatedTables) {
        return buildResult(ValidateSqlStatus.SUCCESS, relatedTables, "");
    }

    public static ValidateSqlResult failed(String errorMessage) {
        return buildResult(ValidateSqlStatus.FAILED, null, errorMessage);
    }

    private static ValidateSqlResult buildResult(ValidateSqlStatus status,
                                                 List<DatasetBasic> relatedTables,
                                                 String message) {
        ValidateSqlResult result = new ValidateSqlResult();
        result.setValidateStatus(status.getFlag());
        result.setRelatedTables(relatedTables);
        result.setMessage(message);
        return result;
    }
}
