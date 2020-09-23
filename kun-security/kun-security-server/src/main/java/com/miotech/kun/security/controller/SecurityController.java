package com.miotech.kun.security.controller;

import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.security.SecurityContextHolder;
import com.miotech.kun.security.model.bo.UserInfo;
import com.miotech.kun.security.model.entity.User;
import com.miotech.kun.security.model.vo.UserListVO;
import com.miotech.kun.security.service.AbstractSecurityService;
import com.miotech.kun.security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: Jie Chen
 * @created: 2020/7/1
 */
@RestController
@RequestMapping("/kun/api/v1")
public class SecurityController {

    @Autowired
    UserService userService;

    @Autowired
    AbstractSecurityService abstractSecurityService;

    @GetMapping("/security/whoami")
    public RequestResult<UserInfo> whoami() {
        UserInfo userInfo = SecurityContextHolder.getUserInfo();
        return RequestResult.success(abstractSecurityService.enrichUserInfo(userInfo));
    }

    @GetMapping("/security/user/search")
    public RequestResult<UserListVO> getUsers(@RequestParam("keyword") String keyword) {
        UserListVO vo = new UserListVO();
        vo.setUsers(userService.getUsers().stream().map(User::getName).collect(Collectors.toList()));
        return RequestResult.success(vo);
    }

    @GetMapping("/security/user/list")
    public RequestResult<List<UserInfo>> getUserList() {
        List<UserInfo> userInfos = userService.getUsers().stream()
                .map(abstractSecurityService::convertToUserInfo)
                .map(abstractSecurityService::enrichUserInfo)
                .collect(Collectors.toList());
        return RequestResult.success(userInfos);
    }
}
