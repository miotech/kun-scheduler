package com.miotech.kun.dataplatform.facade;

import com.miotech.kun.dataplatform.facade.model.deploy.DeployedTask;
import com.miotech.kun.security.model.UserInfo;

import java.util.List;
import java.util.Map;
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
     * Query userinfo by workflowTaskId
     * @param workflowTaskId
     * @return The info of user
     */
    UserInfo getUserByTaskId(Long workflowTaskId);

    /**
     * Query by workflowTaskIds
     * @param taskIds
     * @return
     */
    Map<Long, DeployedTask> findByWorkflowTaskIds(List<Long> taskIds);
}
