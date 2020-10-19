package com.miotech.kun.commons.network.protocol;

import com.miotech.kun.commons.network.buffer.ManagedBuffer;
import com.miotech.kun.commons.network.buffer.NettyManagedBuffer;
import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Objects;

public final class RpcRequest extends AbstractMessage implements RequestMessage {
  /** Used to link an RPC request with its response. */
  public final long requestId;

  public RpcRequest(long requestId, ManagedBuffer message) {
    super(message, true);
    this.requestId = requestId;
  }

  @Override
  public Message.Type type() { return Type.RpcRequest; }

  @Override
  public int encodedLength() {
    // The integer is the length of RequestId (8 Bytes), body length is already included in frame length
    return 8;
  }

  @Override
  public void encode(ByteBuf buf) {
    // only encode requestId, data is sent by zero-copy transfer
    buf.writeLong(requestId);
  }

  public static RpcRequest decode(ByteBuf buf) {
    long requestId = buf.readLong();
    return new RpcRequest(requestId, new NettyManagedBuffer(buf.retain()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(requestId, body());
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof RpcRequest) {
      RpcRequest o = (RpcRequest) other;
      return requestId == o.requestId && super.equals(o);
    }
    return false;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
      .append("requestId", requestId)
      .append("body", body())
      .toString();
  }
}
