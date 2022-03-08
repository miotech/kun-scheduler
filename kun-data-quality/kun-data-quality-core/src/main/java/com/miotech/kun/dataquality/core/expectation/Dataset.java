package com.miotech.kun.dataquality.core.expectation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Dataset {

    private Long gid;

    private DataSource dataSource;

    @JsonCreator
    public Dataset(@JsonProperty("gid") Long gid, @JsonProperty("dataSource") DataSource dataSource) {
        this.gid = gid;
        this.dataSource = dataSource;
    }
}
