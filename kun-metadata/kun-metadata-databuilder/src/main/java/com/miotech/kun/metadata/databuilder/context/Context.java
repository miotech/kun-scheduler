package com.miotech.kun.metadata.databuilder.context;

import com.google.inject.Injector;
import com.miotech.kun.commons.utils.Props;

public class Context {

    private final Props props;

    private final Injector injector;

    public Context(Props props, Injector injector) {
        this.props = props;
        this.injector = injector;
    }

    public Props getProps() {
        return props;
    }

    public Injector getInjector() {
        return injector;
    }
}
