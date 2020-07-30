package com.miotech.kun.security.testing;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class TestSecurityToken extends AbstractAuthenticationToken {

    private final Object principal;

    public TestSecurityToken(Object principal) {
        super(null);
        this.principal = principal;
        this.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

}
