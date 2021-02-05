package com.miotech.kun.metadata.core.model;

import java.io.Serializable;

public class DatasetFieldMapping implements Serializable {

    private final Long datasourceId;

    private final String pattern;

    private final String type;

    public DatasetFieldMapping(Long datasourceId, String pattern, String type) {
        this.datasourceId = datasourceId;
        this.pattern = pattern;
        this.type = type;
    }

    public Long getDatasourceId() {
        return datasourceId;
    }

    public String getPattern() {
        return pattern;
    }

    public String getType() {
        return type;
    }

}
