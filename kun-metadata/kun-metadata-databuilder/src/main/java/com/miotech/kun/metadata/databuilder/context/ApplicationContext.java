package com.miotech.kun.metadata.databuilder.context;

import com.google.common.base.Preconditions;
import com.google.inject.Guice;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.databuilder.operator.BuilderModule;

import java.util.concurrent.atomic.AtomicBoolean;

public class ApplicationContext {

    private static AtomicBoolean initialized = new AtomicBoolean(false);
    private static Context context;

    public synchronized static void init(Props props) {
        Preconditions.checkNotNull(props, "props should not be null");
        if (initialized.compareAndSet(false, true)) {
            context = new Context(props, Guice.createInjector(new BuilderModule(props)));
        }
    }

    public static Context getContext() {
        Preconditions.checkNotNull(context, "context is not initialized");
        return context;
    }
}
