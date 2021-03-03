package com.miotech.kun.security.filter;

import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.security.SecurityContextHolder;
import com.miotech.kun.security.common.ConfigKey;
import com.miotech.kun.security.model.NoBodyResponse;
import com.miotech.kun.security.model.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.StringJoiner;

/**
 * @author: Jie Chen
 * @created: 2020/11/27
 */
@Slf4j
public class AuthenticateFilter implements Filter {

    /**
     * separate from app rest template
     */
    RestTemplate restTemplate = new RestTemplate();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            try {
                if (StringUtils.isNotEmpty(httpServletRequest.getRequestURI())
                        && !httpServletRequest.getRequestURI().startsWith("/actuator")
                        && !httpServletRequest.getRequestURI().startsWith("/v3/api-docs")
                        && !httpServletRequest.getRequestURI().startsWith("/swagger-ui")) {
                    doAuthenticate(httpServletRequest);
                }
            } catch (Exception e) {
                log.error("Failed to authenticate.", e);
                if (response instanceof HttpServletResponse) {
                    HttpServletResponse httpServletResponse = (HttpServletResponse) response;
                    httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response = new NoBodyResponse(httpServletResponse);
                }

            }
        }
        chain.doFilter(request, response);
    }


    private void doAuthenticate(HttpServletRequest request) {
        StringJoiner cookieStrBuilder = new StringJoiner(";");
        if (request.getCookies() != null && request.getCookies().length != 0) {
            for (Cookie cookie : request.getCookies()) {
                String cookieFullStr = cookie.getName() + "=" + cookie.getValue();
                cookieStrBuilder.add(cookieFullStr);
            }
        }
        String passToken = request.getHeader(ConfigKey.HTTP_REQUEST_PASS_TOKEN_KEY);
        if (StringUtils.isEmpty(passToken)) {
            passToken = request.getParameter(ConfigKey.HTTP_REQUEST_PASS_TOKEN_KEY);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, cookieStrBuilder.toString());
        headers.add(ConfigKey.HTTP_REQUEST_PASS_TOKEN_KEY, passToken);
        HttpEntity entity = new HttpEntity(headers);
        String authUrl = ConfigKey.getSecurityServerAuthenticateUrl();
        ResponseEntity<RequestResult<UserInfo>> authResult = restTemplate.exchange(authUrl,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<RequestResult<UserInfo>>() {
                });
        if (!authResult.getStatusCode().is2xxSuccessful()) {
            throw new BadCredentialsException("Response status code " + authResult.getStatusCode());
        }
        UserInfo userInfo = authResult.getBody().getResult();
        SecurityContextHolder.setUserInfo(userInfo);
    }

}
