package com.miotech.kun.security.saml2;

import com.miotech.kun.security.model.AuthenticationOriginInfo;
import com.miotech.kun.security.model.UserInfo;
import org.opensaml.saml.saml2.core.Assertion;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.saml2.provider.service.authentication.OpenSamlAuthenticationProvider.ResponseToken;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ResponseToAuthenticationConverter implements
        Converter<ResponseToken, Saml2Authentication> {

    private final Saml2AuthorityAttributeLookup saml2AuthorityAttributeLookup;

    public ResponseToAuthenticationConverter(final Saml2AuthorityAttributeLookup saml2AuthorityAttributeLookup) {
        this.saml2AuthorityAttributeLookup = saml2AuthorityAttributeLookup;
    }

    @Override
    public Saml2Authentication convert(ResponseToken responseToken) {
        final Assertion assertion = responseToken.getResponse().getAssertions().get(0);
        final Map<String, List<Object>> attributes = XmlUtils.getAssertionAttributes(assertion);
        final String registrationId = responseToken.getToken().getRelyingPartyRegistration().getRegistrationId();
        final ScimSaml2AuthenticatedPrincipal principal = new ScimSaml2AuthenticatedPrincipal(
                assertion,
                attributes,
                saml2AuthorityAttributeLookup.getIdentityMappings(registrationId));
        final Collection<? extends GrantedAuthority> assertionAuthorities =
                getAssertionAuthorities(attributes,
                        saml2AuthorityAttributeLookup.getAuthorityAttribute(registrationId));
        Saml2Authentication saml2Authentication = new Saml2Authentication(
                principal,
                responseToken.getToken().getSaml2Response(),
                assertionAuthorities);
        UserInfo userInfo = new UserInfo();
        AuthenticationOriginInfo authOriginInfo = new AuthenticationOriginInfo();
        userInfo.setUsername(principal.getName());
        userInfo.setFirstName((String) attributes.get("FirstName").stream().findFirst().orElse(null));
        userInfo.setLastName((String) attributes.get("LastName").stream().findFirst().orElse(null));
        userInfo.setEmail((String) attributes.get("Email").stream().findFirst().orElse(null));
        authOriginInfo.setGroups(attributes.get("Groups").stream().map(group -> (String) group).collect(Collectors.toList()));
        authOriginInfo.setAuthType(registrationId);
        userInfo.setAuthOriginInfo(authOriginInfo);
        saml2Authentication.setDetails(userInfo);
        return saml2Authentication;
    }

    private static Collection<? extends GrantedAuthority> getAssertionAuthorities(
            final Map<String, List<Object>> attributes,
            final String authoritiesAttributeName) {
        if (attributes == null) {
            return Collections.emptySet();
        }

        final List<Object> groups = attributes.get(authoritiesAttributeName);
        if (groups == null) {
            return Collections.EMPTY_LIST;
        }
        return groups.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(String::toLowerCase)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }
}
