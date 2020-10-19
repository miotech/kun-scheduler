package com.miotech.kun.commons.network.server;

import com.miotech.kun.commons.network.TestManagedBuffer;
import com.miotech.kun.commons.network.client.TransportClient;
import com.miotech.kun.commons.network.mock.MockFailRpcHandler;
import com.miotech.kun.commons.network.mock.MockNoOpRpcHandler;
import com.miotech.kun.commons.network.protocol.RpcFailure;
import com.miotech.kun.commons.network.protocol.RpcRequest;
import com.miotech.kun.commons.network.protocol.RpcResponse;
import com.miotech.kun.commons.network.utils.NettyUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class TransportRequestHandlerTest {

    @Test
    public void handleRpcRequestSuccess() {
        RpcHandler rpcHandler = new MockNoOpRpcHandler();
        TransportClient reverseClient = mock(TransportClient.class);
        Channel channel = mock(Channel.class);
        TransportRequestHandler requestHandler = new TransportRequestHandler(channel, reverseClient,
                rpcHandler);

        RpcRequest request = new RpcRequest(NettyUtils.requestId(), new TestManagedBuffer(0));
        when(channel.writeAndFlush(any(RpcResponse.class)))
                .thenReturn(mock(ChannelFuture.class));

        requestHandler.handle(request);

        verify(channel, times(1))
                .writeAndFlush(any(RpcResponse.class));
    }


    @Test
    public void handleRpcRequestFailed() {
        RpcHandler rpcHandler = new MockFailRpcHandler();
        TransportClient reverseClient = mock(TransportClient.class);
        Channel channel = mock(Channel.class);
        TransportRequestHandler requestHandler = new TransportRequestHandler(channel, reverseClient,
                rpcHandler);

        RpcRequest request = new RpcRequest(NettyUtils.requestId(), new TestManagedBuffer(0));

        when(channel.writeAndFlush(any(RpcFailure.class)))
                .thenReturn(mock(ChannelFuture.class));

        requestHandler.handle(request);

        verify(channel, times(1))
                .writeAndFlush(any(RpcFailure.class));
    }

}