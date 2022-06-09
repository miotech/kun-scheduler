package com.miotech.kun.security.service;

import com.google.protobuf.Empty;
import com.miotech.kun.security.annotation.GrpcServer;
import com.miotech.kun.security.facade.rpc.*;
import com.miotech.kun.security.model.bo.*;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@GrpcServer
public class SecurityService extends SecurityGrpc.SecurityImplBase {

    @Autowired
    private UserRoleScopeService userRoleScopeService;

    @Override
    public void findUsersOnSpecifiedRole(UsersOnSpecifiedRoleReq request, StreamObserver<UsersOnSpecifiedRoleResp> responseObserver) {
        List<String> usernames = userRoleScopeService.findUsersOnSpecifiedRole(request.getModule(), request.getRolename(), request.getSourceSystemId());
        UsersOnSpecifiedRoleResp roleResp = UsersOnSpecifiedRoleResp.newBuilder()
                .addAllUsernames(usernames)
                .build();
        responseObserver.onNext(roleResp);
        responseObserver.onCompleted();
    }

    @Override
    public void findRoleOnSpecifiedModule(RoleOnSpecifiedModuleReq request, StreamObserver<RoleOnSpecifiedModuleResp> responseObserver) {
        UserRoleOnModuleResp userRoleOnModuleResp = userRoleScopeService.findRoleOnSpecifiedModule(UserRoleOnModuleReq.convertFrom(request));

        RoleOnSpecifiedModuleResp.Builder builder = RoleOnSpecifiedModuleResp.newBuilder()
                .setUsername(userRoleOnModuleResp.getUsername())
                .setModule(userRoleOnModuleResp.getModule());

        RoleOnSpecifiedModuleResp userRoleResp = builder.build();
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
                        .map(rr -> {
                            ScopeRole.Builder builder = ScopeRole.newBuilder().setSourceSystemId(rr.getSourceSystemId());
                            if (rr.getRolename() != null) {
                                builder.setRolename(rr.getRolename());
                            }

                            return builder.build();
                        }).collect(Collectors.toList()))
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
