package com.miotech.kun.dataquality.web.persistence;

import com.google.common.collect.ImmutableList;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.dataquality.web.model.AbnormalDataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class AbnormalDatasetRepository {

    public static final String TABLE_NAME = "kun_dq_abnormal_dataset";
    public static final List<String> TABLE_COLUMNS = ImmutableList.of("id", "dataset_gid", "task_run_id", "task_id", "task_name", "create_time", "update_time", "schedule_at", "status");
    public static final List<String> INSERT_COLUMNS = ImmutableList.of("dataset_gid", "task_run_id", "task_id", "task_name", "create_time", "update_time", "schedule_at");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void create(AbnormalDataset abnormalDataset) {
        String sql = DefaultSQLBuilder.newBuilder()
                .insert(INSERT_COLUMNS.toArray(new String[0]))
                .into(TABLE_NAME)
                .asPrepared()
                .getSQL();

        jdbcTemplate.update(sql,
                abnormalDataset.getDatasetGid(),
                abnormalDataset.getTaskRunId(),
                abnormalDataset.getTaskId(),
                abnormalDataset.getTaskName(),
                abnormalDataset.getCreateTime(),
                abnormalDataset.getUpdateTime(),
                abnormalDataset.getScheduleAt());
    }

    public boolean updateStatus(Long id, String status) {
        String sql = DefaultSQLBuilder.newBuilder()
                .update(TABLE_NAME)
                .set("status", "update_time")
                .where("id = ?")
                .asPrepared()
                .getSQL();
        int updatedRows = jdbcTemplate.update(sql, status, DateTimeUtils.now(), id);
        return updatedRows == 1;
    }

    public void updateStatusByTaskRunId(Long taskRunId, String status) {
        String sql = DefaultSQLBuilder.newBuilder()
                .update(TABLE_NAME)
                .set("status", "update_time")
                .where("task_run_id = ?")
                .asPrepared()
                .getSQL();
        jdbcTemplate.update(sql, status, DateTimeUtils.now(), taskRunId);
    }

    public List<AbnormalDataset> fetchByScheduleAtAndStatusIsNull(String scheduleAt) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select(TABLE_COLUMNS.toArray(new String[0]))
                .from(TABLE_NAME)
                .where("schedule_at = ? and status is null")
                .getSQL();

        return jdbcTemplate.query(sql, AbnormalDatasetRowMapper.INSTANCE, scheduleAt);
    }

    public List<AbnormalDataset> fetchAll() {
        String sql = DefaultSQLBuilder.newBuilder()
                .select(TABLE_COLUMNS.toArray(new String[0]))
                .from(TABLE_NAME)
                .getSQL();
        return jdbcTemplate.query(sql, AbnormalDatasetRowMapper.INSTANCE);
    }

    public static class AbnormalDatasetRowMapper implements RowMapper<AbnormalDataset> {
        public static final AbnormalDatasetRowMapper INSTANCE = new AbnormalDatasetRowMapper();

        @Override
        public AbnormalDataset mapRow(ResultSet rs, int rowNum) throws SQLException {
            return AbnormalDataset.newBuilder()
                    .withId(rs.getLong("id"))
                    .withDatasetGid(rs.getLong("dataset_gid"))
                    .withTaskRunId(rs.getLong("task_run_id"))
                    .withTaskId(rs.getLong("task_id"))
                    .withTaskName(rs.getString("task_name"))
                    .withCreateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp("create_time")))
                    .withUpdateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp("update_time")))
                    .withScheduleAt(rs.getString("schedule_at"))
                    .withStatus(rs.getString("status"))
                    .build();
        }
    }

}
