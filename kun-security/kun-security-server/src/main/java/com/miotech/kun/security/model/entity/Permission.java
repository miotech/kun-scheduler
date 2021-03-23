package com.miotech.kun.security.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.security.model.constant.EntityType;
import com.miotech.kun.security.model.constant.PermissionType;
import lombok.Data;

import java.util.StringJoiner;

/**
 * @author: Jie Chen
 * @created: 2021/1/19
 */
@Data
public class Permission {

    @JsonSerialize(using= ToStringSerializer.class)
    Long id;

    String objectName;

    EntityType objectType;

    PermissionType type;

    String createUser;

    Long createTime;

    String updateUser;

    Long updateTime;

    public String toPermissionString() {
        StringJoiner stringJoiner = new StringJoiner("_");
        stringJoiner
                .add(objectName.toUpperCase())
                .add(objectType.name())
                .add(type.name());
        return stringJoiner.toString();
    }
}
