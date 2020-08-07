package com.miotech.kun.security.common;

import org.springframework.ldap.core.AttributesMapper;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;

/**
 * @author: Melo
 * @created: 8/6/20
 */
public class LdapUserIdAttributesMapper implements AttributesMapper<String> {

    @Override
    public String mapFromAttributes(Attributes attributes) throws NamingException {
        NamingEnumeration usernames = attributes.get("memberuid").getAll();
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        while(usernames.hasMore()) {
            if(first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append(usernames.next().toString());
        }
        return sb.toString();
    }
}
