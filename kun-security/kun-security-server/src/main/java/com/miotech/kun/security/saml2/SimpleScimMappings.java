package com.miotech.kun.security.saml2;

/**
 * @author: Jie Chen
 * @created: 2021/3/10
 */
public class SimpleScimMappings {

    String givenName;

    String familyName;

    String email;

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
