package com.miotech.kun.security.common;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;

public class PassToken extends AbstractAuthenticationToken {

    private String principal = ConfigKey.DEFAULT_INTERNAL_PASS_TOKEN_KEY;

    public PassToken() {
        super(Collections.singletonList(() -> "ADMIN"));
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
