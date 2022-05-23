package com.miotech.kun.security.model.bo;

import com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq;
import lombok.Data;

import java.util.List;

@Data
public class UpdateScopeRequest {

    private String username;

    private String rolename;

    private String module;

    private List<String> sourceSystemIds;

    public static UpdateScopeRequest convertFrom(UpdateScopeOnSpecifiedRoleReq req) {
        UpdateScopeRequest updateScopeRequest = new UpdateScopeRequest();
        updateScopeRequest.setUsername(req.getUsername());
        updateScopeRequest.setRolename(req.getRolename());
        updateScopeRequest.setModule(req.getModule());
        updateScopeRequest.setSourceSystemIds(req.getSourceSystemIdsList());
        return updateScopeRequest;
    }

}
