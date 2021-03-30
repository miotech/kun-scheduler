package com.miotech.kun.security.controller;

import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.common.model.vo.IdVO;
import com.miotech.kun.security.common.UserStatus;
import com.miotech.kun.security.model.UserInfo;
import com.miotech.kun.security.model.bo.UserRequest;
import com.miotech.kun.security.model.entity.User;
import com.miotech.kun.security.model.vo.UserListVO;
import com.miotech.kun.security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: Jie Chen
 * @created: 2021/1/28
 */
@RestController
@RequestMapping("/kun/api/v1/security/user")
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("/add")
    public RequestResult<User> addUser(@RequestBody UserRequest userRequest) {
        return RequestResult.success(userService.addUser(userRequest));
    }

    @PostMapping("/disable/{id}")
    public RequestResult<IdVO> disableUser(@PathVariable("id") Long id) {
        IdVO idVO = new IdVO();
        idVO.setId(userService.updateUserStatus(id, UserStatus.DISABLE));
        return RequestResult.success(idVO);
    }

    @GetMapping("/search")
    public RequestResult<UserListVO> getUsers(@RequestParam("keyword") String keyword) {
        UserListVO vo = new UserListVO();
        vo.setUsers(userService.getUsers().stream().map(User::getName).collect(Collectors.toList()));
        return RequestResult.success(vo);
    }

    @GetMapping("/list")
    public RequestResult<List<User>> getUserList() {
        return RequestResult.success(userService.getUsers());
    }

    @GetMapping("/{id}")
    public RequestResult<User> getUser(@PathVariable("id") Long id) {
        return RequestResult.success(userService.getUser(id));
    }
}
