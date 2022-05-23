package com.miotech.kun.security.model.bo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserRoleOnModuleResp {

    private String username;

    private String module;

    private String rolename;

    public UserRoleOnModuleResp(String username, String module) {
        this.username = username;
        this.module = module;
    }
}
