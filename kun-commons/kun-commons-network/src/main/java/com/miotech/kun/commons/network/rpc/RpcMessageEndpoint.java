package com.miotech.kun.commons.network.rpc;

import org.apache.commons.lang3.NotImplementedException;

public interface RpcMessageEndpoint {
    /**
     * Process Message from other endpoint，using @RpcInContext to response to the sender endpoint
     */
    default Object receiveAndReply(RpcInContext context, Object content) {
        throw new NotImplementedException();
    }

    /**
     * Process Message from other endpoint, do not require answering
     */
    default void receive(Object content) {
        throw new NotImplementedException();
    }

    /**
     * Invoked when any exception is thrown during handling messages.
     */
    default void onError(Throwable cause) throws Throwable {
        throw cause;
    }
}
