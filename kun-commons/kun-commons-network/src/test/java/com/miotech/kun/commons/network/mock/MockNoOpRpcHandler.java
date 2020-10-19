package com.miotech.kun.commons.network.mock;

import com.miotech.kun.commons.network.client.RpcResponseCallback;
import com.miotech.kun.commons.network.client.TransportClient;
import com.miotech.kun.commons.network.server.RpcHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class MockNoOpRpcHandler extends RpcHandler {

    private Logger logger = LoggerFactory.getLogger(MockNoOpRpcHandler.class);

    @Override
    public void receive(TransportClient client, ByteBuffer message, RpcResponseCallback callback) {
        logger.info("Receive message - {}", message);
        callback.onSuccess(ByteBuffer.allocate(10));
    }
}
