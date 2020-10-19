package com.miotech.kun.commons.network.protocol;

import io.netty.buffer.ByteBuf;

public interface Encodable {
  /** Number of bytes of the encoded form of this object. */
  int encodedLength();

  /**
   * Serializes this object by writing into the given ByteBuf.
   * This method must write exactly encodedLength() bytes.
   */
  void encode(ByteBuf buf);
}
