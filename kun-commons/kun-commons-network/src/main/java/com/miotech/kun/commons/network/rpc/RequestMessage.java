package com.miotech.kun.commons.network.rpc;

import com.miotech.kun.commons.network.client.TransportClient;

import java.nio.ByteBuffer;

class RequestMessage {
    private final RpcAddress senderAddress;
    private final Object content;
    private final RpcMessageEndpointClient receiver;

    public RequestMessage(RpcAddress senderAddress, Object content, RpcMessageEndpointClient receiver) {
        this.senderAddress = senderAddress;
        this.content = content;
        this.receiver = receiver;
    }

    public RpcAddress getSenderAddress() {
        return senderAddress;
    }

    public Object getContent() {
        return content;
    }

    public RpcMessageEndpointClient getReceiver() {
        return receiver;
    }

    /** parse RequestMessage from ByteBuffer payload **/
    public static RequestMessage fromByteBuffer(
            ByteBuffer bytes,
            TransportClient client
    ) {
        return null;
    }
}
