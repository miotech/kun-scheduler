package com.miotech.kun.security.factory;

import com.miotech.kun.security.model.bo.UserRoleOnModuleReq;

public class MockUserRoleOnModuleReqFactory {

    private MockUserRoleOnModuleReqFactory() {
    }

    public static UserRoleOnModuleReq create(String module, String username) {
        UserRoleOnModuleReq req = new UserRoleOnModuleReq();
        req.setModule(module);
        req.setUsername(username);
        return req;
    }

}
