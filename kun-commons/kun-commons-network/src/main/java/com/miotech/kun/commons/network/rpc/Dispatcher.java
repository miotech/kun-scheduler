package com.miotech.kun.commons.network.rpc;

import com.miotech.kun.commons.network.client.RpcResponseCallback;
import com.miotech.kun.commons.network.serialize.Serialization;
import com.miotech.kun.commons.utils.EventConsumer;
import com.miotech.kun.commons.utils.EventLoop;
import com.miotech.kun.commons.utils.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * A wrapper for EventLoop to isolate io and message process
 */
public class Dispatcher {
    private final static Logger logger = LoggerFactory.getLogger(Dispatcher.class);

    private final AtomicBoolean stopped;
    private final ConcurrentHashMap<String, RpcMessageEndpoint> endpoints =
            new ConcurrentHashMap<>();

    private final RpcMessageLoop delegate;
    private final Serialization serialization;

    public Dispatcher(String name, Serialization serialization) {
        delegate = new RpcMessageLoop(name);
        this.serialization = serialization;
        this.stopped = new AtomicBoolean(false);
    }

    public void start() {
        // register all message consumer
        List<EventConsumer<String, InMessage>> consumers = endpoints.values().stream()
                .map(RpcMessageConsumer::new)
                .collect(Collectors.toList());
        delegate.addConsumers(consumers);
        delegate.start();
    }

    public void stop() {
        stopped.compareAndSet(false, true);
        this.delegate.stop();
    }

    public void registerEndpoint(String name, RpcMessageEndpoint endpoint) {
        if (stopped.get()) {
            throw new IllegalStateException("Rpc has been stopped");
        }
        if (endpoints.containsKey(name)) {
            throw new IllegalArgumentException("There is already an RpcMessageConsumer called " + name);
        }
        endpoints.put(name, endpoint);
    }

    public void postMessage(RequestMessage message) {
        postMessage(message, null);
    }

    public void postMessage(RequestMessage message, RpcResponseCallback callback) {
        RpcMessage rpcMessage;
        if (callback == null) {
            rpcMessage = new RpcMessage(message.getSenderAddress(), message.getContent());
        } else {
            RpcInContext rpcInContext = new RpcInContext(serialization, callback);
            rpcMessage = new RpcMessage(message.getSenderAddress(), message.getContent(), rpcInContext);
        }
        postMessage(message.getReceiver().getRpcEndpointAddress().getName(), rpcMessage);
    }

    public void postToAll(InMessage message) {
        endpoints.keySet()
                .forEach(n -> delegate.post(n, message));
    }

    public void postMessage(String name, InMessage message) {
        delegate.post(name, message);
    }

    private class RpcMessageLoop extends EventLoop<String, InMessage> {
        public RpcMessageLoop(String name) {
            super(name);
        }
    }

    private class RpcMessageConsumer extends EventConsumer<String, InMessage> {

        final RpcMessageEndpoint endpoint;

        public RpcMessageConsumer(RpcMessageEndpoint endpoint) {
            this.endpoint = endpoint;
        }

        @Override
        public void onReceive(InMessage event) {
            if (event instanceof RpcMessage) {
                RpcMessage message = (RpcMessage) event;
                try {
                    if (message.getRpcInContext() != null) {
                        endpoint.receiveAndReply(message.getRpcInContext(), message.getContent());
                    } else {
                        endpoint.receive(message.getContent());
                    }
                } catch (Throwable e) {
                    try {
                        endpoint.onError(e);
                    } catch (Throwable throwable) {
                        throw ExceptionUtils.wrapIfChecked(throwable);
                    }
                }
            }

        }
    }
}