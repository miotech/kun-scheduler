package com.miotech.kun.metadata.web;

import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.commons.web.module.AppModule;
import com.miotech.kun.metadata.facade.MetadataServiceFacade;
import com.miotech.kun.metadata.common.rpc.MetadataServiceFacadeImpl;


public class KunMetadataModule extends AppModule {
    private final Props props;

    public KunMetadataModule(Props props) {
        super(props);
        this.props = props;
    }

    @Override
    protected void configure() {
        super.configure();
        bind(MetadataServiceFacade.class).to(MetadataServiceFacadeImpl.class);
    }


}
