package com.miotech.kun.datadiscovery.model.entity;

import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.NullSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.commons.utils.CustomDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * @program: kun
 * @description: glossary  base info
 * @author: zemin  huang
 * @create: 2022-01-24 09:23
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlossaryBasicInfo {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String name;

    private String description;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long parentId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long prevId;

    private String createUser;

    @JsonSerialize(using = CustomDateTimeSerializer.class)
    private OffsetDateTime createTime;

    private String updateUser;

    @JsonSerialize(using = CustomDateTimeSerializer.class)
    private OffsetDateTime updateTime;

    private boolean deleted;

}
