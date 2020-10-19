package com.miotech.kun.commons.network.mock;

import com.miotech.kun.commons.network.client.RpcResponseCallback;
import com.miotech.kun.commons.network.client.TransportClient;
import com.miotech.kun.commons.network.server.RpcHandler;

import java.nio.ByteBuffer;

public class MockFailRpcHandler extends RpcHandler {

    @Override
    public void receive(TransportClient client, ByteBuffer message, RpcResponseCallback callback) {
        throw new UnsupportedOperationException("Cannot handle messages");
    }
}
