package com.miotech.kun.datadiscovery.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.datadiscovery.model.entity.SecurityInfo;
import com.miotech.kun.metadata.core.model.connection.ConnScope;
import com.miotech.kun.metadata.core.model.connection.ConnectionConfigInfo;
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
    private String description;
    private Boolean deleted;
    private String updateUser;
    private OffsetDateTime updateTime;
    private String createUser;
    private OffsetDateTime createTime;

}
