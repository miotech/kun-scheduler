package com.miotech.kun.security.factory;

import com.miotech.kun.security.model.bo.UserRoleRequest;

import java.util.List;

public class MockUserRoleRequestFactory {

    private MockUserRoleRequestFactory() {
    }

    public static UserRoleRequest create(String username, String module, List<String> sourceSystemIds) {
        UserRoleRequest userRoleRequest = new UserRoleRequest();
        userRoleRequest.setUsername(username);
        userRoleRequest.setModule(module);
        userRoleRequest.setSourceSystemIds(sourceSystemIds);
        return userRoleRequest;
    }

}
