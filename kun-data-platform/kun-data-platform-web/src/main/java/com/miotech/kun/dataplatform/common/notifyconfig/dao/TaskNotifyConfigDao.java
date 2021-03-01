package com.miotech.kun.dataplatform.common.notifyconfig.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.commons.utils.StringUtils;
import com.miotech.kun.dataplatform.model.notify.TaskNotifyConfig;
import com.miotech.kun.dataplatform.model.notify.TaskStatusNotifyTrigger;
import com.miotech.kun.dataplatform.notify.userconfig.NotifierUserConfig;
import com.miotech.kun.workflow.core.annotation.Internal;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import com.miotech.kun.workflow.utils.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
@SuppressWarnings("SqlResolve")
public class TaskNotifyConfigDao {
    private static final String TASK_NOTIFY_CONFIG_TABLE_NAME = "kun_dp_task_notify_config";

    private static final List<String> taskDefCols = Lists.newArrayList("id", "workflow_task_id", "notify_when", "notify_config", "created_at", "updated_at");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Query by notify config id
    private static final String NOTIFY_CONFIG_QUERY_BY_ID_STMT = String.format(
            "SELECT %s FROM %s WHERE id = ?",
            StringUtils.join(taskDefCols, ","),
            TASK_NOTIFY_CONFIG_TABLE_NAME
    );

    // Query by workflow id
    private static final String NOTIFY_CONFIG_QUERY_BY_WORKFLOW_TASK_ID_STMT = String.format(
            "SELECT %s FROM %s WHERE workflow_task_id = ?",
            StringUtils.join(taskDefCols, ","),
            TASK_NOTIFY_CONFIG_TABLE_NAME
    );

    // Insertion statement
    // INSERT INTO kun_dp_task_notify_config (workflow_task_id, notify_when, notify_config, created_at, updated_at) VALUES (?,?,?,?,?)
    private static final String NOTIFY_CONFIG_INSERTION_STMT = String.format(
            "INSERT INTO %s (%s) VALUES (%s)",
            TASK_NOTIFY_CONFIG_TABLE_NAME,
            StringUtils.join(taskDefCols.stream()
                    .filter(colName -> !Objects.equals(colName, "id"))
                    .collect(Collectors.toList()),
                    ","),
            StringUtils.repeatJoin("?", ",", taskDefCols.size() - 1)
    );

    // Update statement
    // UPDATE kun_dp_task_notify_config SET workflow_task_id = ?, notify_when = ?, notify_config = ?, updated_at = ? WHERE id = ?
    private static final String NOTIFY_CONFIG_UPDATE_STMT = String.format(
            "UPDATE %s SET %s WHERE id = ?",
            TASK_NOTIFY_CONFIG_TABLE_NAME,
            StringUtils.join(taskDefCols.stream()
                    .filter(colName -> !(Objects.equals(colName, "id") || Objects.equals(colName, "created_at")))
                    .map(colName -> colName + " = ?")
                    .collect(Collectors.toList()),
                    ",")
    );

    // Delete by id statement
    private static final String NOTIFY_CONFIG_DELETE_BY_ID_STMT = String.format("DELETE FROM %s WHERE id = ?", TASK_NOTIFY_CONFIG_TABLE_NAME);

    // Delete by id statement
    private static final String NOTIFY_CONFIG_DELETE_BY_WORKFLOW_TASK_ID_STMT = String.format("DELETE FROM %s WHERE workflow_task_id = ?", TASK_NOTIFY_CONFIG_TABLE_NAME);

    /**
     * Fetch a task notify config by its id
     * @param taskNotifyConfigId id of the configuration record
     * @return An optional object wrapper on TaskNotifyConfig
     */
    public Optional<TaskNotifyConfig> fetchById(Long taskNotifyConfigId) {
        Preconditions.checkNotNull(taskNotifyConfigId, "`taskNotifyConfigId` cannot be null");
        return jdbcTemplate.query(
                NOTIFY_CONFIG_QUERY_BY_ID_STMT,
                TaskNotifyConfigMapper.INSTANCE,
                taskNotifyConfigId
        ).stream().findAny();
    }

