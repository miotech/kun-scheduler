package com.miotech.kun.metadata.core.model.constant;

import com.google.common.base.Preconditions;

public enum  DataBuilderDeployMode {

    ALL,
    DATASOURCE,
    DATASET,
    PUSH;

    public static DataBuilderDeployMode resolve(String method) {
        Preconditions.checkNotNull(method);
        return valueOf(method.toUpperCase());
    }

}
