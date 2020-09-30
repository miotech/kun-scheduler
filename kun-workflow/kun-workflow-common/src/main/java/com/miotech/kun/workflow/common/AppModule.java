package com.miotech.kun.workflow.common;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.miotech.kun.commons.rpc.RpcConsumer;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.facade.MetadataServiceFacade;

public abstract class AppModule extends AbstractModule {
    protected final Props props;

    public AppModule(Props props) {
        this.props = props;
    }

    @Override
    protected void configure() {
        Names.bindProperties(binder(), props.toProperties());
        bind(Props.class).toInstance(props);
    }

    @Singleton
    @Provides
    public MetadataServiceFacade metadataService(RpcConsumer rpcConsumer) {
        return rpcConsumer.getService("default", MetadataServiceFacade.class, "1.0");
    }
}
