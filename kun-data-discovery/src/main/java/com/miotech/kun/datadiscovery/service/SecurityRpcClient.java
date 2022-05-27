package com.miotech.kun.datadiscovery.service;

import com.google.common.collect.Lists;
import com.miotech.kun.security.facade.rpc.*;
import com.miotech.kun.security.service.BaseSecurityService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Component
public class SecurityRpcClient extends BaseSecurityService {

    @Value("${grpc.server.security-host}")
    private String grpcSecurityHost;

    @Value("${grpc.server.port}")
    private int grpcServerPort;

    private ManagedChannel channel;
    public static SecurityGrpc.SecurityBlockingStub stub;

    @PostConstruct
    public void init() {
        channel = ManagedChannelBuilder
                .forAddress(grpcSecurityHost, grpcServerPort)
                .usePlaintext()
                .build();
        stub = SecurityGrpc.newBlockingStub(channel);
    }

    public RoleOnSpecifiedResourcesResp findRoleOnSpecifiedResources(String moduleName, List<String> sourceSystemIds) {
        RoleOnSpecifiedResourcesReq roleOnSpecifiedResourcesReq =
                RoleOnSpecifiedResourcesReq.newBuilder().addAllSourceSystemIds(sourceSystemIds).setModule(moduleName).setUsername(getCurrentUsername()).build();
        return stub.findRoleOnSpecifiedResources(roleOnSpecifiedResourcesReq);
    }

    public void addScopeOnSpecifiedRole(String moduleName, String roleName, String userName, List<String> sourceSystemIds) {
        UpdateScopeOnSpecifiedRoleReq updateScopeOnSpecifiedRoleReq = UpdateScopeOnSpecifiedRoleReq.newBuilder()
                .setModule(moduleName)
                .setUsername(userName)
                .setRolename(roleName)
                .addAllSourceSystemIds(sourceSystemIds).build();
        stub.addScopeOnSpecifiedRole(updateScopeOnSpecifiedRoleReq);
    }

    public void deleteScopeOnSpecifiedRole(String moduleName, String roleName, String userName, List<String> sourceSystemIds) {
        UpdateScopeOnSpecifiedRoleReq updateScopeOnSpecifiedRoleReq = UpdateScopeOnSpecifiedRoleReq.newBuilder()
                .setModule(moduleName)
                .setUsername(userName)
                .setRolename(roleName)
                .addAllSourceSystemIds(sourceSystemIds).build();
        stub.deleteScopeOnSpecifiedRole(updateScopeOnSpecifiedRoleReq);
    }

    public RoleOnSpecifiedModuleResp findRoleOnSpecifiedModule(String moduleName) {
        RoleOnSpecifiedModuleReq roleOnSpecifiedModuleReq = RoleOnSpecifiedModuleReq.newBuilder().setModule(moduleName).setUsername(getCurrentUsername()).build();
        return stub.findRoleOnSpecifiedModule(roleOnSpecifiedModuleReq);
    }

    public List<String> getGlossaryEditorList(String moduleName, String role, Long sourceSystemId) {
        UsersOnSpecifiedRoleReq specifiedRoleReq = UsersOnSpecifiedRoleReq.newBuilder().setModule(moduleName).setRolename(role).setSourceSystemId(sourceSystemId.toString()).build();
        UsersOnSpecifiedRoleResp usersOnSpecifiedRole = stub.findUsersOnSpecifiedRole(specifiedRoleReq);
        if (Objects.isNull(usersOnSpecifiedRole) || CollectionUtils.isEmpty(usersOnSpecifiedRole.getUsernamesList())) {
            Lists.newArrayList();
        }
        return usersOnSpecifiedRole.getUsernamesList();
    }
}