    /**
     * Fetch a task notify config by its bound workflow task id
     * @param workflowTaskId id of target workflow task
     * @return An optional object wrapper on TaskNotifyConfig
     */
    public Optional<TaskNotifyConfig> fetchByWorkflowTaskId(Long workflowTaskId) {
        Preconditions.checkNotNull(workflowTaskId, "`workflowTaskId` cannot be null");
        return jdbcTemplate.query(
                NOTIFY_CONFIG_QUERY_BY_WORKFLOW_TASK_ID_STMT,
                TaskNotifyConfigMapper.INSTANCE,
                workflowTaskId
        ).stream().findAny();
    }

    /**
     * Insert a TaskNotifyConfig record into database
     * @param taskNotifyConfig configuration object to persist.
     *                         All properties are required to be not null except `id`
     *                         since it will be auto generated.
     * @return Persisted TaskNotifyConfig record
     * @throws IllegalStateException when persisted record not found
     */
    public TaskNotifyConfig create(TaskNotifyConfig taskNotifyConfig) {
        // 1. Preconditions check
        checkTaskNotifyConfig(taskNotifyConfig, false);

        // 2. Convert properties
        String userConfigListJSON = JSONUtils.toJsonString(taskNotifyConfig.getNotifierConfigs());
        OffsetDateTime currentTime = DateTimeUtils.now();

        // 3. Perform insertion
        log.debug("Attempting to insert notify config record: workflow_task_id = {}, triggerType = {}, notifyConfigs = {}",
                taskNotifyConfig.getWorkflowTaskId(),
                taskNotifyConfig.getTriggerType().getTypeName(),
                userConfigListJSON
        );
        jdbcTemplate.update(
                NOTIFY_CONFIG_INSERTION_STMT,
                // id will be auto generated
                taskNotifyConfig.getWorkflowTaskId(),             // workflow_task_id
                taskNotifyConfig.getTriggerType().getTypeName(),  // notify_when
                userConfigListJSON,                               // notify_config
                currentTime,                                      // created_at
                currentTime                                       // updated_at
        );

        // 4. Fetch persisted record
        return fetchByWorkflowTaskId(taskNotifyConfig.getWorkflowTaskId()).orElseThrow(IllegalStateException::new);
    }

    /**
     * Update an existing TaskNotifyConfig record
     * @param taskNotifyConfig configuration object to persist. All properties are required to be not null.
     * @return Persisted TaskNotifyConfig record
     * @throws IllegalArgumentException when task notify config record does not exists
     * @throws IllegalStateException when persisted record not found
     */
    public TaskNotifyConfig update(TaskNotifyConfig taskNotifyConfig) {
        // 1. Preconditions check
        checkTaskNotifyConfig(taskNotifyConfig, true);

        // 2. Convert properties
        String userConfigListJSON = JSONUtils.toJsonString(taskNotifyConfig.getNotifierConfigs());
        OffsetDateTime currentTime = DateTimeUtils.now();

        // 3. Perform update
        log.debug("Attempting to update notify config record: id = {}, workflow_task_id = {}, triggerType = {}, notifyConfigs = {}",
                taskNotifyConfig.getId(),
                taskNotifyConfig.getWorkflowTaskId(),
                taskNotifyConfig.getTriggerType().getTypeName(),
                userConfigListJSON
        );
        int affectedRows = jdbcTemplate.update(
                NOTIFY_CONFIG_UPDATE_STMT,
                // update fields
                taskNotifyConfig.getWorkflowTaskId(),             // workflow_task_id
                taskNotifyConfig.getTriggerType().getTypeName(),  // notify_when
                userConfigListJSON,                               // notify_config
                currentTime,                                      // updated_at
                // id
                taskNotifyConfig.getId()
        );
        if (affectedRows == 0) {
            throw new IllegalArgumentException(String.format("Failed to update TaskNotifyConfig: id = %s. Record does not exist.", taskNotifyConfig.getId()));
        }

        // 4. Fetch persisted record
        return fetchById(taskNotifyConfig.getId()).orElseThrow(IllegalStateException::new);
    }

