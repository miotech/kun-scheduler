package com.miotech.kun.security.service;

import com.google.protobuf.Empty;
import com.miotech.kun.security.annotation.GrpcServer;
import com.miotech.kun.security.facade.rpc.*;
import com.miotech.kun.security.model.bo.*;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.Collectors;

@GrpcServer
public class SecurityService extends SecurityGrpc.SecurityImplBase {

    @Autowired
    private UserRoleScopeService userRoleScopeService;

    @Override
    public void findRoleOnSpecifiedModule(RoleOnSpecifiedModuleReq request, StreamObserver<RoleOnSpecifiedModuleResp> responseObserver) {
        UserRoleOnModuleResp userRoleOnModuleResp = userRoleScopeService.findRoleOnSpecifiedModule(UserRoleOnModuleReq.convertFrom(request));
        RoleOnSpecifiedModuleResp userRoleResp = RoleOnSpecifiedModuleResp.newBuilder()
                .setUsername(userRoleOnModuleResp.getUsername())
                .setModule(userRoleOnModuleResp.getModule())
                .setRolename(userRoleOnModuleResp.getRolename())
                .build();
        responseObserver.onNext(userRoleResp);
        responseObserver.onCompleted();
    }

    @Override
    public void findRoleOnSpecifiedResources(RoleOnSpecifiedResourcesReq request, StreamObserver<RoleOnSpecifiedResourcesResp> responseObserver) {
        UserRoleWithScope userRoleWithScope = userRoleScopeService.findRoleOnSpecifiedResources(UserRoleRequest.convertFrom(request));
        RoleOnSpecifiedResourcesResp userRoleResp = RoleOnSpecifiedResourcesResp.newBuilder()
                .setUsername(userRoleWithScope.getUsername())
                .setModule(userRoleWithScope.getModule())
                .addAllScopeRoles(userRoleWithScope.getResourceRoles().stream()
                        .map(rr -> ScopeRole.newBuilder()
                                .setRolename(rr.getRolename())
                                .setSourceSystemId(rr.getSourceSystemId())
                                .build()).collect(Collectors.toList()))
                .build();
        responseObserver.onNext(userRoleResp);
        responseObserver.onCompleted();
    }

    @Override
    public void addScopeOnSpecifiedRole(UpdateScopeOnSpecifiedRoleReq request, StreamObserver<Empty> responseObserver) {
        userRoleScopeService.addScopeOnSpecifiedRole(UpdateScopeRequest.convertFrom(request));
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void deleteScopeOnSpecifiedRole(UpdateScopeOnSpecifiedRoleReq request, StreamObserver<Empty> responseObserver) {
        userRoleScopeService.deleteScopeOnSpecifiedRole(UpdateScopeRequest.convertFrom(request));
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }
}
