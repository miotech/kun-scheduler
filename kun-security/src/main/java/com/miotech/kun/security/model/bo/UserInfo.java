package com.miotech.kun.security.model.bo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@Data
public class UserInfo implements Serializable {

    @JsonProperty("id")
    private Long id;

    @JsonProperty(value = "username", required = true)
    private String username;

    @JsonProperty(value = "password", required = true, access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private Set<String> permissions;
}
