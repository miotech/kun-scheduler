package com.miotech.kun.commons.network.mock;

import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

/**
 * A writable channel that stores the written data in a byte array in memory.
 */
public class ByteArrayWritableChannel implements WritableByteChannel {

  private final byte[] data;
  private int offset;

  public ByteArrayWritableChannel(int size) {
    this.data = new byte[size];
  }

  public byte[] getData() {
    return data;
  }

  public int length() {
    return offset;
  }

  /** Resets the channel so that writing to it will overwrite the existing buffer. */
  public void reset() {
    offset = 0;
  }

  /**
   * Reads from the given buffer into the internal byte array.
   */
  @Override
  public int write(ByteBuffer src) {
    int toTransfer = Math.min(src.remaining(), data.length - offset);
    src.get(data, offset, toTransfer);
    offset += toTransfer;
    return toTransfer;
  }

  @Override
  public void close() {

  }

  @Override
  public boolean isOpen() {
    return true;
  }

}
