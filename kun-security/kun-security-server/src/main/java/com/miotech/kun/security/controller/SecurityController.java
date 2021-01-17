package com.miotech.kun.security.controller;

import com.google.common.collect.Sets;
import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.security.SecurityContextHolder;
import com.miotech.kun.security.model.UserInfo;
import com.miotech.kun.security.model.entity.Permission;
import com.miotech.kun.security.model.entity.Permissions;
import com.miotech.kun.security.model.entity.User;
import com.miotech.kun.security.model.vo.UserListVO;
import com.miotech.kun.security.service.AbstractSecurityService;
import com.miotech.kun.security.service.PermissionService;
import com.miotech.kun.security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: Jie Chen
 * @created: 2020/7/1
 */
@RestController
@RequestMapping("/kun/api/v1/security")
public class SecurityController {

    @Autowired
    UserService userService;

    @Autowired
    PermissionService permissionService;

    @Autowired
    AbstractSecurityService abstractSecurityService;

    @GetMapping("/whoami")
    public RequestResult<UserInfo> whoami() {
        UserInfo userInfo = SecurityContextHolder.getUserInfo();
        Permissions permissions = permissionService.findByUserId(userInfo.getId());
        userInfo.setPermissions(Sets.newHashSet(permissions.getPermissions().stream()
                .map(Permission::toPermissionString).collect(Collectors.toList())));
        return RequestResult.success(userInfo);
    }

    @GetMapping("/user/search")
    public RequestResult<UserListVO> getUsers(@RequestParam("keyword") String keyword) {
        UserListVO vo = new UserListVO();
        vo.setUsers(userService.getUsers().stream().map(User::getName).collect(Collectors.toList()));
        return RequestResult.success(vo);
    }

    @GetMapping("/user/list")
    public RequestResult<List<UserInfo>> getUserList() {
        List<UserInfo> userInfos = userService.getUsers().stream()
                .map(abstractSecurityService::convertToUserInfo)
                .collect(Collectors.toList());
        return RequestResult.success(userInfos);
    }

    @GetMapping("/user/{id}")
    public RequestResult<UserInfo> getUser(@PathVariable("id") Long id) {
        User user = userService.getUser(id);
        UserInfo userInfo = abstractSecurityService.convertToUserInfo(user);
        return RequestResult.success(userInfo);
    }
}
