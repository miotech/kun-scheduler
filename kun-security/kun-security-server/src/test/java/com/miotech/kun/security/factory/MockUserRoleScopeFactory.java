package com.miotech.kun.security.factory;

import com.miotech.kun.security.model.bo.UserRoleScope;

public class MockUserRoleScopeFactory {

    private MockUserRoleScopeFactory() {
    }



    public static UserRoleScope create() {
        return create("test_module");
    }

    public static UserRoleScope create(String module) {
        return UserRoleScope.builder()
                .username("admin")
                .module(module)
                .rolename("viewer")
                .sourceSystemId("id_1")
                .build();
    }

}
