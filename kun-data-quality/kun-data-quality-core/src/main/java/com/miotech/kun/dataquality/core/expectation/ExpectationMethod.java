package com.miotech.kun.dataquality.core.expectation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.sql.ResultSet;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class ExpectationMethod {

    private final Mode mode;

    public ExpectationMethod(Mode mode) {
        this.mode = mode;
    }

    public Mode getMode() {
        return mode;
    }

    @JsonIgnore
    public abstract ValidationResult validate(ResultSet rs);

    public enum Mode {
        JDBC    // 通过JDBC执行sql后验证的模式
        ;

    }

}
