package com.miotech.kun.security.authenticate.resolver.impl;

import com.miotech.kun.security.authenticate.resolver.AttributesResolver;

import java.util.Map;

/**
 * @author: Jie Chen
 * @created: 2021/3/3
 */
public class OktaAttributesResolver implements AttributesResolver {

    @Override
    public String getUsernameKey() {
        return "username";
    }

    @Override
    public String getFullNameKey() {
        return "fullname";
    }

    @Override
    public String resolveUsername(Map userInfoMap) {
        String usernameKey = getUsernameKey();
        return String.valueOf(userInfoMap.get(usernameKey));
    }

    @Override
    public String resolveFullName(Map userInfoMap) {
        String fullNameKey = getFullNameKey();
        return String.valueOf(userInfoMap.get(fullNameKey));
    }

}
