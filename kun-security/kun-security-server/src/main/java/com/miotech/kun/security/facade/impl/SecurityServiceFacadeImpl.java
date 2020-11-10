package com.miotech.kun.security.facade.impl;

import com.miotech.kun.security.facade.SecurityServiceFacade;
import com.miotech.kun.security.model.bo.UserInfo;
import com.miotech.kun.security.model.entity.User;
import com.miotech.kun.security.service.AbstractSecurityService;
import com.miotech.kun.security.service.UserService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: Jie Chen
 * @created: 2020/11/10
 */
@DubboService(version = "1.0.0")
@Service
public class SecurityServiceFacadeImpl implements SecurityServiceFacade {

    @Autowired
    UserService userService;

    @Autowired
    AbstractSecurityService abstractSecurityService;

    @Override
    public UserInfo getUserById(Long id) {
        User user = userService.getUser(id);
        return abstractSecurityService.convertToUserInfo(user);
    }
}
