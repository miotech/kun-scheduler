package com.miotech.kun.dataquality.model.entity;

import com.miotech.kun.dataquality.model.ValidateSqlStatus;
import lombok.Data;

/**
 * @author: Jie Chen
 * @created: 2020/7/17
 */
@Data
public class ValidateSqlResult {

    private Integer validateStatus;

    public static ValidateSqlResult success() {
        return buildResult(ValidateSqlStatus.SUCCESS);
    }

    public static ValidateSqlResult failed() {
        return buildResult(ValidateSqlStatus.FAILED);
    }

    private static ValidateSqlResult buildResult(ValidateSqlStatus status) {
        ValidateSqlResult result = new ValidateSqlResult();
        result.setValidateStatus(status.getFlag());
        return result;
    }
}
