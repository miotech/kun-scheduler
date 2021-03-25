package com.miotech.kun.security.authenticate.filter;

import com.miotech.kun.security.common.ConfigKey;
import com.miotech.kun.security.common.PassToken;
import com.miotech.kun.security.model.UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author: Jie Chen
 * @created: 2021/3/3
 */
public class PassTokenFilter implements Filter {

    String passToken;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
            if (isEqualToPassToken((HttpServletRequest) req, passToken)) {
                Authentication authentication = new PassToken();
                SecurityContextHolder.getContext().setAuthentication(authentication);
                setUserInfoInContextHolder();
            }

            chain.doFilter(req, res);

    }

    private void setUserInfoInContextHolder() {
        UserInfo userInfo = getPassTokenUserInfo();
        com.miotech.kun.security.SecurityContextHolder.setUserInfo(userInfo);
    }

    private boolean isEqualToPassToken(HttpServletRequest httpServletRequest,
                                       String passToken) {
        String headerToken = httpServletRequest.getHeader(ConfigKey.HTTP_REQUEST_PASS_TOKEN_KEY);
        return StringUtils.equals(headerToken, passToken);
    }

    private UserInfo getPassTokenUserInfo() {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(ConfigKey.DEFAULT_INTERNAL_PASS_TOKEN_ID);
        userInfo.setUsername(ConfigKey.DEFAULT_INTERNAL_PASS_TOKEN_KEY);
        return userInfo;
    }

    public void setPassToken(String passToken) {
        this.passToken = passToken;
    }
}
