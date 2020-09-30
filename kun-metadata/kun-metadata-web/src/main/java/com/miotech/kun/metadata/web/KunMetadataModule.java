package com.miotech.kun.metadata.web;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.commons.web.annotation.BasePackageScan;
import com.miotech.kun.metadata.facade.MetadataServiceFacade;
import com.miotech.kun.metadata.web.constant.PropKey;
import com.miotech.kun.metadata.web.rpc.MetadataServiceFacadeImpl;
import com.miotech.kun.workflow.client.DefaultWorkflowClient;
import com.miotech.kun.workflow.client.WorkflowClient;

public class KunMetadataModule extends AbstractModule {
    private final Props props;

    public KunMetadataModule(Props props) {
        this.props = props;
    }

    @Override
    protected void configure() {
        bind(MetadataServiceFacade.class).to(MetadataServiceFacadeImpl.class);
    }

    @Provides
    @Singleton
    @BasePackageScan
    public String getPackageScan() {
        return this.getClass().getPackage().getName();
    }

    @Provides
    @Singleton
    public WorkflowClient getWorkflowClient() {
        return new DefaultWorkflowClient(props.get(PropKey.WORKFLOW_URL));
    }
}
