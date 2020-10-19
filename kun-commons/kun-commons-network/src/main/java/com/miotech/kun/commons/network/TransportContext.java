package com.miotech.kun.commons.network;

import com.codahale.metrics.Counter;
import com.miotech.kun.commons.network.client.TransportClient;
import com.miotech.kun.commons.network.client.TransportClientFactory;
import com.miotech.kun.commons.network.client.TransportResponseHandler;
import com.miotech.kun.commons.network.protocol.MessageDecoder;
import com.miotech.kun.commons.network.protocol.MessageEncoder;
import com.miotech.kun.commons.network.server.RpcHandler;
import com.miotech.kun.commons.network.server.TransportChannelHandler;
import com.miotech.kun.commons.network.server.TransportRequestHandler;
import com.miotech.kun.commons.network.server.TransportServer;
import com.miotech.kun.commons.network.utils.NettyUtils;
import com.miotech.kun.commons.network.utils.TransportConf;
import com.miotech.kun.commons.network.utils.TransportFrameDecoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

public class TransportContext {
    private final TransportConf conf;
    private final boolean closeIdleConnections;
    private final RpcHandler rpcHandler;
    private final Counter registeredConnections = new Counter();

    public TransportContext(TransportConf conf,
                            RpcHandler rpcHandler) {
        this(conf, rpcHandler, false);
    }

    public TransportContext(TransportConf conf,
                            RpcHandler rpcHandler,
                            boolean closeIdleConnections
                            ) {
        this.conf = conf;
        this.rpcHandler = rpcHandler;
        this.closeIdleConnections = closeIdleConnections;
    }

    public TransportConf getConf() {
        return conf;
    }

    public TransportClientFactory createClientFactory() {
        return new TransportClientFactory(this);
    }

    public TransportServer createServer() {
        return createServer(0);
    }

    public TransportServer createServer(int port) {
        return createServer(null, port);
    }

    public TransportServer createServer(
            String host, int port) {
        return new TransportServer(this, host, port, rpcHandler);
    }

    public Counter getRegisteredConnections() {
        return registeredConnections;
    }

    public TransportChannelHandler initializePipeline(SocketChannel channel) {
        return initializePipeline(channel, rpcHandler);
    }

    /**
     * Initialize the Client or Server side Netty Channel Pipeline
     * @param channel
     * @param channelRpcHandler
     * @return
     */
    public TransportChannelHandler initializePipeline(SocketChannel channel, RpcHandler channelRpcHandler) {
        TransportChannelHandler channelHandler = createChannelHandler(channel, channelRpcHandler);
        ChannelPipeline pipeline = channel.pipeline()
                .addLast("encoder", MessageEncoder.INSTANCE)
                .addLast(TransportFrameDecoder.HANDLER_NAME, NettyUtils.createFrameDecoder())
                .addLast("decoder", MessageDecoder.INSTANCE)
                // heatbeat
                .addLast("idleStateHandler",
                        new IdleStateHandler(0, 0, conf.connectionTimeoutMs() / 1000))
                .addLast("userHandler", channelHandler);
        return channelHandler;
    }

    /**
     * Creates the server- and client-side handler which is used to handle both RequestMessages and
     * ResponseMessages. The channel is expected to have been successfully created, though certain
     * properties (such as the remoteAddress()) may not be available yet.
     */
    private TransportChannelHandler createChannelHandler(Channel channel, RpcHandler rpcHandler) {
        TransportResponseHandler responseHandler = new TransportResponseHandler(channel);
        TransportClient client = new TransportClient(channel, responseHandler);
        TransportRequestHandler requestHandler = new TransportRequestHandler(channel, client, rpcHandler);
        return new TransportChannelHandler(client, responseHandler, requestHandler,
                conf.connectionTimeoutMs(), closeIdleConnections, this);
    }

}
