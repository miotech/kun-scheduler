package com.miotech.kun.security.model.bo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2021/1/19
 */
@Data
public class SavePermissionRequest {

    List<HasPermissionRequest> hasPermissions = new ArrayList<>();

    Long createUser;

    Long createTime;

    Long updateUser;

    Long updateTime;

    public void addHasPermission(HasPermissionRequest hasPermission) {
        hasPermissions.add(hasPermission);
    }
}
