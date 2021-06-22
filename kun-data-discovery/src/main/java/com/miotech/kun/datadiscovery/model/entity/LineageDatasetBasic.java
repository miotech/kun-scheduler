package com.miotech.kun.datadiscovery.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * @author: Jie Chen
 * @created: 2020/10/25
 */
@Data
public class LineageDatasetBasic {

    @JsonSerialize(using= ToStringSerializer.class)
    Long gid;

    String name;

    String datasource;

    String type;

    Long rowCount;

    Watermark highWatermark;

    Boolean deleted;
}
