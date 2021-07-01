package com.miotech.kun.metadata.core.model.dataset;

import java.io.Serializable;

public class DatasetFieldMapping implements Serializable {

    private final String datasourceType;

    private final String pattern;

    private final String type;

    public DatasetFieldMapping(String datasourceType, String pattern, String type) {
        this.datasourceType = datasourceType;
        this.pattern = pattern;
        this.type = type;
    }

    public String getDatasourceType() {
        return datasourceType;
    }

    public String getPattern() {
        return pattern;
    }

    public String getType() {
        return type;
    }

}
