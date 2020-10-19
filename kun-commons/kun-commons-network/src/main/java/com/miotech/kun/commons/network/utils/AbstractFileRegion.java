package com.miotech.kun.commons.network.utils;

import io.netty.channel.FileRegion;
import io.netty.util.AbstractReferenceCounted;

public abstract class AbstractFileRegion extends AbstractReferenceCounted implements FileRegion {

  @Override
  @SuppressWarnings("deprecation")
  public final long transfered() {
    return transferred();
  }

  @Override
  public AbstractFileRegion retain() {
    super.retain();
    return this;
  }

  @Override
  public AbstractFileRegion retain(int increment) {
    super.retain(increment);
    return this;
  }

  @Override
  public AbstractFileRegion touch() {
    super.touch();
    return this;
  }

  @Override
  public AbstractFileRegion touch(Object o) {
    return this;
  }
}
