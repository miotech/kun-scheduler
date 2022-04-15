package com.miotech.kun.security.service;

import com.miotech.kun.security.common.UserStatus;
import com.miotech.kun.security.dao.UserDao;
import com.miotech.kun.security.model.bo.UserExtensionInformation;
import com.miotech.kun.security.model.bo.UserRequest;
import com.miotech.kun.security.model.entity.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/7/1
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class UserService extends BaseSecurityService {

    @Autowired
    private UserSystem userSystem;

    @Autowired
    private UserDao userDao;

    @Autowired
    PasswordEncoder passwordEncoder;

    public User addUser(UserRequest userRequest) {
        userRequest.setCreateUser(getCurrentUser().getId());
        userRequest.setCreateTime(System.currentTimeMillis());
        userRequest.setUpdateUser(getCurrentUser().getId());
        userRequest.setUpdateTime(System.currentTimeMillis());
        if (StringUtils.isNotBlank(userRequest.getPassword())) {
            userRequest.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        }

        UserExtensionInformation extensionInformation = userSystem.getExtensionInformation(userRequest.getUsername());
        if (extensionInformation != null) {
            userRequest.setEmail(extensionInformation.getEmail());
            userRequest.setWeComId(extensionInformation.getWeComId());
        }

        return userDao.create(userRequest);
    }

    public void updateUserStatus(Long id, UserStatus userStatus) {
        userDao.updateStatus(id, userStatus);
    }

    public User getUser(Long id) {
        return userDao.findById(id);
    }

    public User getUserByName(String name) {
        return userDao.findByUsername(name);
    }

    public List<User> getUsers() {
        return userDao.findAll();
    }
}
