package com.miotech.kun.security.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * @author: Jie Chen
 * @created: 2020/6/29
 */
@Data
public class User {

    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    @JsonProperty("username")
    private String name;

    @JsonProperty(value = "password", required = true, access = JsonProperty.Access.WRITE_ONLY)
    String password;

    String createUser;

    Long createTime;

    String updateUser;

    Long updateTime;
}
