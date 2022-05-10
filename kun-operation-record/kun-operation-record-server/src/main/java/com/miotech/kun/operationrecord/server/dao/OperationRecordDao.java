package com.miotech.kun.operationrecord.server.dao;

import com.google.common.collect.ImmutableList;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.operationrecord.common.event.OperationRecordEvent;
import com.miotech.kun.operationrecord.common.model.OperationRecord;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class OperationRecordDao {

    private static final String OPERATION_RECORD_TABLE_NAME = "kun_operation_record";

    private static final List<String> operationRecordCols = ImmutableList.of("id", "operator", "type", "event", "status", "create_time", "update_time");
    private static final List<String> operationRecordCreateCols = ImmutableList.of("operator", "type", "event", "status", "create_time", "update_time");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void create(OperationRecordEvent operationEvent) {
        String sql = DefaultSQLBuilder.newBuilder()
                .insert(operationRecordCreateCols.toArray(new String[0]))
                .into(OPERATION_RECORD_TABLE_NAME)
                .asPrepared()
                .getSQL();

        jdbcTemplate.update(sql,
                operationEvent.getOperator(),
                operationEvent.getOperationType().name(),
                JSONUtils.toJsonString(operationEvent.getEvent()),
                operationEvent.getStatus(),
                DateTimeUtils.fromTimestamp(operationEvent.getTimestamp()),
                DateTimeUtils.now());
    }

    public List<OperationRecord> findByOperator(String operator) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select(operationRecordCols.toArray(new String[0]))
                .from(OPERATION_RECORD_TABLE_NAME)
                .where("operator = ?")
                .orderBy("id desc")
                .getSQL();

        return jdbcTemplate.query(sql, OperationRecordMapper.INSTANCE, operator);
    }

    public static class OperationRecordMapper implements RowMapper<OperationRecord> {
        public static final OperationRecordMapper INSTANCE = new OperationRecordMapper();

        @Override
        public OperationRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            return OperationRecord.builder()
                    .id(rs.getLong("id"))
                    .operator(rs.getString("operator"))
                    .type(rs.getString("type"))
                    .event(rs.getString("event"))
                    .status(rs.getString("status"))
                    .createTime(DateTimeUtils.fromTimestamp(rs.getTimestamp("create_time")))
                    .updateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp("update_time")))
                    .build();
        }
    }

}
