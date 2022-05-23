package com.miotech.kun.security.factory;

import com.miotech.kun.security.model.bo.UserRoleScope;

public class MockUserRoleScopeFactory {

    private MockUserRoleScopeFactory() {
    }

    public static UserRoleScope create() {
        return UserRoleScope.builder()
                .username("admin")
                .module("test")
                .rolename("viewer")
                .sourceSystemId("id_1")
                .build();
    }

}
