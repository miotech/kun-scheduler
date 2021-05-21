package com.miotech.kun.metadata.core.model.process;

import java.util.Locale;

public enum PullProcessType {
    DATASOURCE,
    DATASET;

    public static PullProcessType from(String type) {
        switch (type.toUpperCase()) {
            case "DATASOURCE":
                return DATASOURCE;
            case "DATASET":
                return DATASET;
            default:
                throw new IllegalArgumentException(String.format("Unknown name \"%s\" for enum type `PullProcessType`.", type));
        }
    }
}
