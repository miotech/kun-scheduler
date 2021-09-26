package com.miotech.kun.dataplatform.facade;

import com.miotech.kun.dataplatform.facade.model.deploy.DeployedTask;

import java.util.List;
import java.util.Optional;

/**
 * Exposed interface of data-platform service module.
 * @author seaway
 */
public interface DeployedTaskFacade {

    /**
     * Query by definitionId
     * @param definitionId Unique ID of TaskDefinition model
     * @return Optional.
     */
    Optional<DeployedTask> findOptional(Long definitionId);

    /**
     * Query by workflowTaskId
     * @param workflowTaskId
     * @return
     */
    Optional<DeployedTask> findByWorkflowTaskId(Long workflowTaskId);

    /**
     * Query username by workflowTaskId
     * @param workflowTaskId
     * @return The list of username
     */
    List<String> getUserByTaskId(Long workflowTaskId);

}
