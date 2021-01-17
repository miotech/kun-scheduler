package com.miotech.kun.security.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@Data
public class UserInfo implements Serializable {

    @JsonProperty("id")
    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    @JsonProperty(value = "username", required = true)
    private String username;

    @JsonProperty(value = "password", required = true, access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private Set<String> permissions;

    String createUser;

    Long createTime;

    String updateUser;

    Long updateTime;
}
