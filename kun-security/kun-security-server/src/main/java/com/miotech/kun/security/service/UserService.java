package com.miotech.kun.security.service;

import com.miotech.kun.security.model.UserInfo;
import com.miotech.kun.security.model.entity.User;
import com.miotech.kun.security.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    public User addUser(UserInfo userInfo) {
        userInfo.setCreateUser(getCurrentUsername());
        userInfo.setCreateTime(System.currentTimeMillis());
        userInfo.setUpdateUser(getCurrentUsername());
        userInfo.setUpdateTime(System.currentTimeMillis());
        return userRepository.addUser(userInfo);
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
