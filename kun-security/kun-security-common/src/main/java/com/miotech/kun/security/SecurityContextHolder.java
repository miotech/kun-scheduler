package com.miotech.kun.security;

import com.miotech.kun.security.model.bo.UserInfo;

/**
 * @author: Jie Chen
 * @created: 2020/9/22
 */
public class SecurityContextHolder {

    private static final ThreadLocal<UserInfo> USER_INFO_CONTEXT = new ThreadLocal<>();

    public static void setUserInfo(UserInfo userInfo) {
        USER_INFO_CONTEXT.set(userInfo);
    }

    public static UserInfo getUserInfo() {
        return USER_INFO_CONTEXT.get();
    }

    public static void removeUserInfo() {
        USER_INFO_CONTEXT.remove();
    }
}
