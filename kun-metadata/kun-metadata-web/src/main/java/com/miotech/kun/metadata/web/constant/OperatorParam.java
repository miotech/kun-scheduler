package com.miotech.kun.metadata.web.constant;

public enum OperatorParam {
    MCE("mce-operator", "", "com.miotech.kun.metadata.databuilder.operator.MCEOperator"),
    MSE("mse-operator", "", "com.miotech.kun.metadata.databuilder.operator.MSEOperator")
    ;

    private String name;

    private String description;

    private String className;

    OperatorParam(String name, String description, String className) {
        this.name = name;
        this.description = description;
        this.className = className;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getClassName() {
        return className;
    }

    public static OperatorParam convert(String name) {
        for (OperatorParam value : values()) {
            if (value.name.equals(name)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Invalid name: " + name);
    }

}
