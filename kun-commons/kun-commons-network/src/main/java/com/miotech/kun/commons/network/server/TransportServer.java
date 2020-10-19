package com.miotech.kun.commons.network.server;

import com.miotech.kun.commons.network.TransportContext;
import com.miotech.kun.commons.network.utils.IOMode;
import com.miotech.kun.commons.network.utils.NettyUtils;
import com.miotech.kun.commons.network.utils.TransportConf;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class TransportServer implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(TransportServer.class);

    private final TransportContext context;
    private final TransportConf conf;
    private final PooledByteBufAllocator pooledAllocator;
    private final RpcHandler appRpcHandler;
    private ServerBootstrap bootstrap;
    private ChannelFuture channelFuture;
    private int port = -1;

    public TransportServer(TransportContext context,
                           String hostToBind,
                           int portToBind,
                           RpcHandler appRpcHandler) {
        this.context = context;
        this.appRpcHandler = appRpcHandler;
        this.conf = context.getConf();
        if (conf.sharedByteBufAllocators()) {
            this.pooledAllocator = NettyUtils.getSharedPooledByteBufAllocator(
                    conf.preferDirectBufsForSharedByteBufAllocators(), true);
        } else {
            this.pooledAllocator = NettyUtils.createPooledByteBufAllocator(
                    conf.preferDirectBufs(), true, conf.serverThreads());
        }

        this.port = portToBind;

        initServer(hostToBind, portToBind);
    }

    private void initServer(String hostToBind, int portToBind) {
        IOMode ioMode = IOMode.valueOf(conf.ioMode());
        EventLoopGroup bossGroup = NettyUtils.createEventLoop(ioMode, 1,
                conf.getModuleName() + "-boss");
        EventLoopGroup workerGroup =  NettyUtils.createEventLoop(ioMode, conf.serverThreads(),
                conf.getModuleName() + "-server");

        bootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NettyUtils.getServerChannelClass(ioMode))
                .option(ChannelOption.ALLOCATOR, pooledAllocator)
                .option(ChannelOption.SO_REUSEADDR, !SystemUtils.IS_OS_WINDOWS)
                .childOption(ChannelOption.ALLOCATOR, pooledAllocator);

        if (conf.backLog() > 0) {
            bootstrap.option(ChannelOption.SO_BACKLOG, conf.backLog());
        }

        if (conf.receiveBuf() > 0) {
            bootstrap.childOption(ChannelOption.SO_RCVBUF, conf.receiveBuf());
        }

        if (conf.sendBuf() > 0) {
            bootstrap.childOption(ChannelOption.SO_SNDBUF, conf.sendBuf());
        }

        if (conf.enableTcpKeepAlive()) {
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        }

        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                logger.debug("New connection accepted for remote address {}.", ch.remoteAddress());
                RpcHandler rpcHandler = appRpcHandler;
                context.initializePipeline(ch, rpcHandler);
            }
        });

        InetSocketAddress address = hostToBind == null ?
                new InetSocketAddress(portToBind): new InetSocketAddress(hostToBind, portToBind);
        channelFuture = bootstrap.bind(address);
        channelFuture.syncUninterruptibly();

        port = ((InetSocketAddress) channelFuture.channel().localAddress()).getPort();
        logger.debug("Server started on port: {}", port);
    }

    public int getPort() {
        return port;
    }

    @Override
    public void close() {
        if (channelFuture != null) {
            channelFuture.channel().close().awaitUninterruptibly(10, TimeUnit.SECONDS);
            channelFuture = null;
        }
        if (bootstrap != null && bootstrap.config().group() != null) {
            bootstrap.config().group().shutdownGracefully();
        }
        if (bootstrap != null && bootstrap.config().childGroup() != null) {
            bootstrap.config().childGroup().shutdownGracefully();
        }
        bootstrap = null;
        logger.debug("Server closed");
    }
}
