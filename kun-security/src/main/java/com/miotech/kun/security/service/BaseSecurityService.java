package com.miotech.kun.security.service;

import com.miotech.kun.security.model.bo.UserInfo;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author: Jie Chen
 * @created: 2020/7/1
 */
public class BaseSecurityService {

    public String getCurrentUser() {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            UserInfo userInfo = (UserInfo) SecurityContextHolder.getContext().getAuthentication().getDetails();
            return userInfo.getUsername();
        }
        return "anonymousUser";
    }
}
