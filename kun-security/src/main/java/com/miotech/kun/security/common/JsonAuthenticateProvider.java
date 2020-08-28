package com.miotech.kun.security.common;

import com.google.common.io.Files;
import com.miotech.kun.common.utils.JSONUtils;
import com.miotech.kun.security.model.bo.JsonUsers;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.nio.charset.Charset;

/**
 * @author: Jie Chen
 * @created: 2020/8/24
 */
public class JsonAuthenticateProvider implements AuthenticationProvider {

    private static final Log LOG = LogFactory.getLog(JsonAuthenticateProvider.class);

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            File jsonFile = ResourceUtils.getFile("classpath:kun-users.json");
            String usersJson = Files.asCharSource(jsonFile, Charset.defaultCharset()).read();
            JsonUsers jsonUsers = JSONUtils.toJavaObject(usersJson, JsonUsers.class);
            boolean isAuth = jsonUsers.authUser((String) authentication.getPrincipal(), (String) authentication.getCredentials());
            if (!isAuth) {
                authentication.setAuthenticated(false);
                throw new BadCredentialsException("Error username or password.");
            }
        } catch (Exception e) {
            LOG.error("Authenticate failed.", e);
            authentication.setAuthenticated(false);
            throw new InternalAuthenticationServiceException(e.getMessage());
        }
        return authentication;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

}