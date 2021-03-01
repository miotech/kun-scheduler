package com.miotech.kun.security.saml2;

import com.unboundid.scim2.common.types.Email;
import com.unboundid.scim2.common.types.Name;
import com.unboundid.scim2.common.types.UserResource;
import org.opensaml.saml.saml2.core.Assertion;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ScimSaml2AuthenticatedPrincipal implements AuthenticatedPrincipal, Serializable {
    private static final long serialVersionUID = 530L;

    private final Saml2UserResource userResource;

    public ScimSaml2AuthenticatedPrincipal(
            final Assertion assertion,
            final Map<String, List<Object>> attributes,
            final SimpleScimMappings attributeMappings) {
        Assert.notNull(assertion, "assertion cannot be null");
        Assert.notNull(assertion.getSubject(), "assertion subject cannot be null");
        Assert.notNull(assertion.getSubject().getNameID(), "assertion subject NameID cannot be null");
        Assert.notNull(attributes, "attributes cannot be null");
        Assert.notNull(attributeMappings, "attributeMappings cannot be null");

        final Name name = new Name()
                .setFamilyName(getAttribute(attributes, attributeMappings, SimpleScimMappings::getFamilyName))
                .setGivenName(getAttribute(attributes, attributeMappings, SimpleScimMappings::getGivenName));

        final List<Email> emails = new ArrayList<>(1);
        emails.add(new Email()
                .setValue(getAttribute(attributes, attributeMappings, SimpleScimMappings::getEmail))
                .setPrimary(true));

        userResource = new Saml2UserResource();
        userResource.setUserName(assertion.getSubject().getNameID().getValue());
        userResource.setName(name);
        userResource.setEmails(emails);
    }

    private static String getAttribute(
            final Map<String, List<Object>> attributes,
            final SimpleScimMappings simpleScimMappings,
            final Function<SimpleScimMappings, String> attributeMapper) {

        final String key = attributeMapper.apply(simpleScimMappings);

        return new ArrayList<>(attributes.get(key)).stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getName() {
        return this.userResource.getUserName();
    }

    public UserResource getUserResource() {
        return this.userResource;
    }
}
