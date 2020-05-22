package com.miotech.kun.common.dao;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.taskrun.TaskRun;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.db.DatabaseOperator;
import com.miotech.kun.workflow.db.ResultSetMapper;
import com.miotech.kun.workflow.utils.DateTimeUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@Singleton
public class TaskRunDao extends AbstractDao<TaskRun>{

    @Inject
    private DatabaseOperator dbOperator;

    @Override
    public String getTableName() {
        return "kun_wf_task_run";
    }

    @Override
    public Map<String, String> getTableColumnMapping() {
        return ImmutableMap.<String, String>builder()
                .put("id", "id")
                .put("task.id", "task_id")
                .put("scheduledTick", "scheduled_tick")
                .put("status", "status")
                .put("startAt", "start_at")
                .put("endAt", "end_at")
                .put("inlets", "inlets")
                .put("outlets", "outlets")
                .build();
    }

    @Override
    public ResultSetMapper<TaskRun> getMapper() {
        return TaskRunMapper.INSTANCE;
    }

    private static class TaskRunMapper implements ResultSetMapper<TaskRun> {
        public static TaskRunDao.TaskRunMapper INSTANCE = new TaskRunDao.TaskRunMapper();

        @Override
        public TaskRun map(ResultSet rs) throws SQLException {
            return TaskRun.newBuilder()
                    .withId(rs.getLong("id"))
                    .withScheduledTick(new Tick(rs.getString("scheduled_tick")))
                    .withStatus(TaskRunStatus.resolve(rs.getString("status")))
//                    .withInlets(rs.getString("inlets"))
//                    .withOutlets(rs.getString("outlets"))
                    .withStartAt(DateTimeUtils.fromTimestamp(rs.getTimestamp("start_at")))
                    .withEndAt(DateTimeUtils.fromTimestamp(rs.getTimestamp("end_at")))
                    .build();
        }
    }
}
