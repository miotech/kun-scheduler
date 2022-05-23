package com.miotech.kun.datadiscovery.controller;

import com.google.common.collect.ImmutableList;
import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.datadiscovery.model.entity.GlossaryChildren;
import com.miotech.kun.datadiscovery.service.GlossaryService;
import com.miotech.kun.datadiscovery.service.SecurityRpcClient;
import com.miotech.kun.security.facade.rpc.UpdateScopeOnSpecifiedRoleReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kun/api/v1")
public class TestGRpcController {

    @Autowired
    GlossaryService glossaryService;

    @Autowired
    private SecurityRpcClient securityRpcClient;

    @GetMapping("/grpc/addScope/{username}")
    public RequestResult<GlossaryChildren> addScope(@PathVariable("username") String username) {
        UpdateScopeOnSpecifiedRoleReq req = UpdateScopeOnSpecifiedRoleReq.newBuilder()
                .setUsername(username)
                .setRolename("viewer")
                .setModule("test")
                .addAllSourceSystemIds(ImmutableList.of("id_1"))
                .build();
        securityRpcClient.stub.addScopeOnSpecifiedRole(req);
        return RequestResult.success();
    }

}
