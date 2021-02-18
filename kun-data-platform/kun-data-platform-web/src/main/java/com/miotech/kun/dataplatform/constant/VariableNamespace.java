package com.miotech.kun.dataplatform.constant;

public class VariableNamespace {
    private final String namespace;

    public VariableNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public String toString() {
        return this.namespace;
    }
}
