package com.miotech.kun.datadiscovery.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Jie Chen
 * @created: 6/12/20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceTemplateVO {

    @JsonProperty("type")
    private String type;

    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

}
