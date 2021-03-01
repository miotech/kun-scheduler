package com.miotech.kun.security.authenticate.filter;

import com.miotech.kun.security.authenticate.DefaultSecurityService;
import com.miotech.kun.security.model.UserInfo;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.*;
import java.io.IOException;

/**
 * @author: Jie Chen
 * @created: 2021/3/3
 */
public class SyncUserInfoFilter implements Filter {

    private DefaultSecurityService defaultSecurityService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()) {
            syncUserInfo(authentication);
        }

        chain.doFilter(request, response);
    }

    private void syncUserInfo(Authentication authentication) {
        UserInfo userInfo = (UserInfo) authentication.getDetails();
        if (userInfo == null) {
            return;
        }
        UserInfo savedUser = defaultSecurityService.getOrSave(userInfo);
        com.miotech.kun.security.SecurityContextHolder.setUserInfo(savedUser);
    }


    public void setDefaultSecurityService(DefaultSecurityService defaultSecurityService) {
        this.defaultSecurityService = defaultSecurityService;
    }
}
