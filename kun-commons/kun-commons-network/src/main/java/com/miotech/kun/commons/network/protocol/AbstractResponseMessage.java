package com.miotech.kun.commons.network.protocol;

import com.miotech.kun.commons.network.buffer.ManagedBuffer;

/**
 * Abstract class for response messages.
 */
public abstract class AbstractResponseMessage extends AbstractMessage implements ResponseMessage {

  protected AbstractResponseMessage(ManagedBuffer body, boolean isBodyInFrame) {
    super(body, isBodyInFrame);
  }

  public abstract ResponseMessage createFailureResponse(String error);
}
