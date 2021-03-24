package com.miotech.kun.security.controller;

import com.google.common.collect.Sets;
import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.security.SecurityContextHolder;
import com.miotech.kun.security.model.UserInfo;
import com.miotech.kun.security.model.bo.HasPermissionRequest;
import com.miotech.kun.security.model.constant.EntityType;
import com.miotech.kun.security.model.entity.Permission;
import com.miotech.kun.security.model.entity.Permissions;
import com.miotech.kun.security.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

/**
 * @author: Jie Chen
 * @created: 2020/7/1
 */
@RestController
@RequestMapping("/kun/api/v1/security")
public class SecurityController {

    @Autowired
    PermissionService permissionService;

    @GetMapping("/whoami")
    public RequestResult<UserInfo> whoami() {
        UserInfo userInfo = SecurityContextHolder.getUserInfo();
        UserInfo authUserInfo = (UserInfo) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getDetails();
        userInfo.setAuthOriginInfo(authUserInfo.getAuthOriginInfo());
        HasPermissionRequest permissionRequest = HasPermissionRequest.builder()
                .subjectId(userInfo.getId())
                .subjectType(EntityType.USER)
                .build();
        Permissions permissions = permissionService.find(permissionRequest);
        userInfo.setPermissions(Sets.newHashSet(permissions.getPermissions().stream()
                .map(Permission::toPermissionString).collect(Collectors.toList())));
        return RequestResult.success(userInfo);
    }
}
