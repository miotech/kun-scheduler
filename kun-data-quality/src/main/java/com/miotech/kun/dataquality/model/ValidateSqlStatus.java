package com.miotech.kun.dataquality.model;

/**
 * @author: Jie Chen
 * @created: 2020/7/17
 */
public enum ValidateSqlStatus {

    SUCCESS(0),

    FAILED(1);

    private Integer flag;
    ValidateSqlStatus(Integer flag) {
        this.flag = flag;
    }

    public Integer getFlag() {
        return flag;
    }
}
