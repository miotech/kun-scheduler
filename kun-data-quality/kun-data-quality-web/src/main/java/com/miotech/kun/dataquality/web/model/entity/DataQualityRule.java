package com.miotech.kun.dataquality.web.model.entity;

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

    String benchmarkValue;

    String volatility;

    @Override
    public String toString() {
        return "DataQualityRule{" +
                "originalValue='" + originalValue + '\'' +
                ", field='" + field + '\'' +
                ", operator='" + operator + '\'' +
                ", expectedType='" + expectedType + '\'' +
                ", expectedValue='" + expectedValue + '\'' +
                ", benchmarkValue='" + benchmarkValue + '\'' +
                ", volatility='" + volatility + '\'' +
                '}';
    }
}
