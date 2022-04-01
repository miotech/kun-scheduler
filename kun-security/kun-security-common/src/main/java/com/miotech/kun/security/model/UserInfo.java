package com.miotech.kun.security.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@Data
public class UserInfo implements Serializable {

    private static final long serialVersionUID = -6013170276960874830L;

    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    @JsonProperty(required = true)
    private String username;

    @JsonProperty(required = true, access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private Set<String> permissions;

    private AuthenticationOriginInfo authOriginInfo;

    private String firstName;

    private String lastName;

    private String email;

    private String weComId;

    private Long userGroupId;

}
