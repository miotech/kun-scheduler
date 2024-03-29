package com.miotech.kun.security.service;

import com.google.common.collect.Lists;
import com.miotech.kun.security.dao.UserRoleScopeDao;
import com.miotech.kun.security.model.bo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserRoleScopeService {

    @Autowired
    private UserRoleScopeDao userRoleScopeDao;

    public UserRoleOnModuleResp findRoleOnSpecifiedModule(UserRoleOnModuleReq req) {
        UserRoleOnModuleResp resp = new UserRoleOnModuleResp(req.getUsername(), req.getModule());
        List<UserRoleScope> userRoleScopes = userRoleScopeDao.findByUsernameAndModule(req.getUsername(), req.getModule());
        Set<String> rolenames = userRoleScopes.stream().map(UserRoleScope::getRolename).collect(Collectors.toSet());
        resp.setRolenames(rolenames);
        return resp;
    }

    public UserRoleWithScope findRoleOnSpecifiedResources(UserRoleRequest userRoleRequest) {
        UserRoleWithScope userRoleWithScope = new UserRoleWithScope(userRoleRequest.getUsername(), userRoleRequest.getModule());
        List<UserRoleScope> userRoleScopes = userRoleScopeDao.findByUsernameAndModule(userRoleRequest.getUsername(), userRoleRequest.getModule());
        Map<String, UserRoleScope> userRoleScopeMap = userRoleScopes.stream().collect(Collectors.toMap(UserRoleScope::getSourceSystemId, Function.identity()));
        List<ResourceRole> resourceRoles = Lists.newArrayList();
        for (String sourceSystemId : userRoleRequest.getSourceSystemIds()) {
            ResourceRole resourceRole = new ResourceRole();
            resourceRole.setSourceSystemId(sourceSystemId);
            boolean containsKey = userRoleScopeMap.containsKey(sourceSystemId);
            if (containsKey) {
                UserRoleScope userRoleScope = userRoleScopeMap.get(sourceSystemId);
                String rolename = userRoleScope.getRolename();
                resourceRole.setRolename(rolename);
            }

            resourceRoles.add(resourceRole);
        }
        userRoleWithScope.setResourceRoles(resourceRoles);
        return userRoleWithScope;
    }

    public List<String> findUsersOnSpecifiedRole(String module, String rolename, String sourceSystemId) {
        List<UserRoleScope> userRoleScopes = userRoleScopeDao.findByModuleAndRolenameAndSourceSystemId(module, rolename, sourceSystemId);
        return userRoleScopes.stream().map(UserRoleScope::getUsername).collect(Collectors.toList());
    }

    public boolean bindUser(BindUserRequest bindUserRequest) {
        return true;
    }

    public void addScopeOnSpecifiedRole(UpdateScopeRequest updateScopeRequest) {
        for (String sourceSystemId : updateScopeRequest.getSourceSystemIds()) {
            UserRoleScope userRoleScope = UserRoleScope.builder()
                    .username(updateScopeRequest.getUsername())
                    .module(updateScopeRequest.getModule())
                    .rolename(updateScopeRequest.getRolename())
                    .sourceSystemId(sourceSystemId)
                    .build();
            userRoleScopeDao.create(userRoleScope);
        }
    }

    public void deleteScopeOnSpecifiedRole(UpdateScopeRequest updateScopeRequest) {
        for (String sourceSystemId : updateScopeRequest.getSourceSystemIds()) {
            UserRoleScope userRoleScope = UserRoleScope.builder()
                    .username(updateScopeRequest.getUsername())
                    .module(updateScopeRequest.getModule())
                    .rolename(updateScopeRequest.getRolename())
                    .sourceSystemId(sourceSystemId)
                    .build();
            userRoleScopeDao.delete(userRoleScope);
        }
    }

}
