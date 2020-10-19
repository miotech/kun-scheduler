package com.miotech.kun.commons.network.rpc;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class RpcMessageEndpointClient {
    private final RpcEndpointAddress rpcEndpointAddress;

    public RpcMessageEndpointClient(RpcEndpointAddress rpcEndpointAddress) {
        this.rpcEndpointAddress = rpcEndpointAddress;
    }

    public RpcEndpointAddress getRpcEndpointAddress() {
        return rpcEndpointAddress;
    }

    public void send(Object content) {
        throw new NotImplementedException();
    }
}
