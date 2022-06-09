package com.miotech.kun.security.model.bo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
public class UserRoleOnModuleResp {

    private String username;

    private String module;

    private Set<String> rolenames;

    public UserRoleOnModuleResp(String username, String module) {
        this.username = username;
        this.module = module;
    }
}
