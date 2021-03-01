package com.miotech.kun.security.saml2;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@ConfigurationProperties("spring.security.saml2.relyingparty")
public class Saml2RelyingPartyMappings implements Saml2AuthorityAttributeLookup {

    private final Map<String, String> authorityMapping = new LinkedHashMap<>();

    private final Map<String, SimpleScimMappings> identityMapping = new LinkedHashMap<>();

    @SuppressWarnings("unused")
    Map<String, String> getAuthorityMapping() {
        return authorityMapping;
    }

    @SuppressWarnings("unused")
    Map<String, SimpleScimMappings> getIdentityMapping() {
        return identityMapping;
    }

    @Override
    public String getAuthorityAttribute(final String registrationId) {
        return this.authorityMapping.get(registrationId);
    }

    @Override
    public SimpleScimMappings getIdentityMappings(final String registrationId) {
        return this.identityMapping.get(registrationId);
    }
}
