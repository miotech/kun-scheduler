package com.miotech.kun.security.testing;

import com.miotech.kun.security.common.Permission;
import com.miotech.kun.security.model.UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class WithMockTestUserSecurityContextFactory
        implements WithSecurityContextFactory<WithMockTestUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockTestUser userInfo) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        Set<Permission> perms = Arrays.stream(userInfo.permissions())
                .filter(StringUtils::isNoneBlank)
                .map(Permission::valueOf)
                .collect(Collectors.toSet());

        UserInfo principal = new UserInfo();
        principal.setId(userInfo.id());
        principal.setUsername(userInfo.name());
        principal.setPassword(userInfo.password());
        principal.setPermissions(perms);

        TestSecurityToken auth =
                new TestSecurityToken(userInfo.name());
        context.setAuthentication(auth);
        com.miotech.kun.security.SecurityContextHolder.setUserInfo(principal);
        return context;
    }
}
