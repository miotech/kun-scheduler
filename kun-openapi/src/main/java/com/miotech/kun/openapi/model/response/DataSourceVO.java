package com.miotech.kun.openapi.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.metadata.core.model.connection.DatasourceConnection;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.metadata.core.model.datasource.DatasourceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: Melo
 * @created: 5/26/20
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private DatasourceType datasourceType;

    private String name;

    @JsonProperty("information")
    private DatasourceConnectionVO datasourceConnection;

    private List<String> tags;

    public DataSourceVO(DataSource ds) {
        this.id = ds.getId();
        this.datasourceType = ds.getDatasourceType();
        this.name = ds.getName();
        this.datasourceConnection = convert(ds.getDatasourceConnection());
    }

    private DatasourceConnectionVO convert(DatasourceConnection datasourceConnection) {
        return new DatasourceConnectionVO(datasourceConnection.getUserConnectionList());
    }
}
