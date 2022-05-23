package com.miotech.kun.security.model.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleScope {

    private static final String Separator = ":";

    private String username;

    private String module;

    private String sourceSystemId;

    private String rolename;

}
