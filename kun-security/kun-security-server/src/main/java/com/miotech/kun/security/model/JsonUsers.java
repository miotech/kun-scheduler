package com.miotech.kun.security.model;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/8/24
 */
@Data
public class JsonUsers {

    List<UserInfo> users;

    public boolean authUser(String username, String password) {
        for (UserInfo user : users) {
            if (StringUtils.equals(user.getUsername(), username)
                    && StringUtils.equals(user.getPassword(), password)) {
                return true;
            }
        }
        return false;
    }
}