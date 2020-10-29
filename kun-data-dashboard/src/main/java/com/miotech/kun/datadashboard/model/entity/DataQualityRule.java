package com.miotech.kun.datadashboard.model.entity;

import lombok.Data;

/**
 * @author: Jie Chen
 * @created: 2020/7/14
 */
@Data
public class DataQualityRule {

    String originalValue;

    String field;

    String operator;

    String expectedType;

    String expectedValue;
}
