package com.miotech.kun.commons.network.utils;

import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.internal.PlatformDependent;

import java.util.UUID;
import java.util.concurrent.ThreadFactory;

public class NettyUtils {

    private NettyUtils() {}

    private static int MAX_DEFAULT_NETTY_THREADS = 8;

    private static final PooledByteBufAllocator[] _sharedPooledByteBufAllocator =
            new PooledByteBufAllocator[2];

    /** Creates a new ThreadFactory which prefixes each thread with the given name. */
    public static ThreadFactory createThreadFactory(String threadPoolPrefix) {
        return new DefaultThreadFactory(threadPoolPrefix, true);
    }

    /** Creates a Netty EventLoopGroup based on the IOMode. */
    public static EventLoopGroup createEventLoop(IOMode mode, int numThreads, String threadPrefix) {
        ThreadFactory threadFactory = createThreadFactory(threadPrefix);

        switch (mode) {
            case NIO:
                return new NioEventLoopGroup(numThreads, threadFactory);
            case EPOLL:
                return new EpollEventLoopGroup(numThreads, threadFactory);
            default:
                throw new IllegalArgumentException("Unknown io mode: " + mode);
        }
    }

    /** Returns the correct (client) SocketChannel class based on IOMode. */
    public static Class<? extends Channel> getClientChannelClass(IOMode mode) {
        switch (mode) {
            case NIO:
                return NioSocketChannel.class;
            case EPOLL:
                return EpollSocketChannel.class;
            default:
                throw new IllegalArgumentException("Unknown io mode: " + mode);
        }
    }

    /** Returns the correct ServerSocketChannel class based on IOMode. */
    public static Class<? extends ServerChannel> getServerChannelClass(IOMode mode) {
        switch (mode) {
            case NIO:
                return NioServerSocketChannel.class;
            case EPOLL:
                return EpollServerSocketChannel.class;
            default:
                throw new IllegalArgumentException("Unknown io mode: " + mode);
        }
    }

    /**
     * Returns the default number of threads for both the Netty client and server thread pools.
     * If numUsableCores is 0, we will use Runtime get an approximate number of available cores.
     */
    public static int defaultNumThreads(int numUsableCores) {
        final int availableCores;
        if (numUsableCores > 0) {
            availableCores = numUsableCores;
        } else {
            availableCores = Runtime.getRuntime().availableProcessors();
        }
        return Math.min(availableCores, MAX_DEFAULT_NETTY_THREADS);
    }

    /**
     * Returns the lazily created shared pooled ByteBuf allocator for the specified allowCache
     * parameter value.
     */
    public static synchronized PooledByteBufAllocator getSharedPooledByteBufAllocator(
            boolean allowDirectBufs,
            boolean allowCache) {
        final int index = allowCache ? 0 : 1;
        if (_sharedPooledByteBufAllocator[index] == null) {
            _sharedPooledByteBufAllocator[index] =
                    createPooledByteBufAllocator(
                            allowDirectBufs,
                            allowCache,
                            defaultNumThreads(0));
        }
        return _sharedPooledByteBufAllocator[index];
    }

    /**
     * Create a pooled ByteBuf allocator but disables the thread-local cache. Thread-local caches
     * are disabled for TransportClients because the ByteBufs are allocated by the event loop thread,
     * but released by the executor thread rather than the event loop thread. Those thread-local
     * caches actually delay the recycling of buffers, leading to larger memory usage.
     */
    public static PooledByteBufAllocator createPooledByteBufAllocator(
            boolean allowDirectBufs,
            boolean allowCache,
            int numCores) {
        if (numCores == 0) {
            numCores = Runtime.getRuntime().availableProcessors();
        }
        return new PooledByteBufAllocator(
                allowDirectBufs && PlatformDependent.directBufferPreferred(),
                Math.min(PooledByteBufAllocator.defaultNumHeapArena(), numCores),
                Math.min(PooledByteBufAllocator.defaultNumDirectArena(), allowDirectBufs ? numCores : 0),
                PooledByteBufAllocator.defaultPageSize(),
                PooledByteBufAllocator.defaultMaxOrder(),
                allowCache ? PooledByteBufAllocator.defaultTinyCacheSize() : 0,
                allowCache ? PooledByteBufAllocator.defaultSmallCacheSize() : 0,
                allowCache ? PooledByteBufAllocator.defaultNormalCacheSize() : 0,
                allowCache ? PooledByteBufAllocator.defaultUseCacheForAllThreads() : false
        );
    }

    /** Returns the remote address on the channel or "&lt;unknown remote&gt;" if none exists. */
    public static String getRemoteAddress(Channel channel) {
        if (channel != null && channel.remoteAddress() != null) {
            return channel.remoteAddress().toString();
        }
        return "<unknown remote>";
    }

    public static long requestId() {
        return Math.abs(UUID.randomUUID().getLeastSignificantBits());
    }

    public static TransportFrameDecoder createFrameDecoder() {
        return new TransportFrameDecoder();
    }
}
