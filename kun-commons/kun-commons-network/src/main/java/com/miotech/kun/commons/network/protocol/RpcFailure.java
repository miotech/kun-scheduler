package com.miotech.kun.commons.network.protocol;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Objects;

/** Response to {@link RpcRequest} for a failed RPC. */
public final class RpcFailure extends AbstractMessage implements ResponseMessage {
  public final long requestId;
  public final String errorString;

  public RpcFailure(long requestId, String errorString) {
    this.requestId = requestId;
    this.errorString = errorString;
  }

  @Override
  public Message.Type type() { return Type.RpcFailure; }

  @Override
  public int encodedLength() {
    return 8 + Encoders.Strings.encodedLength(errorString);
  }

  @Override
  public void encode(ByteBuf buf) {
    buf.writeLong(requestId);
    Encoders.Strings.encode(buf, errorString);
  }

  public static RpcFailure decode(ByteBuf buf) {
    long requestId = buf.readLong();
    String errorString = Encoders.Strings.decode(buf);
    return new RpcFailure(requestId, errorString);
  }

  @Override
  public int hashCode() {
    return Objects.hash(requestId, errorString);
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof RpcFailure) {
      RpcFailure o = (RpcFailure) other;
      return requestId == o.requestId && errorString.equals(o.errorString);
    }
    return false;
  }

  @Override
   public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
      .append("requestId", requestId)
      .append("errorString", errorString)
      .toString();
  }
}
