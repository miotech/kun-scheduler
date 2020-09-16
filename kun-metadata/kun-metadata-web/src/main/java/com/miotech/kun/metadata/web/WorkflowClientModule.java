package com.miotech.kun.metadata.web;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.miotech.kun.commons.utils.Props;
import com.miotech.kun.metadata.web.constant.PropKey;
import com.miotech.kun.workflow.client.DefaultWorkflowClient;
import com.miotech.kun.workflow.client.WorkflowClient;


public class WorkflowClientModule extends AbstractModule {

    private final Props props;

    public WorkflowClientModule(Props props) {
        this.props = props;
    }

    @Provides
    @Singleton
    public WorkflowClient getWorkflowClient() {
        return new DefaultWorkflowClient(props.get(PropKey.WORKFLOW_URL));
    }
}
