package com.miotech.kun.workflow.operator.model;

import lombok.Data;

import java.util.StringJoiner;

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

    @Override
    public String toString() {
        return new StringJoiner(", ", "[", "]")
                .add("field: " + field)
                .add("operator: " + operator)
                .add("expectedType: " + expectedType)
                .add("expectedValue: " + expectedValue)
                .toString();
    }
}
