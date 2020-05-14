package com.miotech.kun.workflow.web;

import com.google.inject.Injector;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

public enum ServerContext {
    SERVER_CONTEXT;

    private Injector injector = null;

    /**
     * Ensure that injector is set only once!
     *
     * @param injector Guice injector is itself used for providing services.
     */
    public synchronized void setInjector(final Injector injector) {
        checkState(this.injector == null, "Injector is already set");
        this.injector = requireNonNull(injector, "arg injector is null");
    }

    public synchronized void unsetInjector() {
        this.injector = null;
    }

    public <T> T getInstance(final Class<T> clazz) {
        return requireNonNull(this.injector).getInstance(clazz);
    }

}

