package com.miotech.kun.security.authenticate.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miotech.kun.security.model.UserInfo;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author: Jie Chen
 * @created: 2021/3/3
 */
public class UsernamePasswordAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final Log LOG = LogFactory.getLog(UsernamePasswordAuthenticationFilter.class);

    private static final String ERROR_MESSAGE = "Failed to authenticate.";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public UsernamePasswordAuthenticationFilter(String defaultFilterProcessesUrl) {
        super(defaultFilterProcessesUrl);
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

            AbstractAuthenticationToken authentication = (AbstractAuthenticationToken) this.getAuthenticationManager().authenticate(token);
            UserInfo userInfo = new UserInfo();
            userInfo.setUsername(authentication.getName());
            authentication.setDetails(userInfo);
            return authentication;
        } catch (IOException e) {
            LOG.error(ERROR_MESSAGE, e);
            throw new InternalAuthenticationServiceException(ERROR_MESSAGE, e);
        }
    }

    protected void setDetails(HttpServletRequest request,
                              UsernamePasswordAuthenticationToken authRequest) {
        authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
    }

}