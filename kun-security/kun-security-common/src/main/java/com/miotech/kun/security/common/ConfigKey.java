package com.miotech.kun.security.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author: Jie Chen
 * @created: 2020/8/6
 */
@Component
public class ConfigKey {

    public static final String REQUEST_PASS_TOKEN_KEY = "pass-token";

    public static final String DEFAULT_PASS_TOKEN_KEY = "kun-pass-token";

    public static String SECURITY_SERVER_BASE_URL;

    @Value("${security.base-url:http://kun-security:8084/kun/api/v1}")
    public void setSecurityServerBaseUrl(String baseUrl) {
        SECURITY_SERVER_BASE_URL = baseUrl;
    }

    public static String getSecurityServerAuthenticateUrl() {
        return SECURITY_SERVER_BASE_URL + "/security/whoami";
    }

    public static String getSecurityServerUserInfoUrl() {
        return SECURITY_SERVER_BASE_URL + "/security/user/";
    }
}
