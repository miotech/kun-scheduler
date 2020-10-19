package com.miotech.kun.commons.network.buffer;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * A {@link ManagedBuffer} backed by {@link ByteBuffer}.
 */
public class NioManagedBuffer extends ManagedBuffer {
  private final ByteBuffer buf;

  public NioManagedBuffer(ByteBuffer buf) {
    this.buf = buf;
  }

  @Override
  public long size() {
    return buf.remaining();
  }

  @Override
  public ByteBuffer nioByteBuffer() throws IOException {
    return buf.duplicate();
  }

  @Override
  public InputStream createInputStream() throws IOException {
    return new ByteBufInputStream(Unpooled.wrappedBuffer(buf));
  }

  @Override
  public ManagedBuffer retain() {
    return this;
  }

  @Override
  public ManagedBuffer release() {
    return this;
  }

  @Override
  public Object convertToNetty() throws IOException {
    return Unpooled.wrappedBuffer(buf);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
      .append("buf", buf)
      .toString();
  }
}

