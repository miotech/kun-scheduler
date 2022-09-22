package com.miotech.kun.datadiscovery.controller;

import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.datadiscovery.model.vo.ConnectionInfoVO;
import com.miotech.kun.datadiscovery.service.ConnectionAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: kun
 * @description: connection controller
 * @author: zemin  huang
 * @create: 2022-09-19 15:44
 **/

@RestController
@RequestMapping("/kun/api/v1")
@Slf4j
@RequiredArgsConstructor
public class ConnectionController {

    private final ConnectionAppService connectionAppService;

    @GetMapping("/metadata/connection/{id}")
    public RequestResult<ConnectionInfoVO> getConnection(@PathVariable Long id) {
        return RequestResult.success(connectionAppService.findSecurityConnectionInfoById(id));
    }

}
