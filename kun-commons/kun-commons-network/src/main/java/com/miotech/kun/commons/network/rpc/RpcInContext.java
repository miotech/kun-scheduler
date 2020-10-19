package com.miotech.kun.commons.network.rpc;

import com.miotech.kun.commons.network.client.RpcResponseCallback;
import com.miotech.kun.commons.network.serialize.Serialization;

import java.nio.ByteBuffer;

public class RpcInContext {

    private final RpcResponseCallback callback;
    private final Serialization serialization;

    public RpcInContext(Serialization serialization,
                        RpcResponseCallback callback) {
        this.callback = callback;
        this.serialization = serialization;
    }

    public void send(Object content) {
        ByteBuffer byteBuffer = serialization.serialize(content);
        callback.onSuccess(byteBuffer);
    }

    public void reply(Object content) {
        send(content);
    }
}
