package com.miotech.kun.workflow.common;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.facade.MetadataServiceFacade;
import com.miotech.kun.workflow.common.rpc.DefaultMetadataServiceConsumer;

public abstract class AppModule extends AbstractModule {
    private final Props props;

    public AppModule(Props props) {
        this.props = props;
    }

    @Override
    protected void configure() {
        Names.bindProperties(binder(), props.toProperties());
        bind(Props.class).toInstance(props);
        bind(MetadataServiceFacade.class).toInstance(new DefaultMetadataServiceConsumer());
    }
}
