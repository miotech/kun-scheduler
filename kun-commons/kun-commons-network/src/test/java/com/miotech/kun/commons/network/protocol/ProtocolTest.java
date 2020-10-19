package com.miotech.kun.commons.network.protocol;

import com.google.common.primitives.Ints;
import com.miotech.kun.commons.network.TestManagedBuffer;
import com.miotech.kun.commons.network.mock.ByteArrayWritableChannel;
import com.miotech.kun.commons.network.utils.NettyUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.FileRegion;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ProtocolTest {

  /**
   * test util function to mock server sent message to client
   * @param msg
   */
  private void testServerToClient(Message msg) {
    EmbeddedChannel serverChannel = new EmbeddedChannel(new FileRegionEncoder(), MessageEncoder.INSTANCE);
    serverChannel.writeOutbound(msg);

    EmbeddedChannel clientChannel = new EmbeddedChannel(
        NettyUtils.createFrameDecoder(), MessageDecoder.INSTANCE);

    while (!serverChannel.outboundMessages().isEmpty()) {
      clientChannel.writeOneInbound(serverChannel.readOutbound());
    }

    assertEquals(1, clientChannel.inboundMessages().size());
    assertEquals(msg, clientChannel.readInbound());
  }

  /**
   * test util function to mock client sent message to server
   * @param msg
   */
  private void testClientToServer(Message msg) {
    EmbeddedChannel clientChannel = new EmbeddedChannel(new FileRegionEncoder(),
      MessageEncoder.INSTANCE);
    clientChannel.writeOutbound(msg);

    EmbeddedChannel serverChannel = new EmbeddedChannel(
        NettyUtils.createFrameDecoder(), MessageDecoder.INSTANCE);

    while (!clientChannel.outboundMessages().isEmpty()) {
      serverChannel.writeOneInbound(clientChannel.readOutbound());
    }

    assertEquals(1, serverChannel.inboundMessages().size());
    assertEquals(msg, serverChannel.readInbound());
  }

  @Test
  public void requests() {
    testClientToServer(new RpcRequest(12345, new TestManagedBuffer(0)));
    testClientToServer(new RpcRequest(12345, new TestManagedBuffer(10)));
  }

  @Test
  public void responses() {
    testServerToClient(new RpcResponse(12345, new TestManagedBuffer(0)));
    testServerToClient(new RpcResponse(12345, new TestManagedBuffer(100)));
    testServerToClient(new RpcFailure(0, "this is an error"));
    testServerToClient(new RpcFailure(0, ""));
  }

  private static class FileRegionEncoder extends MessageToMessageEncoder<FileRegion> {

    @Override
    public void encode(ChannelHandlerContext ctx, FileRegion in, List<Object> out)
            throws Exception {

      ByteArrayWritableChannel channel = new ByteArrayWritableChannel(Ints.checkedCast(in.count()));
      while (in.transferred() < in.count()) {
        in.transferTo(channel, in.transferred());
      }
      out.add(Unpooled.wrappedBuffer(channel.getData()));
    }

  }
}
