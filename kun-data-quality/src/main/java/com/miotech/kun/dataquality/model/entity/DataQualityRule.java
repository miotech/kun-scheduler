package com.miotech.kun.dataquality.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Data;

import java.util.StringJoiner;

/**
 * @author: Jie Chen
 * @created: 2020/7/14
 */
@Data
public class DataQualityRule {

    @JsonProperty("fieldName")
    @JsonSetter(nulls = Nulls.SKIP)
    String field = "";

    @JsonProperty("operator")
    String operator;

    @JsonProperty("fieldType")
    String expectedType;

    @JsonProperty("fieldValue")
    String expectedValue;

    @Override
    public String toString() {
        return new StringJoiner(", ", "[", "]")
                .add("field=" + field)
                .add("operator=" + operator)
                .add("expectedType=" + expectedType)
                .add("expectedValue=" + expectedValue)
                .toString();
    }
}
