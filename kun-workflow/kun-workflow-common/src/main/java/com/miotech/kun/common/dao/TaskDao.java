package com.miotech.kun.common.dao;

import com.miotech.kun.workflow.core.model.common.Tick;
import com.miotech.kun.workflow.core.model.task.Task;
import com.miotech.kun.workflow.db.DatabaseOperator;
import com.miotech.kun.workflow.db.ResultSetMapper;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Singleton
public class TaskDao {
    private final DatabaseOperator dbOperator;

    private final String TASK_TABLE_NAME = "kun_wf_tasks";

    @Inject
    public TaskDao(DatabaseOperator dbOperator) {
        this.dbOperator = dbOperator;
    }

    public List<Task> findByID(long taskId) {
        String sql = String.format("SELECT * from %s WHERE id = ?", TASK_TABLE_NAME);
        return dbOperator.fetchAll(sql, TaskMapper.INSTANCE, taskId);
    }

    public List<Task> fetchScheduledTaskAtTick(Tick tick) {
        String sql = "select * from kun_wf_tick_task_mapping where scheduled_tick = ?";
        return dbOperator.fetchAll(sql, TaskMapper.INSTANCE, tick.toString());
    }

//    public Task insertOneTask(Task task) {
//        String updateTaskSQL = "update kun_wf_tasks where id = ? ";
//        String updateTickTaskSQL = "";
//        dbOperator.transaction(() -> {
//            dbOperator.update(updateTaskSQL);
//        });
//    ˆ}

    private static class TaskMapper implements ResultSetMapper<Task> {
        public static TaskMapper INSTANCE = new TaskMapper();

        @Override
        public Task map(ResultSet rs) throws SQLException {
            return Task.newBuilder()
                    .id(rs.getString("id"))
                    .name(rs.getString("name"))
                    .description(rs.getString("description"))
                    .operatorName(rs.getString("operatorName"))
                    .build();
        }
    }
}
