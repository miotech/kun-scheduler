package com.miotech.kun.commons.network.client;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.SettableFuture;
import com.miotech.kun.commons.network.buffer.NioManagedBuffer;
import com.miotech.kun.commons.network.protocol.RpcRequest;
import com.miotech.kun.commons.utils.ExceptionUtils;
import io.netty.channel.Channel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.miotech.kun.commons.network.utils.NettyUtils.getRemoteAddress;
import static com.miotech.kun.commons.network.utils.NettyUtils.requestId;

public class TransportClient implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(TransportClient.class);

    private final Channel channel;
    private final TransportResponseHandler handler;
    private volatile boolean timedOut;

    public TransportClient(Channel channel, TransportResponseHandler handler) {
        this.channel = Preconditions.checkNotNull(channel);
        this.handler = Preconditions.checkNotNull(handler);
        this.timedOut = false;
    }

    public boolean isActive() {
        return !timedOut && (channel.isOpen() || channel.isActive());
    }

    public void timeOut() {
        this.timedOut = true;
    }

    @VisibleForTesting
    public TransportResponseHandler getHandler() {
        return handler;
    }

    @Override
    public void close() throws IOException {
        channel.close().awaitUninterruptibly(10, TimeUnit.SECONDS);
    }

    /**
     * Sends an opaque message to the RpcHandler on the server-side. The callback will be invoked
     * with the server's response or upon any failure.
     *
     * @param message The message to send.
     * @param callback Callback to handle the RPC's reply.
     * @return The RPC's id.
     */
    public long sendRpc(ByteBuffer message, RpcResponseCallback callback) {
        if (logger.isDebugEnabled()) {
            logger.trace("Sending RPC to {}", getRemoteAddress(channel));
        }

        long requestId = requestId();
        handler.addRpcRequest(requestId, callback);

        RpcChannelListener listener = new RpcChannelListener(requestId, callback);
        channel.writeAndFlush(new RpcRequest(requestId, new NioManagedBuffer(message)))
                .addListener(listener);

        return requestId;

    }

    /**
     * Synchronously sends an opaque message to the RpcHandler on the server-side, waiting for up to
     * a specified timeout for a response.
     */
    public ByteBuffer sendRpcSync(ByteBuffer message, long timeoutMs) {
        final SettableFuture<ByteBuffer> result = SettableFuture.create();

        sendRpc(message, new RpcResponseCallback() {
            @Override
            public void onSuccess(ByteBuffer response) {
                try {
                    ByteBuffer copy = ByteBuffer.allocate(response.remaining());
                    copy.put(response);
                    // flip "copy" to make it readable
                    copy.flip();
                    result.set(copy);
                } catch (Throwable t) {
                    logger.warn("Error in responding PRC callback", t);
                    result.setException(t);
                }
            }

            @Override
            public void onFailure(Throwable e) {
                result.setException(e);
            }
        });

        try {
            return result.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            throw ExceptionUtils.wrapIfChecked(e.getCause());
        } catch (Exception e) {
            throw ExceptionUtils.wrapIfChecked(e);
        }
    }

    /**
     * Sends an opaque message to the RpcHandler on the server-side. No reply is expected for the
     * message, and no delivery guarantees are made.
     *
     * @param message The message to send.
     */
    public void send(ByteBuffer message) {
        throw new UnsupportedOperationException();
    }

    public void removeRpcRequest(long requestId) {
        handler.removeRpcRequest(requestId);
    }

    public Channel getChannel() {
        return this.channel;
    }

    public SocketAddress getSocketAddress() {
        return this.channel.remoteAddress();
    }

    private class StdChannelListener
            implements GenericFutureListener<Future<? super Void>> {
        final long startTime;
        final Object requestId;

        StdChannelListener(Object requestId) {
            this.startTime = System.currentTimeMillis();
            this.requestId = requestId;
        }

        @Override
        public void operationComplete(Future<? super Void> future) throws Exception {
            if (future.isSuccess()) {
                if (logger.isDebugEnabled()) {
                    long timeTaken = System.currentTimeMillis() - startTime;
                    logger.debug("Sending request {} to {} took {} ms", requestId,
                            getRemoteAddress(channel), timeTaken);
                }
            } else {
                String errorMsg = String.format("Failed to send RPC %s to %s: %s", requestId,
                        getRemoteAddress(channel), future.cause());
                logger.error(errorMsg, future.cause());
                channel.close();
                try {
                    handleFailure(errorMsg, future.cause());
                } catch (Exception e) {
                    logger.error("Uncaught exception in RPC response callback handler!", e);
                }
            }
        }

        /**
         * failure handler when sent call failed
         * @param errorMsg
         * @param cause
         * @throws Exception
         */
        void handleFailure(String errorMsg, Throwable cause) throws Exception {}
    }

    private class RpcChannelListener extends StdChannelListener {
        final long rpcRequestId;
        final RpcResponseCallback callback;

        RpcChannelListener(long rpcRequestId, RpcResponseCallback callback) {
            super("RPC " + rpcRequestId);
            this.rpcRequestId = rpcRequestId;
            this.callback = callback;
        }

        @Override
        void handleFailure(String errorMsg, Throwable cause) {
            handler.removeRpcRequest(rpcRequestId);
            callback.onFailure(new IOException(errorMsg, cause));
        }
    }
}
