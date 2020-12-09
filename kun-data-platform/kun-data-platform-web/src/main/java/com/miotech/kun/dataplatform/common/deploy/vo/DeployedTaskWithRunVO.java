package com.miotech.kun.dataplatform.common.deploy.vo;

import com.miotech.kun.dataplatform.model.taskdefinition.TaskPayload;
import com.miotech.kun.workflow.client.model.TaskRun;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public class DeployedTaskWithRunVO extends DeployedTaskVO {
    private final TaskRun latestTaskRun;

    public DeployedTaskWithRunVO(Long id,
                                 Long workflowTaskId,
                                 Long taskDefinitionId,
                                 String name,
                                 String taskTemplateName,
                                 Long owner,
                                 boolean archived,
                                 TaskPayload taskPayload,
                                 TaskRun latestTaskRun) {
        super(id, workflowTaskId, taskDefinitionId, name, taskTemplateName, owner, archived, taskPayload);
        this.latestTaskRun = latestTaskRun;
    }
}
