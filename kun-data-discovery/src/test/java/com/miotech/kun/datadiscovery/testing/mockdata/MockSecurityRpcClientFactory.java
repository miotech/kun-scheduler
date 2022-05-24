package com.miotech.kun.datadiscovery.testing.mockdata;

import com.miotech.kun.datadiscovery.model.enums.GlossaryRole;
import com.miotech.kun.datadiscovery.model.enums.SecurityModule;
import com.miotech.kun.security.SecurityContextHolder;
import com.miotech.kun.security.facade.rpc.RoleOnSpecifiedModuleResp;
import com.miotech.kun.security.facade.rpc.RoleOnSpecifiedResourcesResp;
import com.miotech.kun.security.facade.rpc.ScopeRole;
import com.miotech.kun.security.model.UserInfo;

import java.util.List;

/**
 * @program: kun
 * @description: SecurityRpcClient mock data
 * @author: zemin  huang
 * @create: 2022-05-24 09:49
 **/
public class MockSecurityRpcClientFactory {

    public static RoleOnSpecifiedModuleResp mockRoleOnSpecifiedModule(String userName, GlossaryRole role) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUsername(userName);
        SecurityContextHolder.setUserInfo(userInfo);
        String securityModule = SecurityModule.GLOSSARY.name();
        String roleName = role.name();

        return RoleOnSpecifiedModuleResp.newBuilder()
                .setModule(securityModule)
                .setRolename(roleName)
                .setUsername(userName)
                .build();
    }

    public static RoleOnSpecifiedResourcesResp mockUserRoleRespGlossary(String userName, List<ScopeRole> scopeRoles) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUsername(userName);
        SecurityContextHolder.setUserInfo(userInfo);
        String securityModule = SecurityModule.GLOSSARY.name();
        return RoleOnSpecifiedResourcesResp.newBuilder()
                .setModule(securityModule)
                .setUsername(userName)
                .addAllScopeRoles(scopeRoles)
                .build();
    }


}
