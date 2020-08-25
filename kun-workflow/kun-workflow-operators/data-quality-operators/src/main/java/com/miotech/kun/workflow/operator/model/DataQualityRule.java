package com.miotech.kun.workflow.operator.model;

import java.util.StringJoiner;

/**
 * @author: Jie Chen
 * @created: 2020/7/14
 */
public class DataQualityRule {

    String field;

    String operator;

    String expectedType;

    String expectedValue;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getExpectedType() {
        return expectedType;
    }

    public void setExpectedType(String expectedType) {
        this.expectedType = expectedType;
    }

    public String getExpectedValue() {
        return expectedValue;
    }

    public void setExpectedValue(String expectedValue) {
        this.expectedValue = expectedValue;
    }

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
