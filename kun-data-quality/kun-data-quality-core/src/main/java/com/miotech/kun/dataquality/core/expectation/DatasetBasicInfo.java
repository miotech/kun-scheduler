package com.miotech.kun.dataquality.core.expectation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DatasetBasicInfo {

    private final Long gid;

    private final Long dataSourceId;

    @JsonCreator
    public DatasetBasicInfo(@JsonProperty("gid") Long gid, @JsonProperty("dataSourceId") Long dataSourceId) {
        this.gid = gid;
        this.dataSourceId = dataSourceId;
    }

    public Long getGid() {
        return gid;
    }

    public Long getDataSourceId() {
        return dataSourceId;
    }
}
