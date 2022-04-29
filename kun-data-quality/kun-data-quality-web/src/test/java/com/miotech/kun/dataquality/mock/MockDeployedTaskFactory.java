package com.miotech.kun.dataquality.mock;

import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataplatform.facade.model.deploy.DeployedTask;

public class MockDeployedTaskFactory {

    private MockDeployedTaskFactory() {
    }

    public static DeployedTask create() {
        return DeployedTask.newBuilder()
                .withId(IdGenerator.getInstance().nextId())
                .withName("deployed task")
                .withDefinitionId(IdGenerator.getInstance().nextId())
                .withTaskTemplateName("template name")
                .withWorkflowTaskId(IdGenerator.getInstance().nextId())
                .withOwner(IdGenerator.getInstance().nextId())
                .withTaskCommit(null)
                .withArchived(false)
                .build();
    }

}
