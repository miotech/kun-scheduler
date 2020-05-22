package com.miotech.kun.common.dao;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.miotech.kun.workflow.core.model.taskrun.TaskAttempt;
import com.miotech.kun.workflow.core.model.taskrun.TaskRunStatus;
import com.miotech.kun.workflow.db.DatabaseOperator;
import com.miotech.kun.workflow.db.ResultSetMapper;
import com.miotech.kun.workflow.utils.DateTimeUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Singleton
public class TaskAttemptDao extends AbstractDao<TaskAttempt> {

    @Inject
    private DatabaseOperator dbOperator;

    @Override
    public String getTableName() {
        return "kun_wf_task_attempt";
    }

    @Override
    public Map<String, String> getTableColumnMapping() {
        return ImmutableMap.<String, String>builder()
                .put("id", "id")
                .put("taskRun.id", "task_run_id")
                .put("attempt", "attempt")
                .put("status", "status")
                .put("startAt", "start_at")
                .put("endAt", "end_at")
                .put("logPath", "log_path")
                .build();
    }

    public List<TaskAttempt> findByTaskRunID(Long taskRunId) {
        String sql = String.format("SELECT %s FROM %s WHERE task_run_id = ? ",
                String.join(",", getTableColumns()),
                getTableName());
        return dbOperator.fetchAll(sql, getMapper(), taskRunId);
    }

    @Override
    public ResultSetMapper<TaskAttempt> getMapper() {
        return TaskAttemptMapper.INSTANCE;
    }

    private static class TaskAttemptMapper implements ResultSetMapper<TaskAttempt> {
        public static TaskAttemptMapper INSTANCE = new TaskAttemptDao.TaskAttemptMapper();

        @Override
        public TaskAttempt map(ResultSet rs) throws SQLException {
            return TaskAttempt.newBuilder()
                    .withId(rs.getLong("id"))
                    .withStatus(TaskRunStatus.resolve(rs.getString("status")))
                    .withAttempt(rs.getInt("attempt"))
                    .withStartAt(DateTimeUtils.fromTimestamp(rs.getTimestamp("start_at")))
                    .withEndAt(DateTimeUtils.fromTimestamp(rs.getTimestamp("end_at")))
                    .build();
        }
    }
}