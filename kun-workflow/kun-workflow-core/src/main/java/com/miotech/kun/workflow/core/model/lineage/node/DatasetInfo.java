package com.miotech.kun.workflow.core.model.lineage.node;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DatasetInfo {

    private final Long gid;

    private final String name;

    @JsonCreator
    public DatasetInfo(@JsonProperty("gid") Long gid, @JsonProperty("name") String name) {
        this.gid = gid;
        this.name = name;
    }

    public Long getGid() {
        return gid;
    }

    public String getName() {
        return name;
    }

}
