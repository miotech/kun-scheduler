package com.miotech.kun.security.authenticate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.security.SecurityContextHolder;
import com.miotech.kun.security.common.Constants;
import com.miotech.kun.security.model.UserInfo;
import com.miotech.kun.security.model.bo.HasPermission;
import com.miotech.kun.security.model.bo.PermissionRequest;
import com.miotech.kun.security.model.constant.PermissionType;
import com.miotech.kun.security.model.entity.User;
import com.miotech.kun.security.service.PermissionService;
import com.miotech.kun.security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Service;

@Service
public class DefaultSecurityService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    UserService userService;

    @Autowired
    PermissionService permissionService;

    public UserInfo saveUser(UserInfo userInfo) {
        User user = userService.addUser(userInfo);
        PermissionRequest permissionRequest = new PermissionRequest();
        HasPermission defaultPermission = HasPermission.builder()
                .userId(user.getId())
                .resourceId(0L)
                .permissionType(PermissionType.READ)
                .build();
        permissionRequest.addHasPermission(defaultPermission);
        permissionService.savePermission(permissionRequest);
        return convertToUserInfo(user);
    }

    public UserInfo getOrSave(String username) {
        UserInfo userInfo = getUserFromDB(username);
        if (userInfo == null) {
            userInfo = new UserInfo();
            userInfo.setUsername(username);
            SecurityContextHolder.setUserInfo(userInfo);
            userInfo = saveUser(userInfo);
        }
        return userInfo;
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
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
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

}