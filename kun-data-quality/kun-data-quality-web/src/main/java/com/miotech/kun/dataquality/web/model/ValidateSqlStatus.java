package com.miotech.kun.dataquality.web.model;

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

    public static ValidateSqlStatus convert(Integer flag) {
        for (ValidateSqlStatus value : values()) {
            if (value.getFlag().equals(flag)) {
                return value;
            }
        }

        return null;
    }

}
