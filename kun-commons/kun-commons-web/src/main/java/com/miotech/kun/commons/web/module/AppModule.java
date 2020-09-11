package com.miotech.kun.commons.web.module;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.miotech.kun.commons.utils.Props;


public abstract class AppModule extends AbstractModule {
    private final Props props;

    public AppModule(Props props) {
        this.props = props;
    }

    @Override
    protected void configure() {
        Names.bindProperties(binder(), props.toProperties());
        bind(Props.class).toInstance(props);
    }
}
