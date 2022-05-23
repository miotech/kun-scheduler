package com.miotech.kun.security.model.bo;

import com.miotech.kun.security.facade.rpc.RoleOnSpecifiedResourcesReq;
import lombok.Data;

import java.util.List;

@Data
public class UserRoleRequest {

    private String username;

    private String module;

    private List<String> sourceSystemIds;

    public static UserRoleRequest convertFrom(RoleOnSpecifiedResourcesReq userRoleReq) {
        UserRoleRequest userRoleRequest = new UserRoleRequest();
        userRoleRequest.setUsername(userRoleReq.getUsername());
        userRoleRequest.setModule(userRoleReq.getModule());
        userRoleRequest.setSourceSystemIds(userRoleReq.getSourceSystemIdsList());
        return userRoleRequest;
    }

}
