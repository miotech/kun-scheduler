package com.miotech.kun.dataquality.core;

import com.miotech.kun.metadata.core.model.datasource.DataSource;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Dataset {

    private Long gid;

    private DataSource dataSource;

}
