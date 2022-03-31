package com.miotech.kun.dataquality.core.expectation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import lombok.Builder;

@Builder
public class Dataset {

    private Long gid;

    @JsonIgnore
    private DataSource dataSource;

    @JsonCreator
    public Dataset(@JsonProperty("gid") Long gid) {
        this.gid = gid;
    }

    public Dataset(Long gid, DataSource dataSource) {
        this.gid = gid;
        this.dataSource = dataSource;
    }

    public Long getGid() {
        return gid;
    }

    public void setGid(Long gid) {
        this.gid = gid;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
