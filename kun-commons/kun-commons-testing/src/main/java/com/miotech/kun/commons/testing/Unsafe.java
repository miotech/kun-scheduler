package com.miotech.kun.commons.testing;

import com.google.inject.Injector;

import static com.google.common.base.Preconditions.checkNotNull;

public class Unsafe {
    private static Injector inj;

    static synchronized void setInjector(Injector injector) {
        checkNotNull(injector, "injector should not be null.");
        inj = injector;
    }

    public static synchronized Injector getInjector() {
        checkNotNull(inj, "Injector is not initialized.");
        return inj;
    }
}
