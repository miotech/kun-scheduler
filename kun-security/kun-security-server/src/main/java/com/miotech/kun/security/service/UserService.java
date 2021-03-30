package com.miotech.kun.security.service;

import com.miotech.kun.security.common.UserStatus;
import com.miotech.kun.security.model.UserInfo;
import com.miotech.kun.security.model.bo.UserRequest;
import com.miotech.kun.security.model.entity.User;
import com.miotech.kun.security.persistence.UserRepository;
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
    UserRepository userRepository;

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
        return userRepository.addUser(userRequest);
    }

    public Long updateUserStatus(Long id, UserStatus userStatus) {
        return userRepository.updateUserStatus(id, userStatus);
    }

    public User getUser(Long id) {
        return userRepository.find(id);
    }

    public User getUserByName(String name) {
        return userRepository.findByName(name);
    }

    public List<User> getUsers() {
        return userRepository.findAllUser();
    }
}
