package com.miotech.kun.datadiscovery.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/6/17
 */
@Data
public class Asset {

    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    private String type;

    private String name;

    private String datasource;

    private String database;

    private List<String> owner;

    private String description;

    private Boolean deleted;

}
