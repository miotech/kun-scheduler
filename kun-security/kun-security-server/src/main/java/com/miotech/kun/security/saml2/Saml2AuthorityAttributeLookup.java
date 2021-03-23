package com.miotech.kun.security.saml2;

public interface Saml2AuthorityAttributeLookup {
    String getAuthorityAttribute(String registrationId);

    SimpleScimMappings getIdentityMappings(String registrationId);
}
