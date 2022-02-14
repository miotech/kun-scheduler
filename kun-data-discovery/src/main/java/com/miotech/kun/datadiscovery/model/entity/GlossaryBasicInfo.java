package com.miotech.kun.datadiscovery.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.commons.utils.CustomDateTimeSerializer;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @program: kun
 * @description: glossary  base info
 * @author: zemin  huang
 * @create: 2022-01-24 09:23
 **/
@Data
public class GlossaryBasicInfo {

    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    private String name;

    private String description;

    private Long parentId;

    String createUser;

    @JsonSerialize(using = CustomDateTimeSerializer.class)
    OffsetDateTime createTime;

    String updateUser;

    @JsonSerialize(using = CustomDateTimeSerializer.class)
    OffsetDateTime updateTime;

}
