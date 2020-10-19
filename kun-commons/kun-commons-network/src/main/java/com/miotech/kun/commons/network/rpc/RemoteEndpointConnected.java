package com.miotech.kun.commons.network.rpc;

public class RemoteEndpointConnected extends InMessage {
    private final RpcAddress remoteEnvAddress;

    public RemoteEndpointConnected(RpcAddress remoteEnvAddress) {
        this.remoteEnvAddress = remoteEnvAddress;
    }
}
