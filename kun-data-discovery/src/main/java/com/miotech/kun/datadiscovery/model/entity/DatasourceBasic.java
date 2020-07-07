package com.miotech.kun.datadiscovery.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * @author: Jie Chen
 * @created: 2020/6/22
 */
@Data
public class DatasourceBasic {

    @JsonSerialize(using= ToStringSerializer.class)
    Long id;

    String name;
}
