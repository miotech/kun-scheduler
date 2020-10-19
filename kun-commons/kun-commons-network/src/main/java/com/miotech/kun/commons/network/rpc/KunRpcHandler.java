package com.miotech.kun.commons.network.rpc;

import com.miotech.kun.commons.network.client.RpcResponseCallback;
import com.miotech.kun.commons.network.client.TransportClient;
import com.miotech.kun.commons.network.server.RpcHandler;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

public class KunRpcHandler extends RpcHandler {

    private final Dispatcher dispatcher;
    private final ConcurrentHashMap<RpcAddress, RpcAddress> remoteAddresses = new ConcurrentHashMap<>();

    public KunRpcHandler(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public void receive(TransportClient client,
                        ByteBuffer message,
                        RpcResponseCallback callback) {
        RequestMessage requestMessage = internalReceive(client, message);
        dispatcher.postMessage(requestMessage, callback);
    }

    @Override
    public void receive(TransportClient client,
                        ByteBuffer message) {
        RequestMessage requestMessage = internalReceive(client, message);
        dispatcher.postMessage(requestMessage);
    }

    private RequestMessage internalReceive(TransportClient client, ByteBuffer message) {
        InetSocketAddress addr = (InetSocketAddress) client.getChannel().remoteAddress();
        assert(addr != null);
        RpcAddress clientRpcAddress = new RpcAddress(addr.getHostString(), addr.getPort());
        RequestMessage requestMessage = RequestMessage.fromByteBuffer(message, client);
        if (requestMessage.getSenderAddress() == null) {
            // Create a new message with the socket address of the client as the sender.
            requestMessage = new RequestMessage(clientRpcAddress, requestMessage.getContent(), requestMessage.getReceiver());
        }
        RpcAddress  remoteEnvAddress = requestMessage.getSenderAddress();
        if (remoteAddresses.putIfAbsent(clientRpcAddress, remoteEnvAddress) == null) {
            dispatcher.postToAll(new RemoteEndpointConnected(remoteEnvAddress));
        }
        return requestMessage;
    }

    /**
     * Tell every endpoint "I'm online"
     */
    @Override
    public void channelActive(TransportClient client) {
        InetSocketAddress addr = (InetSocketAddress) client.getChannel().remoteAddress();
        assert(addr != null);
        RpcAddress clientAddr = new RpcAddress(addr.getHostString(), addr.getPort());
        dispatcher.postToAll(new RemoteEndpointConnected(clientAddr));
    }

}
