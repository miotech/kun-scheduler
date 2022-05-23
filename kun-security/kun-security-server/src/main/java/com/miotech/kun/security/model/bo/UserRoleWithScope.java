package com.miotech.kun.security.model.bo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class UserRoleWithScope {

    private String username;

    private String module;

    private List<ResourceRole> resourceRoles;

    public UserRoleWithScope(String username, String module) {
        this.username = username;
        this.module = module;
    }
}
