package com.miotech.kun.workflow.common.taskrun.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.workflow.common.task.dao.TaskDao;
import com.miotech.kun.workflow.core.model.common.Tag;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.common.Variable;
import com.miotech.kun.workflow.core.model.lineage.DataStore;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.db.ResultSetMapper;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import com.miotech.kun.workflow.utils.JSONUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


@Singleton
public class TaskRunMapper implements ResultSetMapper<TaskRun> {
    private static final TaskDao.TaskMapper taskMapper = TaskDao.TaskMapper.INSTANCE;
    private static final String TASK_RUN_MODEL_NAME = "taskrun";

    @Inject
    private TaskDao taskDao;

    @Override
    public TaskRun map(ResultSet rs) throws SQLException {
        rs.getLong(TASK_RUN_MODEL_NAME + "_task_id");
        Task task = !rs.wasNull() ? taskMapper.map(rs) : null;
        // assign tags property
        if (Objects.nonNull(task)) {
            List<Tag> tags = taskDao.fetchTaskTagsByTaskId(task.getId());
            task = task.cloneBuilder().withTags(tags).build();
        }

        return TaskRun.newBuilder()
                .withTask(task)
                .withId(rs.getLong(TASK_RUN_MODEL_NAME + "_id"))
                .withScheduledTick(new Tick(rs.getString(TASK_RUN_MODEL_NAME + "_scheduled_tick")))
                .withStatus(TaskRunStatus.resolve(rs.getString(TASK_RUN_MODEL_NAME + "_status")))
                .withInlets(JSONUtils.jsonToObject(rs.getString(TASK_RUN_MODEL_NAME + "_inlets"), new TypeReference<List<DataStore>>() {}))
                .withOutlets(JSONUtils.jsonToObject(rs.getString(TASK_RUN_MODEL_NAME + "_outlets"), new TypeReference<List<DataStore>>() {}))
                .withDependentTaskRunIds(Collections.emptyList())
                .withStartAt(DateTimeUtils.fromTimestamp(rs.getTimestamp(TASK_RUN_MODEL_NAME + "_start_at")))
                .withEndAt(DateTimeUtils.fromTimestamp(rs.getTimestamp(TASK_RUN_MODEL_NAME + "_end_at")))
                .withVariables(JSONUtils.jsonToObject(rs.getString(TASK_RUN_MODEL_NAME + "_variables"), new TypeReference<List<Variable>>() {}))
                .build();
    }
}
