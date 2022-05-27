package com.miotech.kun.security.model.bo;

import com.miotech.kun.security.facade.rpc.RoleOnSpecifiedModuleReq;
import lombok.Data;

@Data
public class UserRoleOnModuleReq {

    private String username;

    private String module;

    public static UserRoleOnModuleReq convertFrom(RoleOnSpecifiedModuleReq request) {
        UserRoleOnModuleReq req = new UserRoleOnModuleReq();
        req.setUsername(request.getUsername());
        req.setModule(request.getModule());
        return req;
    }
}
