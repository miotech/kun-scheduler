package com.miotech.kun.security.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miotech.kun.security.model.bo.UserInfo;
import com.miotech.kun.security.service.SecurityService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

    private SecurityService securityService;

    private String passToken;

    private String pdfCoaPassToken;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
                && StringUtils.isNotEmpty(authentication.getName())
                && !StringUtils.equals(authentication.getName(), "anonymousUser")) {
            if (!StringUtils.equals(ConfigKey.DEFAULT_PASS_TOKEN_KEY, authentication.getName())) {
                UserInfo savedUser = securityService.getOrSave(authentication.getName());
                UsernamePasswordAuthenticationToken newAuthentication = new UsernamePasswordAuthenticationToken(authentication.getPrincipal(),
                        authentication.getCredentials(),
                        authentication.getAuthorities());
                newAuthentication.setDetails(savedUser);
                SecurityContextHolder.getContext().setAuthentication(newAuthentication);
            }
        } else if (isEqualToPassToken((HttpServletRequest) req, passToken)) {
            PassToken passToken = new PassToken();
            UserInfo userInfo = new UserInfo();
            userInfo.setUsername(ConfigKey.DEFAULT_PASS_TOKEN_KEY);
            passToken.setDetails(userInfo);
            SecurityContextHolder.getContext().setAuthentication(passToken);
        } else if (isEqualToPassToken((HttpServletRequest) req, pdfCoaPassToken)) {
            PassToken passToken = new PassToken();
            UserInfo userInfo = new UserInfo();
            userInfo.setUsername(ConfigKey.PDF_COA_PASS_TOKEN_KEY);
            passToken.setDetails(userInfo);
            SecurityContextHolder.getContext().setAuthentication(passToken);
        }

        super.doFilter(req, res, chain);
    }

    private boolean isEqualToPassToken(HttpServletRequest httpServletRequest,
                                       String passToken) {
        String parameterToken = httpServletRequest.getParameter(ConfigKey.REQUEST_PASS_TOKEN_KEY);
        String headerToken = httpServletRequest.getHeader(ConfigKey.REQUEST_PASS_TOKEN_KEY);
        return StringUtils.equals(headerToken, passToken)
                || StringUtils.equals(parameterToken, passToken);
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

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public void setPassToken(String passToken) {
        this.passToken = passToken;
    }

    public void setPdfCoaPassToken(String passToken) {
        this.pdfCoaPassToken = passToken;
    }
}
