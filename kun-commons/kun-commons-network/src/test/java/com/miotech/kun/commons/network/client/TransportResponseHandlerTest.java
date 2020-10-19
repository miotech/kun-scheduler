package com.miotech.kun.commons.network.client;

import com.miotech.kun.commons.network.buffer.NioManagedBuffer;
import com.miotech.kun.commons.network.protocol.RpcFailure;
import com.miotech.kun.commons.network.protocol.RpcResponse;
import io.netty.channel.local.LocalChannel;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class TransportResponseHandlerTest {
    @Test
    public void handleSuccessfulRPC() throws Exception {
        TransportResponseHandler handler = new TransportResponseHandler(new LocalChannel());
        RpcResponseCallback callback = mock(RpcResponseCallback.class);
        // request
        handler.addRpcRequest(12345, callback);
        assertEquals(1, handler.numOutstandingRequests());

        // verify
        // This response should be ignored.
        handler.handle(new RpcResponse(54321, new NioManagedBuffer(ByteBuffer.allocate(7))));
        assertEquals(1, handler.numOutstandingRequests());

        ByteBuffer resp = ByteBuffer.allocate(10);
        handler.handle(new RpcResponse(12345, new NioManagedBuffer(resp)));
        verify(callback, times(1)).onSuccess(eq(ByteBuffer.allocate(10)));
        assertEquals(0, handler.numOutstandingRequests());
    }

    @Test
    public void handleFailedRPC() throws Exception {
        TransportResponseHandler handler = new TransportResponseHandler(new LocalChannel());
        RpcResponseCallback callback = mock(RpcResponseCallback.class);
        handler.addRpcRequest(12345, callback);
        assertEquals(1, handler.numOutstandingRequests());

        handler.handle(new RpcFailure(54321, "uh-oh!")); // should be ignored
        assertEquals(1, handler.numOutstandingRequests());

        handler.handle(new RpcFailure(12345, "oh no"));
        verify(callback, times(1)).onFailure(any());
        assertEquals(0, handler.numOutstandingRequests());
    }
}