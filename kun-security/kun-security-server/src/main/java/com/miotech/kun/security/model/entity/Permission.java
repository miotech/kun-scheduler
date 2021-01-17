package com.miotech.kun.security.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.security.model.constant.PermissionType;
import lombok.Data;

/**
 * @author: Jie Chen
 * @created: 2021/1/19
 */
@Data
public class Permission {

    @JsonSerialize(using= ToStringSerializer.class)
    Long id;

    String resourceName;

    PermissionType type;

    String createUser;

    Long createTime;

    String updateUser;

    Long updateTime;

    public String toPermissionString() {
        return resourceName + "_" + type.name();
    }
}
