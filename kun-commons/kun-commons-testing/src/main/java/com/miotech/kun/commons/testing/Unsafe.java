package com.miotech.kun.commons.testing;

import com.google.inject.Injector;

import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Preconditions.checkNotNull;

public class Unsafe {
    private static AtomicReference<Injector> inj = new AtomicReference<>(null);

    static synchronized void setInjector(Injector injector) {
        checkNotNull(injector, "injector should not be null.");
        inj.compareAndSet(null, injector);
    }

    public static synchronized Injector getInjector() {
        return inj.get();
    }
}
