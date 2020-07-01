package com.miotech.kun.metadata.databuilder.constant;

import javax.annotation.Nullable;

public enum  DataBuilderDeployMode {

    ALL,
    DATASOURCE,
    DATASET;

    public static DataBuilderDeployMode resolve(@Nullable String method) {
        return valueOf(method.toUpperCase());
    }

}
