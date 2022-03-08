package com.miotech.kun.dataquality.web.persistence;

import com.google.common.collect.ImmutableList;
import com.miotech.kun.common.BaseRepository;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataquality.web.model.DataQualityStatus;
import com.miotech.kun.dataquality.web.model.TemplateType;
import com.miotech.kun.dataquality.web.model.entity.CaseRun;
import com.miotech.kun.dataquality.web.model.entity.DataQualityCaseResult;
import com.miotech.kun.dataquality.web.model.entity.DataQualityRule;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/7/16
 */
@Repository
public class DataQualityRepository extends BaseRepository {

    private static final Integer HISTORY_RECORD_LIMIT = 6;

    public static final String CASE_RUN_TABLE = "kun_dq_case_run";

    public static final String CASE_RUN_MODEL = "caserun";

    public static final List<String> caseRunCols = ImmutableList.of("case_run_id", "task_run_id", "case_id", "status", "task_attempt_id");

    public static final List<String> caseCols = ImmutableList.of("id", "name", "description", "task_id", "template_id", "execution_string"
            , "types", "create_user", "create_time", "update_user", "update_time", "primary_dataset_id", "is_blocking");


    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    DatasetRepository datasetRepository;

    public List<Long> getAllTaskId() {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("task_id")
                .from("kun_dq_case")
                .where("task_id is not null")
                .getSQL();

        return jdbcTemplate.queryForList(sql, Long.class);
    }

    public List<Long> getAllCaseId() {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("id")
                .from("kun_dq_case")
                .getSQL();

        return jdbcTemplate.queryForList(sql, Long.class);
    }

    public CaseRun fetchCaseRunByCaseRunId(long caseRunId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select(caseRunCols.toArray(new String[0]))
                .from(CASE_RUN_TABLE)
                .where("case_run_id = ?")
                .limit()
                .asPrepared()
                .getSQL();
        return jdbcTemplate.queryForObject(sql, CaseRunMapper.INSTANCE, caseRunId, 1);
    }

    public long fetchTaskRunIdByCase(long caseRunId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("task_run_id")
                .from(CASE_RUN_TABLE)
                .where("case_run_id = ?")
                .limit()
                .asPrepared()
                .getSQL();
        return jdbcTemplate.queryForObject(sql, Long.class, caseRunId, 1);

    }

    public List<Long> fetchCaseRunsByTaskRunId(long taskRunId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("case_run_id")
                .from(CASE_RUN_TABLE)
                .where("task_run_id = ?")
                .asPrepared()
                .getSQL();
        return jdbcTemplate.queryForList(sql, Long.class, taskRunId);

    }

    public void insertCaseRunWithTaskRun(List<CaseRun> caseRunList) {
        String sql = DefaultSQLBuilder.newBuilder()
                .insert(caseRunCols.toArray(new String[0]))
                .into(CASE_RUN_TABLE)
                .asPrepared()
                .getSQL();

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, caseRunList.get(i).getCaseRunId());
                ps.setLong(2, caseRunList.get(i).getTaskRunId());
                ps.setLong(3, caseRunList.get(i).getCaseId());
                ps.setString(4, DataQualityStatus.CREATED.name());
                ps.setLong(5, caseRunList.get(i).getTaskAttemptId());
            }

            @Override
            public int getBatchSize() {
                return caseRunList.size();
            }
        });
    }

    public void updateCaseRunStatus(long caseRunId, boolean status) {
        DataQualityStatus dataQualityStatus = status ? DataQualityStatus.SUCCESS : DataQualityStatus.FAILED;
        String sql = DefaultSQLBuilder.newBuilder()
                .update(CASE_RUN_TABLE)
                .set("status")
                .where("case_run_id = ?")
                .asPrepared()
                .getSQL();
        jdbcTemplate.update(sql, dataQualityStatus.name(), caseRunId);
    }

    public String fetchCaseRunStatus(long caseRunId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("status")
                .from(CASE_RUN_TABLE)
                .where("case_run_id = ?")
                .limit()
                .asPrepared()
                .getSQL();
        return jdbcTemplate.queryForObject(sql, String.class, caseRunId, 1);

    }

    @Transactional(rollbackFor = Exception.class)
    public void logDataQualityCaseResults(List<DataQualityCaseResult> results) {
        String sql = DefaultSQLBuilder.newBuilder()
                .insert().into("kun_dq_case_task_history")
                .valueSize(8)
                .duplicateKey("task_run_id", "")
                .getSQL();

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                int index = 0;
                ps.setLong(++index, IdGenerator.getInstance().nextId());
                ps.setLong(++index, results.get(i).getTaskId());
                ps.setLong(++index, results.get(i).getTaskRunId());
                ps.setLong(++index, results.get(i).getCaseId());
                ps.setString(++index, results.get(i).getCaseStatus());
                ps.setString(++index, CollectionUtils.isNotEmpty(results.get(i).getErrorReason()) ? results.get(i).getErrorReason().toString() : "");
                ps.setObject(++index, millisToTimestamp(results.get(i).getStartTime()));
                ps.setObject(++index, millisToTimestamp(results.get(i).getEndTime()));
            }

            @Override
            public int getBatchSize() {
                return results.size();
            }

        });
    }

    public List<Long> getWorkflowTasksByDatasetIds(List<Long> datasetIds) {
        if (datasetIds.isEmpty())
            return new ArrayList<>();

        String sql = "select distinct(case_id) from kun_dq_case_associated_dataset where dataset_id in " + toColumnSql(datasetIds.size());

        return jdbcTemplate.query(sql, rs -> {
            List<Long> caseIds = new ArrayList<>();
            while (rs.next()) {
                Long caseId = rs.getLong("case_id");
                caseIds.add(caseId);
            }
            return caseIds;
        }, datasetIds.toArray());
    }


    public static class CaseRunMapper implements RowMapper<CaseRun> {

        public static final CaseRunMapper INSTANCE = new CaseRunMapper();


        @Override
        public CaseRun mapRow(ResultSet rs, int rowNum) throws SQLException {
            CaseRun caseRun = new CaseRun();
            caseRun.setCaseId(rs.getLong("case_id"));
            caseRun.setCaseRunId(rs.getLong("case_run_id"));
            caseRun.setTaskRunId(rs.getLong("task_run_id"));
            caseRun.setTaskAttemptId(rs.getLong("task_attempt_id"));
            caseRun.setStatus(rs.getString("status"));
            return caseRun;
        }
    }

}
