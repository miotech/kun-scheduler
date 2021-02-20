package com.miotech.kun.security.authenticate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miotech.kun.security.common.ConfigKey;
import com.miotech.kun.security.common.PassToken;
import com.miotech.kun.security.model.UserInfo;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DefaultAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private static final Log LOG = LogFactory.getLog(DefaultAuthenticationFilter.class);

    private static final String ERROR_MESSAGE = "Failed to authenticate.";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private DefaultSecurityService defaultSecurityService;

    private String passToken;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Authentication newAuthentication = authentication;
        if (authentication != null
                && authentication.isAuthenticated()
                && StringUtils.isNotEmpty(authentication.getName())
                && !StringUtils.equals(authentication.getName(), "anonymousUser")) {
            if (authentication.getClass().isAssignableFrom(UsernamePasswordAuthenticationToken.class)
            || authentication.getClass().isAssignableFrom(OAuth2AuthenticationToken.class)) {
                String saveUsername = authentication.getName();
                UserInfo savedUser = defaultSecurityService.getOrSave(saveUsername);
                newAuthentication = new UsernamePasswordAuthenticationToken(authentication.getPrincipal(),
                        authentication.getCredentials(),
                        authentication.getAuthorities());
                com.miotech.kun.security.SecurityContextHolder.setUserInfo(savedUser);
            } else if (authentication.getClass().isAssignableFrom(PassToken.class)) {
                UserInfo userInfo = getPassTokenUserInfo();
                com.miotech.kun.security.SecurityContextHolder.setUserInfo(userInfo);
            }
        } else if (isEqualToPassToken((HttpServletRequest) req, passToken)) {
            PassToken passToken = new PassToken();
            UserInfo userInfo = getPassTokenUserInfo();
            newAuthentication = passToken;
            com.miotech.kun.security.SecurityContextHolder.setUserInfo(userInfo);
        }

        SecurityContextHolder.getContext().setAuthentication(newAuthentication);
        super.doFilter(req, res, chain);
    }

    private UserInfo getPassTokenUserInfo() {
        UserInfo userInfo = new UserInfo();
        userInfo.setUsername(ConfigKey.DEFAULT_INTERNAL_PASS_TOKEN_KEY);
        return userInfo;
    }

    private boolean isEqualToPassToken(HttpServletRequest httpServletRequest,
                                       String passToken) {
        String headerToken = httpServletRequest.getHeader(ConfigKey.HTTP_REQUEST_PASS_TOKEN_KEY);
        return StringUtils.equals(headerToken, passToken);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String requestBody;
        try {
            requestBody = IOUtils.toString(request.getReader());
            UserInfo authRequest = objectMapper.readValue(requestBody, UserInfo.class);

            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword());

            // Allow subclasses to set the "details" property
            setDetails(request, token);

            return this.getAuthenticationManager().authenticate(token);
        } catch (IOException e) {
            LOG.error(ERROR_MESSAGE, e);
            throw new InternalAuthenticationServiceException(ERROR_MESSAGE, e);
        }
    }

    public void setDefaultSecurityService(DefaultSecurityService defaultSecurityService) {
        this.defaultSecurityService = defaultSecurityService;
    }

    public void setPassToken(String passToken) {
        this.passToken = passToken;
    }
}