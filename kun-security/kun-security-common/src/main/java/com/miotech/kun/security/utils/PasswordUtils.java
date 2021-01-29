package com.miotech.kun.security.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * @author: Jie Chen
 * @created: 2021/1/28
 */
public class PasswordUtils {

    public static boolean checkRawPassword(String rawPassword) {
        if (StringUtils.isBlank(rawPassword)) {
            return false;
        }
        return true;
    }
}
