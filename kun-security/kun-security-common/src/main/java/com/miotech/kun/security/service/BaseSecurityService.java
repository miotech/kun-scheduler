package com.miotech.kun.security.service;

import com.miotech.kun.security.SecurityContextHolder;
import com.miotech.kun.security.model.bo.UserInfo;

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
        return SecurityContextHolder.getUserInfo();
    }
}
