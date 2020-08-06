package com.miotech.kun.security.controller;

import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.security.model.bo.UserInfo;
import com.miotech.kun.security.model.entity.User;
import com.miotech.kun.security.model.vo.UserListVO;
import com.miotech.kun.security.service.SecurityService;
import com.miotech.kun.security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
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
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    SecurityService securityService;

    @GetMapping("/user/whoami")
    public RequestResult<UserInfo> whoami() {
        UserInfo userInfo = (UserInfo) SecurityContextHolder.getContext().getAuthentication().getDetails();
        return RequestResult.success(securityService.enrichUserInfo(userInfo));
    }

    @GetMapping("/user/search")
    public RequestResult<UserListVO> getUsers(@RequestParam("keyword") String keyword) {
        UserListVO vo = new UserListVO();
        vo.setUsers(userService.getUsers().stream().map(User::getName).collect(Collectors.toList()));
        return RequestResult.success(vo);
    }

    @GetMapping("/user/list")
    public RequestResult<List<UserInfo>> getUserList() {
        List<UserInfo> userInfos = userService.getUsers().stream()
                .map(securityService::convertToUserInfo)
                .map(securityService::enrichUserInfo)
                .collect(Collectors.toList());
        return RequestResult.success(userInfos);
    }
}
