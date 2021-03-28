package com.miotech.kun.security.authenticate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.common.utils.IdUtils;
import com.miotech.kun.security.SecurityContextHolder;
import com.miotech.kun.security.common.ConfigKey;
import com.miotech.kun.security.common.Constants;
import com.miotech.kun.security.model.UserInfo;
import com.miotech.kun.security.model.bo.*;
import com.miotech.kun.security.model.constant.EntityType;
import com.miotech.kun.security.model.constant.PermissionType;
import com.miotech.kun.security.model.entity.Permissions;
import com.miotech.kun.security.model.entity.Resource;
import com.miotech.kun.security.model.entity.User;
import com.miotech.kun.security.model.entity.UserGroup;
import com.miotech.kun.security.service.PermissionService;
import com.miotech.kun.security.service.ResourceService;
import com.miotech.kun.security.service.UserGroupService;
import com.miotech.kun.security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class DefaultSecurityService implements ApplicationListener<ContextRefreshedEvent> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    UserService userService;

    @Autowired
    PermissionService permissionService;

    @Autowired
    UserGroupService userGroupService;

    @Autowired
    ResourceService resourceService;

    @Value("${security.admin.username:admin}")
    private String adminUsername;

    @Value("${security.admin.password:admin}")
    private String adminPassword;

    public void initSecurity() {
        //init admin user
        UserInfo userInfo = new UserInfo();
        userInfo.setUsername(adminUsername);
        userInfo.setPassword(adminPassword);
        userInfo = getOrSave(userInfo);

        //init root resource
        Resource resource = getOrSaveResource();

        //init miotech user group
        UserGroup userGroup = getOrSaveUserGroup(resource.getId());
        userInfo.setUserGroupId(userGroup.getId());
        getOrSaveUserPermission(userInfo.getId(), userGroup.getId());
    }

    public void getOrSaveUserPermission(Long userId, Long userGroupId) {
        HasPermissionRequest permissionRequest = HasPermissionRequest.builder()
                .subjectId(userId)
                .subjectType(EntityType.USER)
                .objectId(userGroupId)
                .objectType(EntityType.USER_GROUP)
                .build();
        Permissions permissions = permissionService.find(permissionRequest);
        if (permissions.getPermissions().isEmpty()) {
            saveUserPermission(userId, userGroupId);
        }
    }

    public UserInfo saveUser(UserInfo userInfo) {
        User user = userService.addUser(userInfo);
        userInfo.setId(user.getId());
        if (IdUtils.isNotEmpty(userInfo.getUserGroupId())) {
            saveUserPermission(user.getId(), userInfo.getUserGroupId());
        }
        return convertToUserInfo(user);
    }

    public void saveUserPermission(Long userId, Long userGroupId) {
        SavePermissionRequest savePermissionRequest = new SavePermissionRequest();
        HasPermissionRequest defaultPermission = HasPermissionRequest.builder()
                .subjectId(userId)
                .subjectType(EntityType.USER)
                .objectId(userGroupId)
                .objectType(EntityType.USER_GROUP)
                .permissionType(PermissionType.ADMIN)
                .build();
        savePermissionRequest.addHasPermission(defaultPermission);
        permissionService.savePermission(savePermissionRequest);
    }

    public UserInfo getUser(String username) {
        return convertToUserInfo(userService.getUserByName(username));
    }

    public UserInfo getOrSave(UserInfo userInfo) {
        UserInfo savedUserInfo = getUser(userInfo.getUsername());
        if (savedUserInfo == null) {
            SecurityContextHolder.setUserInfo(userInfo);
            if (IdUtils.isEmpty(userInfo.getUserGroupId())) {
                userInfo.setUserGroupId(getDefaultUserGroupId());
            }
            savedUserInfo = saveUser(userInfo);
        }
        SecurityContextHolder.setUserInfo(savedUserInfo);
        return savedUserInfo;
    }

    private Long getDefaultUserGroupId() {
        return userGroupService.findByName(ConfigKey.INITIAL_USER_GROUP_NAME).getId();
    }

    public UserGroup getOrSaveUserGroup(Long resId) {
        UserGroup userGroup = userGroupService.findByName(ConfigKey.INITIAL_USER_GROUP_NAME);
        if (userGroup.getId() == null) {
            UserGroupRequest userGroupRequest = new UserGroupRequest();
            userGroupRequest.setGroupName(ConfigKey.INITIAL_USER_GROUP_NAME);
            userGroupRequest.setResourceId(resId);
            userGroupRequest.setPermissionType(PermissionType.ADMIN);
            userGroup = userGroupService.addUserGroup(userGroupRequest);
        }
        return userGroup;
    }

    public Resource getOrSaveResource() {
        Resource resource = resourceService.findByName(ConfigKey.INITIAL_RESOURCE_NAME);
        if (resource.getId() == null) {
            ResourceRequest resourceRequest = new ResourceRequest();
            resourceRequest.setResourceName(ConfigKey.INITIAL_RESOURCE_NAME);
            resourceRequest.setIsRoot(true);
            resource = resourceService.addResource(resourceRequest);
        }
        return resource;
    }

    public UserInfo convertToUserInfo(User user) {
        if (user != null && user.getId() != null) {
            UserInfo userInfo = new UserInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getName());
            userInfo.setFirstName(user.getFirstName());
            userInfo.setLastName(user.getLastName());
            userInfo.setEmail(user.getEmail());
            userInfo.setAuthOrigin(user.getAuthOrigin());
            return userInfo;
        }
        return null;
    }

    public AuthenticationSuccessHandler loginSuccessHandler() {
        return (request, response, authentication) -> {
            response.setStatus(HttpStatus.OK.value());
            response.setContentType(Constants.HTTP_CONTENT_TYPE);
            response.setCharacterEncoding(Constants.HTTP_ENCODING);
            //UserInfo userInfo = getOrSaveUser(authentication.getName());
            objectMapper.writeValue(response.getWriter(), RequestResult.success("Login Successfully.", authentication.getDetails()));
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

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) {
            initSecurity();
        }
    }
}