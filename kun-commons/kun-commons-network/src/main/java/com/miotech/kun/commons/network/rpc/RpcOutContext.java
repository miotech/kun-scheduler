package com.miotech.kun.commons.network.rpc;

import com.miotech.kun.commons.utils.EventConsumer;
import com.miotech.kun.commons.utils.EventLoop;

public class RpcOutContext {

    private final RpcOutMessageLoop messageLoop;

    public RpcOutContext(RpcOutMessageLoop messageLoop) {
        this.messageLoop = new RpcOutMessageLoop("outbox");
    }

    public void send(RequestMessage requestMessage) {

    }

    public void ask(RequestMessage requestMessage) {

    }


    private class RpcOutMessageLoop extends EventLoop<RpcMessageEndpointClient, OutMessage> {
        public RpcOutMessageLoop(String name) {
            super(name);
        }
    }


    private class RpcOutMessageConsumer extends EventConsumer<RpcMessageEndpointClient, OutMessage> {

        @Override
        public void onReceive(OutMessage event) {

        }
    }

}
