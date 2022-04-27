package com.miotech.kun.dataquality.web.common.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataquality.core.expectation.AssertionResult;
import com.miotech.kun.dataquality.core.expectation.ValidationResult;
import com.miotech.kun.workflow.utils.DateTimeUtils;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class ExpectationRunDao {

    private static final String TABLE_NAME = "kun_dq_expectation_run";
    private static final List<String> COLUMNS = ImmutableList.of("id", "expectation_id", "passed", "execution_result", "assertion_result", "continuous_failing_count", "update_time");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void create(ValidationResult validationResult) {
        String sql = DefaultSQLBuilder.newBuilder()
                .insert(COLUMNS.toArray(new String[0]))
                .into(TABLE_NAME)
                .asPrepared()
                .getSQL();

        long failedCount = 0;
        if (!validationResult.isPassed()) {
            long latestFailingCount = getLatestFailingCount(validationResult.getExpectationId());
            failedCount = latestFailingCount + 1;
        }

        jdbcTemplate.update(sql,
                IdGenerator.getInstance().nextId(),
                validationResult.getExpectationId(),
                validationResult.isPassed(),
                validationResult.getExecutionResult(),
                JSONUtils.toJsonString(validationResult.getAssertionResults()),
                failedCount,
                validationResult.getUpdateTime());
    }



    public ValidationResult fetchByExpectationId(Long expectationId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select(COLUMNS.toArray(new String[0]))
                .from(TABLE_NAME)
                .where("expectation_id = ?")
                .orderBy("id desc")
                .limit(1)
                .getSQL();
        return jdbcTemplate.queryForObject(sql, ExpectationRunRowMapper.INSTANCE, expectationId);
    }

    public List<ValidationResult> fetchByUpdateTimeFromAndPassed(OffsetDateTime updateTimeFrom, boolean passed) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select(COLUMNS.toArray(new String[0]))
                .from(TABLE_NAME)
                .where("update_time >= ? and passed = ?")
                .orderBy("id desc")
                .getSQL();
        return jdbcTemplate.query(sql, ExpectationRunRowMapper.INSTANCE, updateTimeFrom, passed);
    }

    private static class ExpectationRunRowMapper implements RowMapper<ValidationResult> {
        public static final ExpectationRunDao.ExpectationRunRowMapper INSTANCE = new ExpectationRunDao.ExpectationRunRowMapper();

        @Override
        public ValidationResult mapRow(ResultSet rs, int rowNum) throws SQLException {
            return ValidationResult.newBuilder()
                    .withExpectationId(rs.getLong("expectation_id"))
                    .withPassed(rs.getBoolean("passed"))
                    .withExecutionResult(rs.getString("execution_result"))
                    .withAssertionResults(JSONUtils.jsonToObject(rs.getString("assertion_result"),
                            new TypeReference<List<AssertionResult>>() {
                            }))
                    .withContinuousFailingCount(rs.getLong("continuous_failing_count"))
                    .withUpdateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp("update_time")))
                    .build();
        }
    }

    public long getLatestFailingCount(Long expectationId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("continuous_failing_count")
                .from(TABLE_NAME)
                .where("expectation_id = ?")
                .orderBy("id desc")
                .limit(1)
                .getSQL();

        try {
            return jdbcTemplate.queryForObject(sql, Long.class, expectationId);
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            return 0L;
        }
    }

}
