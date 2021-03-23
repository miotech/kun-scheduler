package com.miotech.kun.security.controller;

import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.security.model.bo.UserGroupRequest;
import com.miotech.kun.security.model.entity.UserGroup;
import com.miotech.kun.security.service.UserGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: Jie Chen
 * @created: 2021/2/27
 */
@RestController
@RequestMapping("/kun/api/v1/security/user-group")
public class UserGroupController {

    @Autowired
    UserGroupService userGroupService;

    @PostMapping("/add")
    public RequestResult<UserGroup> addGroup(@RequestBody UserGroupRequest userGroupRequest) {
        return RequestResult.success(userGroupService.addUserGroup(userGroupRequest));
    }

}
