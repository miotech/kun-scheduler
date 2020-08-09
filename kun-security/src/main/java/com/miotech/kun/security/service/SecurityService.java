package com.miotech.kun.security.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.security.common.LdapUserIdAttributesMapper;
import com.miotech.kun.security.model.bo.UserInfo;
import com.miotech.kun.security.model.entity.User;
import com.miotech.kun.security.util.Constants;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ldap.LdapProperties;
import org.springframework.http.HttpStatus;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Service;

import javax.naming.directory.SearchControls;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class SecurityService implements InitializingBean {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, UserInfo> userCache = new ConcurrentHashMap<>();

    @Autowired
    UserService userService;

    @Autowired
    LdapTemplate ldapTemplate;

    @Autowired
    LdapProperties ldapProperties;

    @Value("${security.ldap.user-group-search-base}")
    private String groupSearchBase;

    public List<String> getUserGroup(String username) {
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("objectClass", "posixGroup"))
                .and(new EqualsFilter("memberUid~", username));

        return ldapTemplate.search(groupSearchBase,
                filter.encode(),
                SearchControls.SUBTREE_SCOPE,
                (AttributesMapper<String>) attributes -> (String) attributes.get("cn").get());
    }

    public List<String> getUsersFromGroup(String group) {
        String filter = new EqualsFilter("cn", group).encode();

        return Arrays.asList(ldapTemplate.search(groupSearchBase,
                filter,
                SearchControls.SUBTREE_SCOPE,
                new LdapUserIdAttributesMapper()).get(0).split(","));
    }

    public List<String> getSameGroupUsers(String username) {
        Set<String> users = Sets.newHashSet();
        Set<String> groups = this.getUserGroup(username).stream().collect(Collectors.toSet());
        if(groups.contains(Constants.MIOTECH_USER_GROUP)) {
            users.addAll(this.getUsersFromGroup(Constants.MIOTECH_USER_GROUP));
        }
        if(groups.contains(Constants.MOODYS_USER_GROUP)) {
            users.addAll(this.getUsersFromGroup(Constants.MOODYS_USER_GROUP));
        }
        return users.stream().collect(Collectors.toList());
    }

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
        List<String> userGroups = this.getUserGroup(userInfo.getUsername());
        Set<String> permissions = this.getGroupPermission(userGroups);
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

    public Set<String> getGroupPermission(List<String> groups) {
        Set<String> permissions = Sets.newHashSet();
        Set<String> groupSet = groups.stream().collect(Collectors.toSet());
        if(groupSet.contains(Constants.MOODYS_USER_GROUP)) {
            permissions.add(Constants.PERMISSION_PDF_COA);
        }
        if(groupSet.contains(Constants.MIOTECH_USER_GROUP)) {
            permissions.add(Constants.PERMISSION_DATA_DISCOVERY);
            permissions.add(Constants.PERMISSION_DATA_DEVELOPMENT);
            permissions.add(Constants.PERMISSION_PDF_GENERAL);
        }
        return permissions;
    }

    @Override
    public void afterPropertiesSet() {
        userCache.putAll(userService.getUsers().stream().collect(Collectors.toMap(User::getName, this::convertToUserInfo)));
    }
}
