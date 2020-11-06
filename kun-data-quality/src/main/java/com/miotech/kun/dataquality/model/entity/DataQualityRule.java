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

    String originalValue;

    @JsonSetter(nulls = Nulls.SKIP)
    String field = "";

    String operator;

    String expectedType;

    String expectedValue;

    @Override
    public String toString() {
        return new StringJoiner(", ", "[", "]")
                .add("originalValue=" + originalValue)
                .add("field=" + field)
                .add("operator=" + operator)
                .add("expectedType=" + expectedType)
                .add("expectedValue=" + expectedValue)
                .toString();
    }
}
