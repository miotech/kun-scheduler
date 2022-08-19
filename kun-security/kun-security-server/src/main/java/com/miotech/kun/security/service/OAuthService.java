package com.miotech.kun.security.service;

import com.miotech.kun.security.model.bo.OAuthCallbackInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.stereotype.Service;

@Service
public class OAuthService extends BaseSecurityService {

    @Value("${security.auth.authorize-url}")
    private String authorizeUrl;

    @Value("${security.auth.response-type}")
    private String responseType;

    @Autowired
    private OAuth2ClientProperties oAuth2ClientProperties;

    public OAuthCallbackInformation getCallbackInformation(String registrationId) {
        String clientId = oAuth2ClientProperties.getRegistration().get(registrationId).getClientId();
        String redirectUri = oAuth2ClientProperties.getRegistration().get(registrationId).getRedirectUri();

        return OAuthCallbackInformation.builder()
                .url(authorizeUrl)
                .clientId(clientId)
                .redirectUri(redirectUri)
                .responseType(responseType)
                .build();
    }

}