package com.miotech.kun.security.factory;

import com.miotech.kun.security.model.bo.Role;

public class MockRoleFactory {

    private MockRoleFactory() {
    }

    public static Role create() {
        return create("test", "viewer");
    }

    public static Role create(String module, String name) {
        Role role = new Role();
        role.setModule(module);
        role.setName(name);
        role.setDescription("desc");
        return role;
    }

}
