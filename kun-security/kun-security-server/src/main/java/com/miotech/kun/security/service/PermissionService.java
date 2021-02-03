package com.miotech.kun.security.service;

import com.miotech.kun.security.model.bo.PermissionRequest;
import com.miotech.kun.security.model.entity.Permission;
import com.miotech.kun.security.model.entity.Permissions;
import com.miotech.kun.security.persistence.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: Jie Chen
 * @created: 2021/1/19
 */
@Service
public class PermissionService extends BaseSecurityService {

    @Autowired
    PermissionRepository permissionRepository;

    public Permissions findByUserId(Long userId) {
        return permissionRepository.findByUserId(userId);
    }

    public Permissions savePermission(PermissionRequest permissionRequest) {
        permissionRequest.setCreateUser(getCurrentUsername());
        permissionRequest.setCreateTime(System.currentTimeMillis());
        permissionRequest.setUpdateUser(getCurrentUsername());
        permissionRequest.setUpdateTime(System.currentTimeMillis());
        return permissionRepository.savePermission(permissionRequest);
    }

    public Long removePermission(Long id) {
        return permissionRepository.removePermission(id);
    }
}
