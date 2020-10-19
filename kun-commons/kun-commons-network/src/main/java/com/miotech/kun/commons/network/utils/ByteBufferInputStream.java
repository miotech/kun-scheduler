package com.miotech.kun.commons.network.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Reads data from a ByteBuffer.
 */
public class ByteBufferInputStream extends InputStream {

  private ByteBuffer buffer;

  public ByteBufferInputStream(ByteBuffer buffer) {
    this.buffer = buffer;
  }

  @Override
  public int read() {
    if (buffer == null || buffer.remaining() == 0) {
      cleanUp();
      return -1;
    } else {
      return buffer.get() & 0xFF;
    }
  }

  @Override
  public int read(byte[] dest) throws IOException {
    return read(dest, 0, dest.length);
  }

  @Override
  public int read(byte[] dest, int off, int len) {
    if (buffer == null || buffer.remaining() == 0) {
      cleanUp();
      return -1;
    } else {
      int amountToGet = Math.min(buffer.remaining(), len);
      buffer.get(dest, off, amountToGet);
      return amountToGet;
    }
  }

  @Override
  public long skip(long bytes) {
    if (buffer != null) {
      int amountToSkip = (int) Math.min(bytes, buffer.remaining());
      buffer.position(buffer.position() + amountToSkip);
      if (buffer.remaining() == 0) {
        cleanUp();
      }
      return amountToSkip;
    } else {
      return 0L;
    }
  }

  /**
   * Clean up the buffer, and potentially dispose of it using StorageUtils.dispose().
   */
  private void cleanUp() {
    if (buffer != null) {
      buffer = null;
    }
  }
}