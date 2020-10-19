package com.miotech.kun.commons.network.utils;

import com.google.common.base.Preconditions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Provide a zero-copy way to convert data in ByteArrayOutputStream to ByteBuffer
 */
public class ByteBufferOutputStream extends ByteArrayOutputStream {
  public ByteBufferOutputStream() {
    this(32);
  }

  public ByteBufferOutputStream(int size) {
    super(size);
  }

  public int getCount() {
    return count;
  }

  private boolean closed = false;

  @Override
  public synchronized void write(int b) {
    Preconditions.checkArgument(!closed, "cannot write to a closed ByteBufferOutputStream");
    super.write(b);
  }

  @Override
  public synchronized void write(byte[] b, int off, int len) {
    Preconditions.checkArgument(!closed, "cannot write to a closed ByteBufferOutputStream");
    super.write(b, off, len);
  }

  @Override
  public void reset() {
    Preconditions.checkArgument(!closed, "cannot reset a closed ByteBufferOutputStream");
    super.reset();
  }

  @Override
  public void close() throws IOException {
    if (!closed) {
      super.close();
      closed = true;
    }
  }

  public ByteBuffer toByteBuffer() {
    Preconditions.checkArgument(closed, "can only call toByteBuffer() after ByteBufferOutputStream has been closed");
    return ByteBuffer.wrap(buf, 0, count);
  }
}