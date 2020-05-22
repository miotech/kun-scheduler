package com.miotech.kun.workflow.core.model.task;

public class Variable {
    private final String name;

    private final String value;

    private final String defaultValue;

    public Variable(String name, String value, String defaultValue) {
        this.name = name;
        this.value = value;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public Variable withValue(String value) {
        return new Variable(this.name, value, this.defaultValue);
    }
}
