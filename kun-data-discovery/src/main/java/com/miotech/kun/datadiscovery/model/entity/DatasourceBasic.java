package com.miotech.kun.datadiscovery.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Jie Chen
 * @created: 2020/6/22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatasourceBasic {

    @JsonSerialize(using= ToStringSerializer.class)
    Long id;

    String name;
}
