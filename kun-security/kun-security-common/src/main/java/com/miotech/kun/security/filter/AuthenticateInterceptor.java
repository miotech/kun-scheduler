package com.miotech.kun.security.filter;

import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.security.SecurityContextHolder;
import com.miotech.kun.security.common.ConfigKey;
import com.miotech.kun.security.model.bo.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.StringJoiner;

/**
 * @author: Jie Chen
 * @created: 2020/9/22
 */
@Slf4j
public class AuthenticateInterceptor extends HandlerInterceptorAdapter {

    /**
     * separate from app rest template
     */
    RestTemplate restTemplate = new RestTemplate();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            if (StringUtils.isNotEmpty(request.getRequestURI())
                    && (request.getRequestURI().startsWith("/v3/api-docs")
                    || request.getRequestURI().startsWith("/swagger-ui"))) {
                return true;
            }
            return doAuthenticate(request);
        } catch (Exception e) {
            log.error("Failed to authenticate.", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }

    private boolean doAuthenticate(HttpServletRequest request) {
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
            return false;
        }
        UserInfo userInfo = authResult.getBody().getResult();
        SecurityContextHolder.setUserInfo(userInfo);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        SecurityContextHolder.removeUserInfo();
    }
}
