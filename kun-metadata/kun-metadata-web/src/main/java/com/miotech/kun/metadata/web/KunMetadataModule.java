package com.miotech.kun.metadata.web;

import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.commons.web.module.AppModule;
import com.miotech.kun.metadata.common.service.LineageService;
import com.miotech.kun.metadata.common.service.MetadataDatasetService;
import com.miotech.kun.metadata.facade.LineageServiceFacade;
import com.miotech.kun.metadata.facade.MetadataServiceFacade;


public class KunMetadataModule extends AppModule {
    private final Props props;

    public KunMetadataModule(Props props) {
        super(props);
        this.props = props;
    }

    @Override
    protected void configure() {
        super.configure();
        bind(MetadataServiceFacade.class).to(MetadataDatasetService.class);
        bind(LineageServiceFacade.class).to(LineageService.class);
    }


}
