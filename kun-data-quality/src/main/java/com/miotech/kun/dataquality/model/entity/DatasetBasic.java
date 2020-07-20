package com.miotech.kun.dataquality.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Melo
 * @created: 5/26/20
 */

@Data
@NoArgsConstructor
public class DatasetBasic {
    @JsonProperty("id")
    @JsonSerialize(using= ToStringSerializer.class)
    private Long gid;

    private String name;

    private String datasource;
}
