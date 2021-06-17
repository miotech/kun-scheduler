package com.miotech.kun.datadiscovery.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.commons.utils.CustomDateTimeSerializer;
import lombok.Data;

import java.time.OffsetDateTime;
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

    @JsonSerialize(using = CustomDateTimeSerializer.class)
    OffsetDateTime createTime;

    String updateUser;

    @JsonSerialize(using = CustomDateTimeSerializer.class)
    OffsetDateTime updateTime;

    List<Asset> assets;
}
