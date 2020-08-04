package com.miotech.kun.metadata.web;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.miotech.kun.metadata.web.constant.PropKey;
import com.miotech.kun.workflow.client.DefaultWorkflowClient;
import com.miotech.kun.workflow.client.WorkflowClient;

import java.util.Properties;

public class WorkflowClientModule extends AbstractModule {

    private final Properties properties;

    public WorkflowClientModule(Properties props) {
        this.properties = props;
    }

    @Provides
    @Singleton
    public WorkflowClient getWorkflowClient() {
        return new DefaultWorkflowClient(properties.getProperty(PropKey.WORKFLOW_URL));
    }
}
