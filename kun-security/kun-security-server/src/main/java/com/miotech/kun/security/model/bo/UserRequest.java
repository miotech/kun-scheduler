package com.miotech.kun.security.model.bo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.security.model.AuthenticationOriginInfo;
import com.miotech.kun.security.model.UserInfo;
import lombok.Builder;
import lombok.Data;

/**
 * @author: Jie Chen
 * @created: 2021/3/30
 */
@Data
@Builder
public class UserRequest {

    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    @JsonProperty(required = true)
    private String username;

    @JsonProperty(required = true, access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private AuthenticationOriginInfo authOriginInfo;

    String firstName;

    String lastName;

    String email;

    String weComId;

    Long createUser;

    Long createTime;

    Long updateUser;

    Long updateTime;

    public static UserRequest convertFrom(UserInfo userInfo) {
        return UserRequest.builder()
                .id(userInfo.getId())
                .username(userInfo.getUsername())
                .authOriginInfo(userInfo.getAuthOriginInfo())
                .firstName(userInfo.getFirstName())
                .lastName(userInfo.getLastName())
                .email(userInfo.getEmail())
                .build();
    }
}
