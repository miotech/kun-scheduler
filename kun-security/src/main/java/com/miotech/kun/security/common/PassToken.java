package com.miotech.kun.security.common;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class PassToken extends AbstractAuthenticationToken {

    private String principal = ConfigKey.DEFAULT_PASS_TOKEN_KEY;

    public PassToken() {
        super(null);
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

    public void setPrincipal(String principal) {
        this.principal = principal;
    }
}
