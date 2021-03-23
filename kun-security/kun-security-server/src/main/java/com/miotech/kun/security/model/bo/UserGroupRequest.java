package com.miotech.kun.security.model.bo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.miotech.kun.security.model.constant.PermissionType;
import lombok.Data;

import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2021/2/24
 */
@Data
public class UserGroupRequest {

    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    String groupName;

    List<Long> userIds;

    Long resourceId;

    PermissionType permissionType;

    Long createUser;

    Long createTime;

    Long updateUser;

    Long updateTime;
}
