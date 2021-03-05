package com.miotech.kun.dataplatform.common.notifyconfig.service;

import com.google.common.base.Preconditions;
import com.miotech.kun.dataplatform.common.notifyconfig.dao.TaskNotifyConfigDao;
import com.miotech.kun.dataplatform.model.notify.TaskNotifyConfig;
import com.miotech.kun.dataplatform.model.taskdefinition.TaskDefNotifyConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class TaskNotifyConfigService {
    @Autowired
    private TaskNotifyConfigDao taskNotifyConfigDao;

    /**
     * Fetch an task notify config record by its id
     * @param notifyConfigId Id of target config record
     * @return An optional object wrapper on TaskNotifyConfig
     */
    public Optional<TaskNotifyConfig> fetchTaskNotifyConfigById(Long notifyConfigId) {
        Preconditions.checkNotNull(notifyConfigId, "Task notify config id cannot be null");
        return taskNotifyConfigDao.fetchById(notifyConfigId);
    }

    /**
     * Fetch an task notify config record by its bound workflow task id
     * @param workflowTaskId Id of bounded workflow task to target config record
     * @return An optional object wrapper on TaskNotifyConfig
     */
    public Optional<TaskNotifyConfig> fetchTaskNotifyConfigByWorkflowTaskId(Long workflowTaskId) {
        Preconditions.checkNotNull(workflowTaskId, "Workflow task id cannot be null");
        return taskNotifyConfigDao.fetchByWorkflowTaskId(workflowTaskId);
    }

    /**
     * Update or insert a task notification record. If property `id` exists then update action
     * will be performed. Else will do an insertion.
     * @param taskNotifyConfig the task notify config model object
     * @return Updated notify config object
     */
    @Transactional
    public TaskNotifyConfig upsertTaskNotifyConfig(TaskNotifyConfig taskNotifyConfig) {
        // 1. Preconditions check: properties should not be null
        TaskNotifyConfigDao.checkTaskNotifyConfig(taskNotifyConfig, false);

        Long id = taskNotifyConfig.getId();
        if (Objects.nonNull(id)) {
            // 2. If id is present, do update
            log.debug("Updating task notify config with id = {}.", id);
            return doUpdate(taskNotifyConfig);
        }
        // else
        // 3. If id is null, do insertion
        log.debug("Task notify config id not presenting. Performing insertion.");
        return doInsert(taskNotifyConfig);
    }

    private TaskNotifyConfig doUpdate(TaskNotifyConfig taskNotifyConfigUpdate) {
        Long id = taskNotifyConfigUpdate.getId();
        Optional<TaskNotifyConfig> configOptional = taskNotifyConfigDao.fetchById(id);
        if (!configOptional.isPresent()) {
            throw new IllegalArgumentException(String.format("Cannot find target config record with id = %s", id));
        }
        // if presented
        return taskNotifyConfigDao.update(taskNotifyConfigUpdate);
    }

    private TaskNotifyConfig doInsert(TaskNotifyConfig taskNotifyConfigToInsert) {
        return taskNotifyConfigDao.create(taskNotifyConfigToInsert);
    }

    /**
     * Remove a task notification record by its id.
     * @param taskNotifyConfigId Id of target config record
     * @return {true} if success. {false} if failed to find target notification record.
     */
    public Boolean removeTaskNotifyConfigById(Long taskNotifyConfigId) {
        Preconditions.checkNotNull(taskNotifyConfigId, "id of target config record to be removed should not be null");
        return taskNotifyConfigDao.removeById(taskNotifyConfigId);
    }

    /**
     * Remove a task notification record by its bound workflow id.
     * @param workflowTaskId Id of bounded workflow task to target config record
     * @return {true} if success. {false} if failed to find target notification record.
     */
    public Boolean removeTaskNotifyConfigByWorkflowTaskId(Long workflowTaskId) {
        Preconditions.checkNotNull(workflowTaskId, "id of workflow task id should not be null");
        return taskNotifyConfigDao.removeByWorkflowTaskId(workflowTaskId);
    }

    @Transactional
    public void updateRelatedTaskNotificationConfig(Long workflowTaskId, TaskDefNotifyConfig taskDefNotifyConfig) {
        Optional<TaskNotifyConfig> currentTaskNotifyConfigOptional = fetchTaskNotifyConfigByWorkflowTaskId(workflowTaskId);
        if (currentTaskNotifyConfigOptional.isPresent()) {
            TaskNotifyConfig currentTaskNotifyConfig = currentTaskNotifyConfigOptional.get();
            // if present, do update
            upsertTaskNotifyConfig(
                    currentTaskNotifyConfig
                            .cloneBuilder()
                            .withNotifierConfigs(taskDefNotifyConfig.getNotifierUserConfigList())
                            .withTriggerType(taskDefNotifyConfig.getNotifyWhen())
                            .build()
            );
        } else {
            // else, do insert
            upsertTaskNotifyConfig(
                    TaskNotifyConfig.newBuilder()
                            .withNotifierConfigs(taskDefNotifyConfig.getNotifierUserConfigList())
                            .withTriggerType(taskDefNotifyConfig.getNotifyWhen())
                            .build()
            );
        }
    }
}
