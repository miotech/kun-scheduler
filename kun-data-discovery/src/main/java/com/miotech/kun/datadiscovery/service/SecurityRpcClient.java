package com.miotech.kun.datadiscovery.service;

import com.miotech.kun.security.facade.rpc.SecurityGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class SecurityRpcClient {

    @Value("${grpc.server.security-host}")
    private String grpcSecurityHost;

    @Value("${grpc.server.port}")
    private int grpcServerPort;

    private ManagedChannel channel;
    public static SecurityGrpc.SecurityBlockingStub stub;

    @PostConstruct
    public void init(){
        channel = ManagedChannelBuilder
                .forAddress(grpcSecurityHost, grpcServerPort)
                .usePlaintext()
                .build();
        stub = SecurityGrpc.newBlockingStub(channel);
    }

}
