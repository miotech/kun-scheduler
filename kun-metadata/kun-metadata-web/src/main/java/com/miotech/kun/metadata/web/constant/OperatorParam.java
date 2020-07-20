package com.miotech.kun.metadata.web.constant;

import com.google.common.base.Preconditions;

public enum OperatorParam {
    BUILD_ALL(WorkflowApiParam.OPERATOR_NAME_BUILD_ALL, WorkflowApiParam.PACKAGE_PATH_BUILD_ALL, PropKey.OPERATOR_ID_BUILD_ALL),
    REFRESH(WorkflowApiParam.OPERATOR_NAME_REFRESH, WorkflowApiParam.PACKAGE_PATH_REFRESH, PropKey.OPERATOR_ID_REFRESH);

    private String name;
    private String packagePath;
    private String operatorKey;

    public String getName() {
        return name;
    }

    public String getPackagePath() {
        return packagePath;
    }

    public String getOperatorKey() {
        return operatorKey;
    }

    OperatorParam(String name, String packagePath, String operatorKey) {
        this.name = name;
        this.packagePath = packagePath;
        this.operatorKey = operatorKey;
    }

    public static OperatorParam get(String name) {
        Preconditions.checkNotNull(name);
        for (OperatorParam value : values()) {
            if (name.equals(value.name)) {
                return value;
            }
        }
        throw new IllegalArgumentException("OperatorParam Not Found, name: " + name);
    }

}
