package com.miotech.kun.security.service;

import com.miotech.kun.security.model.bo.HasPermissionRequest;
import com.miotech.kun.security.model.bo.SavePermissionRequest;
import com.miotech.kun.security.model.bo.UserGroupRequest;
import com.miotech.kun.security.model.constant.EntityType;
import com.miotech.kun.security.model.entity.Permissions;
import com.miotech.kun.security.model.entity.UserGroup;
import com.miotech.kun.security.persistence.UserGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author: Jie Chen
 * @created: 2021/2/25
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class UserGroupService extends BaseSecurityService {

    @Autowired
    UserGroupRepository userGroupRepository;

    @Autowired
    PermissionService permissionService;

    public UserGroup findByName(String userGroupName) {
        return userGroupRepository.findByName(userGroupName);
    }

    public UserGroup addUserGroup(UserGroupRequest userGroupRequest) {
        userGroupRequest.setCreateUser(getCurrentUser().getId());
        userGroupRequest.setUpdateUser(getCurrentUser().getId());
        userGroupRequest.setCreateTime(System.currentTimeMillis());
        userGroupRequest.setUpdateTime(System.currentTimeMillis());
        UserGroup userGroup = userGroupRepository.addUserGroup(userGroupRequest);
        SavePermissionRequest savePermissionRequest = new SavePermissionRequest();
        savePermissionRequest.addHasPermission(HasPermissionRequest.builder()
                .subjectId(userGroup.getId())
                .subjectType(EntityType.USER_GROUP)
                .objectId(userGroupRequest.getResourceId())
                .objectType(EntityType.RESOURCE)
                .permissionType(userGroupRequest.getPermissionType())
                .build());
        savePermissionRequest.setCreateUser(getCurrentUser().getId());
        savePermissionRequest.setUpdateUser(getCurrentUser().getId());
        savePermissionRequest.setCreateTime(System.currentTimeMillis());
        savePermissionRequest.setUpdateTime(System.currentTimeMillis());
        Permissions permissions = permissionService.savePermission(savePermissionRequest);
        userGroup.setPermissions(permissions);
        return userGroup;
    }

}