    /**
     * Remove a TaskNotifyConfig record by its id
     * @param taskNotifyConfigId id of the task notify config record to remove
     * @return {true} if success. {false} if target record not found.
     */
    public boolean removeById(Long taskNotifyConfigId) {
        // 1. Preconditions check
        Preconditions.checkNotNull(taskNotifyConfigId, "Argument `taskNotifyConfigId` cannot be null");

        // 2. Perform deletion
        int affectedRows = jdbcTemplate.update(NOTIFY_CONFIG_DELETE_BY_ID_STMT, taskNotifyConfigId);
        log.debug("Attempting to delete TaskNotifyConfig record by id = {}. Affected rows = {}", taskNotifyConfigId, affectedRows);

        // 3. Return success of not
        return affectedRows > 0;
    }

    /**
     * Remove a TaskNotifyConfig record by its bound workflow task id
     * @param workflowTaskId id of the config record to remove
     * @return {true} if success. {false} if target record not found.
     */
    public boolean removeByWorkflowTaskId(Long workflowTaskId) {
        // 1. Preconditions check
        Preconditions.checkNotNull(workflowTaskId, "Argument `taskNotifyConfigId` cannot be null");

        // 2. Perform deletion
        int affectedRows = jdbcTemplate.update(NOTIFY_CONFIG_DELETE_BY_WORKFLOW_TASK_ID_STMT, workflowTaskId);
        log.debug("Attempting to delete TaskNotifyConfig record by workflow task id = {}. Affected rows = {}", workflowTaskId, affectedRows);

        // 3. Return success of not
        return affectedRows > 0;
    }

    @Internal
    public static void checkTaskNotifyConfig(TaskNotifyConfig taskNotifyConfig, boolean checkId) {
        Preconditions.checkNotNull(taskNotifyConfig, "Argument `taskNotifyConfig` cannot be null");
        if (checkId) {
            Preconditions.checkArgument(taskNotifyConfig.getId() != null, "Property `id` cannot be null");
        }
        Preconditions.checkArgument(taskNotifyConfig.getWorkflowTaskId() != null, "Property `workflowTaskId` cannot be null");
        Preconditions.checkArgument(taskNotifyConfig.getTriggerType() != null, "Property `triggerType` cannot be null");
        Preconditions.checkArgument(taskNotifyConfig.getNotifierConfigs() != null, "Property `notifierConfigs` cannot be null");
    }

    /**
     * Mapper class for TaskNotifyConfig
     */
    private static class TaskNotifyConfigMapper implements RowMapper<TaskNotifyConfig> {
        public static final TaskNotifyConfigMapper INSTANCE = new TaskNotifyConfigMapper();

        private static final ObjectMapper objectMapper = new ObjectMapper();

        private static final TypeReference<List<NotifierUserConfig>> NOTIFIER_USER_CONFIG_LIST_TYPE_REF = new TypeReference<List<NotifierUserConfig>>() {};

        @Override
        public TaskNotifyConfig mapRow(ResultSet rs, int rowNum) throws SQLException {
            return TaskNotifyConfig.newBuilder()
                    .withId(rs.getLong("id"))
                    .withWorkflowTaskId(rs.getLong("workflow_task_id"))
                    .withTriggerType(TaskStatusNotifyTrigger.from(rs.getString("notify_when")))
                    .withNotifierConfigs(parseUserConfigList(rs.getString("notify_config")))
                    .build();
        }

        private static List<NotifierUserConfig> parseUserConfigList(String jsonStr) {
            try {
                return objectMapper.readValue(jsonStr, NOTIFIER_USER_CONFIG_LIST_TYPE_REF);
            } catch (JsonProcessingException e) {
                log.error("Failed to parse JSON string to type `NotifierUserConfig`: {}", jsonStr);
                throw ExceptionUtils.wrapIfChecked(e);
            }
        }
    }
}
