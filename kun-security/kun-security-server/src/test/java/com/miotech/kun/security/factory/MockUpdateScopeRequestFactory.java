package com.miotech.kun.security.factory;

import com.miotech.kun.security.model.bo.UpdateScopeRequest;

import java.util.List;

public class MockUpdateScopeRequestFactory {

    private MockUpdateScopeRequestFactory() {
    }

    public static UpdateScopeRequest create(String username, String module, String rolename, List<String> sourceSystemIds) {
        UpdateScopeRequest updateScopeRequest = new UpdateScopeRequest();
        updateScopeRequest.setUsername(username);
        updateScopeRequest.setModule(module);
        updateScopeRequest.setRolename(rolename);
        updateScopeRequest.setSourceSystemIds(sourceSystemIds);
        return updateScopeRequest;
    }

    public static UpdateScopeRequest create(String module, String rolename, List<String> sourceSystemIds) {
        return create("admin", module, rolename, sourceSystemIds);
    }

}
