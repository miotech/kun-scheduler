package com.miotech.kun.security.service;

import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.commons.utils.StringUtils;
import com.miotech.kun.security.model.UserInfo;
import com.miotech.kun.security.model.entity.User;
import com.miotech.kun.security.persistence.UserRepository;
import com.miotech.kun.security.utils.PasswordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/7/1
 */
@Service
public class UserService extends BaseSecurityService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    public User addUser(UserInfo userInfo) {
        userInfo.setCreateUser(getCurrentUsername());
        userInfo.setCreateTime(System.currentTimeMillis());
        userInfo.setUpdateUser(getCurrentUsername());
        userInfo.setUpdateTime(System.currentTimeMillis());
        return userRepository.addUser(userInfo);
    }

    public Long removeUser(Long id) {
        return userRepository.removeUser(id);
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
