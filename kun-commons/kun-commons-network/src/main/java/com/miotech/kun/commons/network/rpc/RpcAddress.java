package com.miotech.kun.commons.network.rpc;

public class RpcAddress {
    private String host;

    private int port;

    public RpcAddress(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public String toString() {
       return host + ":" + port;
    }
}
