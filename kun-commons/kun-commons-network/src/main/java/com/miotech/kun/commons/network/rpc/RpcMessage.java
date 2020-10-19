package com.miotech.kun.commons.network.rpc;

class RpcMessage extends InMessage {
    private final RpcAddress rpcAddress;

    private final Object content;

    private final RpcInContext rpcInContext;

    public RpcMessage(RpcAddress rpcAddress, Object content) {
        this(rpcAddress, content, null);
    }

    public RpcMessage(RpcAddress rpcAddress, Object content, RpcInContext rpcInContext) {
        this.rpcAddress = rpcAddress;
        this.content = content;
        this.rpcInContext = rpcInContext;
    }

    public RpcAddress getRpcAddress() {
        return rpcAddress;
    }

    public Object getContent() {
        return content;
    }

    public RpcInContext getRpcInContext() {
        return rpcInContext;
    }
}
