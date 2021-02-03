package com.miotech.kun.security.model.bo;

import com.miotech.kun.security.model.constant.PermissionType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2021/1/19
 */
@Data
public class PermissionRequest {

    List<HasPermission> hasPermissions = new ArrayList<>();

    String createUser;

    Long createTime;

    String updateUser;

    Long updateTime;

    public void addHasPermission(HasPermission hasPermission) {
        hasPermissions.add(hasPermission);
    }
}
