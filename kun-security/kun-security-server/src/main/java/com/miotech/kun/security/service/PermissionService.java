package com.miotech.kun.security.service;

import com.miotech.kun.security.model.bo.HasPermissionRequest;
import com.miotech.kun.security.model.bo.SavePermissionRequest;
import com.miotech.kun.security.model.entity.Permissions;
import com.miotech.kun.security.persistence.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author: Jie Chen
 * @created: 2021/1/19
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class PermissionService extends BaseSecurityService {

    @Autowired
    PermissionRepository permissionRepository;

    public Permissions find(HasPermissionRequest permissionRequest) {
        return permissionRepository.find(permissionRequest);
    }

    public Permissions savePermission(SavePermissionRequest savePermissionRequest) {
        savePermissionRequest.setCreateUser(getCurrentUser().getId());
        savePermissionRequest.setCreateTime(System.currentTimeMillis());
        savePermissionRequest.setUpdateUser(getCurrentUser().getId());
        savePermissionRequest.setUpdateTime(System.currentTimeMillis());
        return permissionRepository.savePermission(savePermissionRequest);
    }

    public Long removePermission(Long id) {
        return permissionRepository.removePermission(id);
    }
}
