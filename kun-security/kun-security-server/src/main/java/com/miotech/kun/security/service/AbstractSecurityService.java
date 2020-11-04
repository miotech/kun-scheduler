package com.miotech.kun.security.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.security.model.bo.UserInfo;
import com.miotech.kun.security.model.entity.User;
import com.miotech.kun.security.util.Constants;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AbstractSecurityService implements InitializingBean {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, UserInfo> userCache = new ConcurrentHashMap<>();

    @Autowired
    UserService userService;

    public UserInfo saveUser(UserInfo userInfo) {
        User user = userService.addUser(userInfo);
        return saveUserToCache(convertToUserInfo(user));
    }

    public UserInfo saveUserToCache(UserInfo userInfo) {
        userCache.put(userInfo.getUsername(), userInfo);
        return userCache.get(userInfo.getUsername());
    }

    public UserInfo getOrSave(String username) {
        UserInfo userInfo = getUserFromCache(username);
        if (userInfo == null) {
            userInfo = getUserFromDB(username);
            if (userInfo == null) {
                userInfo = new UserInfo();
                userInfo.setUsername(username);
                userInfo = saveUser(userInfo);
            } else {
                saveUserToCache(userInfo);
            }
        }
        return userInfo;
    }

    public UserInfo getUserFromCache(String username) {
        return userCache.get(username);
    }

    public UserInfo getUserFromDB(String username) {
        return convertToUserInfo(userService.getUserByName(username));
    }

    public UserInfo convertToUserInfo(User user) {
        if (user != null && user.getId() != null) {
            UserInfo userInfo = new UserInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getName());
            return userInfo;
        }
        return null;
    }

    public UserInfo enrichUserInfo(UserInfo userInfo) {
        Set<String> permissions = Sets.newHashSet();
        permissions.add(Constants.PERMISSION_DATA_DISCOVERY);
        permissions.add(Constants.PERMISSION_DATA_DEVELOPMENT);
        userInfo.setPermissions(permissions);
        return userInfo;
    }

    public AuthenticationSuccessHandler loginSuccessHandler() {
        return (request, response, authentication) -> {
            response.setStatus(HttpStatus.OK.value());
            response.setContentType(Constants.HTTP_CONTENT_TYPE);
            response.setCharacterEncoding(Constants.HTTP_ENCODING);
            UserInfo userInfo = getOrSave(authentication.getName());
            objectMapper.writeValue(response.getWriter(), RequestResult.success("Login Successfully.", userInfo));
        };
    }

    public AuthenticationFailureHandler loginFailureHandler() {
        return (request, response, exception) -> {
            response.setStatus(HttpStatus.OK.value());
            response.setContentType(Constants.HTTP_CONTENT_TYPE);
            response.setCharacterEncoding(Constants.HTTP_ENCODING);
            objectMapper.writeValue(response.getWriter(), RequestResult.error("Login Failed."));
        };
    }

    public LogoutSuccessHandler logoutSuccessHandler() {
        return (request, response, authentication) -> {
            response.setStatus(HttpStatus.OK.value());
            response.setContentType(Constants.HTTP_CONTENT_TYPE);
            response.setCharacterEncoding(Constants.HTTP_ENCODING);
            objectMapper.writeValue(response.getWriter(), RequestResult.success("Logout Successfully."));
        };
    }

    @Override
    public void afterPropertiesSet() {
        userCache.putAll(userService.getUsers().stream().collect(Collectors.toMap(User::getName, this::convertToUserInfo)));
    }
}