package com.miotech.kun.datadiscovery.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
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
public class DatasetBasic {
    @JsonProperty("id")
    @JsonSerialize(using= ToStringSerializer.class)
    private Long gid;

    private String name;

    private String datasource;

    private String database;

    private String schema;

    private String description;

    private String type;

    private Watermark highWatermark;

    private Watermark lowWatermark;

    private List<String> owners;

    private List<String> tags;

    private List<GlossaryBasic> glossaries;

    private Boolean deleted;
}
