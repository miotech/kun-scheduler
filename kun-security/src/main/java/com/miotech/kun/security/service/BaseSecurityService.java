package com.miotech.kun.security.service;

import com.miotech.kun.security.model.bo.UserInfo;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author: Jie Chen
 * @created: 2020/7/1
 */
public class BaseSecurityService {

    public String getCurrentUsername() {
        UserInfo userInfo = getCurrentUser();
        if (userInfo != null) {
            return userInfo.getUsername();
        }
        return "anonymousUser";
    }

    public UserInfo getCurrentUser() {
        UserInfo userInfo = null;
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            userInfo = (UserInfo) SecurityContextHolder.getContext().getAuthentication().getDetails();
        }
        return userInfo;
    }
}
