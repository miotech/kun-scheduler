package com.miotech.kun.datadiscover.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

    @JsonProperty("database_name")
    private String databaseName;

    private String schema;

    private String description;

    private String type;

    @JsonProperty("high_watermark")
    private Watermark highWatermark;

    private List<String> owners;

    private List<String> tags;
}
