package com.miotech.kun.openapi.model.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.metadata.core.model.connection.ConnScope;
import com.miotech.kun.metadata.core.model.connection.ConnectionConfigInfo;
import com.miotech.kun.metadata.core.model.connection.ConnectionInfo;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @program: kun
 * @description:
 * @author: zemin  huang
 * @create: 2022-09-20 14:58
 **/
@Data
public class ConnectionInfoVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long datasourceId;
    private String name;
    private ConnScope connScope;
    private ConnectionConfigInfo connectionConfigInfo;

    public ConnectionInfoVO(ConnectionInfo source) {

        this.id = source.getId();
        this.datasourceId = source.getDatasourceId();
        this.name = source.getName();
        this.connScope = source.getConnScope();
        this.connectionConfigInfo = source.getConnectionConfigInfo();
    }
}
