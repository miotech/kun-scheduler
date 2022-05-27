package com.miotech.kun.security.controller;

import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.security.model.bo.BindUserRequest;
import com.miotech.kun.security.model.bo.UpdateScopeRequest;
import com.miotech.kun.security.model.bo.UserRoleRequest;
import com.miotech.kun.security.model.bo.UserRoleWithScope;
import com.miotech.kun.security.service.UserRoleScopeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/kun/api/v1/security/user-role")
public class UserRoleController {

    @Autowired
    private UserRoleScopeService userRoleScopeService;

    @GetMapping("resource/roles")
    public RequestResult<UserRoleWithScope> getRolesOfResource(@RequestBody UserRoleRequest userRoleRequest) {
        UserRoleWithScope userRoleWithScope = userRoleScopeService.findRoleOnSpecifiedResources(userRoleRequest);
        return RequestResult.success(userRoleWithScope);
    }

    @PostMapping("/_bind")
    public RequestResult<Object> bindUser(@RequestBody BindUserRequest bindUserRequest) {
        userRoleScopeService.bindUser(bindUserRequest);
        return RequestResult.success();
    }

    @PostMapping("/_addScope")
    public RequestResult<Object> addScope(@RequestBody UpdateScopeRequest updateScopeRequest) {
        userRoleScopeService.addScopeOnSpecifiedRole(updateScopeRequest);
        return RequestResult.success();
    }

    @PostMapping("/_deleteScope")
    public RequestResult<Object> deleteScope(@RequestBody UpdateScopeRequest updateScopeRequest) {
        userRoleScopeService.deleteScopeOnSpecifiedRole(updateScopeRequest);
        return RequestResult.success();
    }

}
