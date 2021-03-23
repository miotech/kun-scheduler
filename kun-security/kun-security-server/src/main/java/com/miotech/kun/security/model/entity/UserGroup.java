package com.miotech.kun.security.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2021/2/24
 */
@Data
public class UserGroup {

    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    String name;

    @JsonSerialize(using= ToStringSerializer.class)
    List<Long> userIds;

    Permissions permissions;

    String createUser;

    Long createTime;

    String updateUser;

    Long updateTime;
}
