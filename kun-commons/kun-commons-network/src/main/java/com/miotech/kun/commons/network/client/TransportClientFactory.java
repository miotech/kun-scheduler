package com.miotech.kun.commons.network.client;

import com.google.common.annotations.VisibleForTesting;
import com.miotech.kun.commons.network.TransportContext;
import com.miotech.kun.commons.network.server.TransportChannelHandler;
import com.miotech.kun.commons.network.utils.IOMode;
import com.miotech.kun.commons.network.utils.JavaUtils;
import com.miotech.kun.commons.network.utils.NettyUtils;
import com.miotech.kun.commons.network.utils.TransportConf;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class TransportClientFactory implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(TransportClientFactory.class);

    private final TransportContext ctx;
    private final TransportConf conf;
    private final ConcurrentHashMap<SocketAddress, ClientPool> connectionPool;
    private final int numConnectionsPerPeer;
    private final Random rand;
    private Class<? extends Channel> socketChannelClass;
    private EventLoopGroup workerGroup;
    private PooledByteBufAllocator pooledAllocator;
    private int fastFailTimeWindow;

    public TransportClientFactory(TransportContext ctx) {
        this.ctx = ctx;
        this.conf = ctx.getConf();
        this.connectionPool = new ConcurrentHashMap<>();
        this.numConnectionsPerPeer = conf.numConnectionsPerPeer();
        this.rand = new Random();
        initFactory();
    }

    private void initFactory() {
        IOMode ioMode = IOMode.valueOf(conf.ioMode());
        this.socketChannelClass = NettyUtils.getClientChannelClass(ioMode);
        this.workerGroup = NettyUtils.createEventLoop(
                ioMode,
                conf.clientThreads(),
                conf.getModuleName() + "-client");
        if (conf.sharedByteBufAllocators()) {
            this.pooledAllocator = NettyUtils.getSharedPooledByteBufAllocator(
                    conf.preferDirectBufsForSharedByteBufAllocators(), false /* allowCache */);
        } else {
            this.pooledAllocator = NettyUtils.createPooledByteBufAllocator(
                    conf.preferDirectBufs(), false /* allowCache */, conf.clientThreads());
        }
        fastFailTimeWindow = (int)(conf.ioRetryWaitTimeMs() * 0.95);
    }


    /**
     * Create a {@link TransportClient} connecting to the given remote host / port.
     *
     * We maintain an array of clients (size determined by spark.shuffle.io.numConnectionsPerPeer)
     * and randomly picks one to use. If no client was previously created in the randomly selected
     * spot, this function creates a new client and places it there.
     *
     * If the fastFail parameter is true, fail immediately when the last attempt to the same address
     * failed within the fast fail time window (95 percent of the io wait retry timeout). The
     * assumption is the caller will handle retrying.
     *
     * This blocks until a connection is successfully established and fully bootstrapped.
     *
     * Concurrency: This method is safe to call from multiple threads.
     *
     * @param remoteHost remote address host
     * @param remotePort remote address port
     * @param fastFail whether this call should fail immediately when the last attempt to the same
     *                 address failed with in the last fast fail time window.
     */
    public TransportClient createClient(String remoteHost, int remotePort, boolean fastFail)
            throws IOException, InterruptedException {
        // Get connection from the connection pool first.
        // If it is not found or not active, create a new one.
        // Use unresolved address here to avoid DNS resolution each time we creates a client.
        final InetSocketAddress unresolvedAddress =
                InetSocketAddress.createUnresolved(remoteHost, remotePort);

        // Create the ClientPool if we don't have it yet.
        ClientPool clientPool = connectionPool.get(unresolvedAddress);
        if (clientPool == null) {
            connectionPool.putIfAbsent(unresolvedAddress, new ClientPool(numConnectionsPerPeer));
            clientPool = connectionPool.get(unresolvedAddress);
        }

        int clientIndex = rand.nextInt(numConnectionsPerPeer);
        TransportClient cachedClient = clientPool.clients[clientIndex];

        if (cachedClient != null && cachedClient.isActive()) {
            // Make sure that the channel will not timeout by updating the last use time of the
            // handler. Then check that the client is still alive, in case it timed out before
            // this code was able to update things.
            TransportChannelHandler handler = cachedClient.getChannel().pipeline()
                    .get(TransportChannelHandler.class);
            synchronized (handler) {
                handler.getResponseHandler().updateTimeOfLastRequest();
            }

            if (cachedClient.isActive()) {
                logger.trace("Returning cached connection to {}: {}",
                        cachedClient.getSocketAddress(), cachedClient);
                return cachedClient;
            }
        }

        // If we reach here, we don't have an existing connection open. Let's create a new one.
        // Multiple threads might race here to create new connections. Keep only one of them active.
        final long preResolveHost = System.nanoTime();
        final InetSocketAddress resolvedAddress = new InetSocketAddress(remoteHost, remotePort);
        final long hostResolveTimeMs = (System.nanoTime() - preResolveHost) / 1000000;
        final String resolvMsg = resolvedAddress.isUnresolved() ? "failed" : "succeed";
        if (hostResolveTimeMs > 2000) {
            logger.warn("DNS resolution {} for {} took {} ms",
                    resolvMsg, resolvedAddress, hostResolveTimeMs);
        } else {
            logger.trace("DNS resolution {} for {} took {} ms",
                    resolvMsg, resolvedAddress, hostResolveTimeMs);
        }

        synchronized (clientPool.locks[clientIndex]) {
            cachedClient = clientPool.clients[clientIndex];

            if (cachedClient != null) {
                if (cachedClient.isActive()) {
                    logger.trace("Returning cached connection to {}: {}", resolvedAddress, cachedClient);
                    return cachedClient;
                } else {
                    logger.info("Found inactive connection to {}, creating a new one.", resolvedAddress);
                }
            }
            // If this connection should fast fail when last connection failed in last fast fail time
            // window and it did, fail this connection directly.
            if (fastFail && System.currentTimeMillis() - clientPool.lastConnectionFailed <
                    fastFailTimeWindow) {
                throw new IOException(
                        String.format("Connecting to %s failed in the last %s ms, fail this connection directly",
                                resolvedAddress, fastFailTimeWindow));
            }
            try {
                clientPool.clients[clientIndex] = createClient(resolvedAddress);
                clientPool.lastConnectionFailed = 0;
            } catch (IOException e) {
                clientPool.lastConnectionFailed = System.currentTimeMillis();
                throw e;
            }
            return clientPool.clients[clientIndex];
        }
    }

    public TransportClient createClient(String remoteHost, int remotePort)
            throws IOException, InterruptedException {
        return createClient(remoteHost, remotePort, false);
    }

    /**
     * Create a completely new {@link TransportClient} to the given remote host / port.
     * This connection is not pooled.
     *
     * As with {@link #createClient(String, int)}, this method is blocking.
     */
    public TransportClient createUnmanagedClient(String remoteHost, int remotePort)
            throws IOException, InterruptedException {
        final InetSocketAddress address = new InetSocketAddress(remoteHost, remotePort);
        return createClient(address);
    }

    /** Create a completely new {@link TransportClient} to the remote address. */
    @VisibleForTesting
    TransportClient createClient(InetSocketAddress address)
            throws IOException, InterruptedException {
        logger.debug("Creating new connection to {}", address);

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(socketChannelClass)
                // Disable Nagle's Algorithm since we don't want packets to wait
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, conf.connectionTimeoutMs())
                .option(ChannelOption.ALLOCATOR, pooledAllocator);

        if (conf.receiveBuf() > 0) {
            bootstrap.option(ChannelOption.SO_RCVBUF, conf.receiveBuf());
        }

        if (conf.sendBuf() > 0) {
            bootstrap.option(ChannelOption.SO_SNDBUF, conf.sendBuf());
        }

        final AtomicReference<TransportClient> clientRef = new AtomicReference<>();
        final AtomicReference<Channel> channelRef = new AtomicReference<>();

        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) {
                TransportChannelHandler clientHandler = ctx.initializePipeline(ch);
                clientRef.set(clientHandler.getClient());
                channelRef.set(ch);
            }
        });

        // Connect to the remote server
        long preConnect = System.nanoTime();
        ChannelFuture cf = bootstrap.connect(address);
        if (!cf.await(conf.connectionTimeoutMs())) {
            throw new IOException(
                    String.format("Connecting to %s timed out (%s ms)", address, conf.connectionTimeoutMs()));
        } else if (cf.cause() != null) {
            throw new IOException(String.format("Failed to connect to %s", address), cf.cause());
        }

        TransportClient client = clientRef.get();
        Channel channel = channelRef.get();
        assert client != null : "Channel future completed successfully with null client";

        return client;
    }

    @Override
    public void close() {
        // Go through all clients and close them if they are active.
        for (ClientPool clientPool : connectionPool.values()) {
            for (int i = 0; i < clientPool.clients.length; i++) {
                TransportClient client = clientPool.clients[i];
                if (client != null) {
                    clientPool.clients[i] = null;
                    JavaUtils.closeQuietly(client);
                }
            }
        }
        connectionPool.clear();

        if (workerGroup != null && !workerGroup.isShuttingDown()) {
            workerGroup.shutdownGracefully();
        }
    }

    private static class ClientPool {
        TransportClient[] clients;
        Object[] locks;
        volatile long lastConnectionFailed;

        ClientPool(int size) {
            clients = new TransportClient[size];
            locks = new Object[size];
            for (int i = 0; i < size; i++) {
                locks[i] = new Object();
            }
            lastConnectionFailed = 0;
        }
    }
}
