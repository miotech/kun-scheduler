package com.miotech.kun.metadata.databuilder.constant;

import io.prestosql.jdbc.$internal.guava.base.Preconditions;

public enum  DataBuilderDeployMode {

    ALL,
    DATASOURCE,
    DATASET;

    public static DataBuilderDeployMode resolve(String method) {
        Preconditions.checkNotNull(method);
        return valueOf(method.toUpperCase());
    }

}
