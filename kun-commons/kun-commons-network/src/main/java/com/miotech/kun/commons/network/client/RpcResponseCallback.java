package com.miotech.kun.commons.network.client;

import java.nio.ByteBuffer;

/**
 * Callback for the result of a single RPC. This will be invoked once with either success or
 * failure.
 */
public interface RpcResponseCallback {
  /**
   * Successful serialized result from server.
   *
   * After `onSuccess` returns, `response` will be recycled and its content will become invalid.
   * Please copy the content of `response` if you want to use it after `onSuccess` returns.
   */
  void onSuccess(ByteBuffer response);

  /** Exception either propagated from server or raised on client side. */
  void onFailure(Throwable e);
}
