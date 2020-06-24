package com.miotech.kun.datadiscover.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/6/17
 */
@Data
public class Glossary {

    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    private String name;

    private String description;

    private Glossary parent;

    String createUser;

    Long createTime;

    String updateUser;

    Long updateTime;

    List<Asset> assets;
}
