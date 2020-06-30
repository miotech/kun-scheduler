package com.miotech.kun.security.common;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class PassToken extends AbstractAuthenticationToken {

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
        return "pass-token";
    }
}
