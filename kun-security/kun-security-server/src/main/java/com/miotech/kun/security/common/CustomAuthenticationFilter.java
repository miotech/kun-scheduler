package com.miotech.kun.security.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miotech.kun.security.model.bo.UserInfo;
import com.miotech.kun.security.service.AbstractSecurityService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private static final Log LOG = LogFactory.getLog(CustomAuthenticationFilter.class);

    private static final String ERROR_MESSAGE = "Something went wrong while parsing /login request body";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private AbstractSecurityService abstractSecurityService;

    private String passToken;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Authentication newAuthentication = authentication;
        if (authentication != null
                && StringUtils.isNotEmpty(authentication.getName())
                && !StringUtils.equals(authentication.getName(), "anonymousUser")) {
            if (authentication.getClass().isAssignableFrom(UsernamePasswordAuthenticationToken.class)) {
                String saveUsername = authentication.getName().toLowerCase();
                UserInfo savedUser = abstractSecurityService.getOrSave(saveUsername);
                newAuthentication = new UsernamePasswordAuthenticationToken(authentication.getPrincipal(),
                        authentication.getCredentials(),
                        authentication.getAuthorities());

                com.miotech.kun.security.SecurityContextHolder.setUserInfo(savedUser);
            }
        } else if (isEqualToPassToken((HttpServletRequest) req, passToken)) {
            PassToken passToken = new PassToken();
            UserInfo userInfo = new UserInfo();
            userInfo.setUsername(ConfigKey.DEFAULT_PASS_TOKEN_KEY);
            newAuthentication = passToken;
            com.miotech.kun.security.SecurityContextHolder.setUserInfo(userInfo);
        }

        SecurityContextHolder.getContext().setAuthentication(newAuthentication);
        super.doFilter(req, res, chain);
    }

    private boolean isEqualToPassToken(HttpServletRequest httpServletRequest,
                                       String passToken) {
        String headerToken = httpServletRequest.getHeader(ConfigKey.REQUEST_PASS_TOKEN_KEY);
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

    public void setAbstractSecurityService(AbstractSecurityService abstractSecurityService) {
        this.abstractSecurityService = abstractSecurityService;
    }

    public void setPassToken(String passToken) {
        this.passToken = passToken;
    }
}