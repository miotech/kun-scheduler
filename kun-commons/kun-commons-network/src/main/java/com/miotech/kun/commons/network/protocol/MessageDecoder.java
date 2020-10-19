package com.miotech.kun.commons.network.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Decoder used by the client side to encode server-to-client responses.
 * This encoder is stateless so it is safe to be shared by multiple threads.
 */
@ChannelHandler.Sharable
public final class MessageDecoder extends MessageToMessageDecoder<ByteBuf> {

  private static final Logger logger = LoggerFactory.getLogger(MessageDecoder.class);

  public static final MessageDecoder INSTANCE = new MessageDecoder();

  private MessageDecoder() {}

  @Override
  public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
    Message.Type msgType = Message.Type.decode(in);
    Message decoded = decode(msgType, in);
    assert decoded.type() == msgType;
    logger.trace("Received message {}: {}", msgType, decoded);
    out.add(decoded);
  }

  private Message decode(Message.Type msgType, ByteBuf in) {
    switch (msgType) {

      case RpcRequest:
        return RpcRequest.decode(in);

      case RpcResponse:
        return RpcResponse.decode(in);

      case RpcFailure:
        return RpcFailure.decode(in);

      default:
        throw new IllegalArgumentException("Unexpected message type: " + msgType);
    }
  }
}
