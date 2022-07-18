package com.miotech.kun.dataplatform.facade;

import com.miotech.kun.dataplatform.facade.model.taskdefinition.TaskDefinition;
import com.miotech.kun.dataplatform.facade.model.taskdefinition.TaskTry;

import java.util.List;
import java.util.Optional;

/**
 * Expose TaskDefinition interface
 */
public interface TaskDefinitionFacade {

    /**
     * Query TaskTry according to taskRunId
     * @param taskRunId
     * @return
     */
    Optional<TaskTry> findTaskTryByTaskRunId(Long taskRunId);

    /**
     * Query TaskDefinition according to taskDefinitionId
     * @param taskDefId
     * @return
     */
    TaskDefinition find(Long taskDefId);

    /**
     * Query TaskDefinitions according to taskDefIds
     * @param taskDefIds
     * @return
     */
    List<TaskDefinition> findByDefIds(List<Long> taskDefIds);

}
