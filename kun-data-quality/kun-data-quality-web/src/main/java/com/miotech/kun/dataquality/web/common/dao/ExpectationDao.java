package com.miotech.kun.dataquality.web.common.dao;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.db.sql.SQLBuilder;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataquality.core.assertion.Assertion;
import com.miotech.kun.dataquality.core.expectation.Dataset;
import com.miotech.kun.dataquality.core.expectation.Expectation;
import com.miotech.kun.dataquality.core.expectation.ExpectationMethod;
import com.miotech.kun.dataquality.core.metrics.Metrics;
import com.miotech.kun.dataquality.core.expectation.CaseType;
import com.miotech.kun.dataquality.web.model.DataQualityStatus;
import com.miotech.kun.dataquality.web.model.bo.ExpectationsRequest;
import com.miotech.kun.dataquality.web.model.entity.*;
import com.miotech.kun.dataquality.web.persistence.DatasetRepository;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class ExpectationDao {

    private static final String TABLE_NAME = "kun_dq_expectation";
    public static final String CASE_RUN_TABLE = "kun_dq_case_run";
    public static final String CASE_RUN_MODEL = "caserun";
    private static final List<String> COLUMNS = ImmutableList.of("id", "name", "types", "description", "method", "metrics_config", "assertion_config", "trigger",
            "dataset_gid", "task_id", "case_type", "create_time", "update_time", "create_user", "update_user");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    DatasetRepository datasetRepository;

    public void create(Expectation spec) {
        String sql = DefaultSQLBuilder.newBuilder()
                .insert(COLUMNS.toArray(new String[0]))
                .into(TABLE_NAME)
                .asPrepared()
                .getSQL();
        jdbcTemplate.update(sql,
                spec.getExpectationId(),
                spec.getName(),
                StringUtils.join(spec.getTypes(), ","),
                spec.getDescription(),
                JSONUtils.toJsonString(spec.getMethod()),
                JSONUtils.toJsonString(spec.getMetrics()),
                JSONUtils.toJsonString(spec.getAssertion()),
                spec.getTrigger().name(),
                spec.getDataset().getGid(),
                spec.getTaskId(),
                spec.getCaseType().name(),
                spec.getCreateTime(),
                spec.getUpdateTime(),
                spec.getCreateUser(),
                spec.getUpdateUser());
    }

    public Expectation fetchById(Long id) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select(COLUMNS.toArray(new String[0]))
                .from(TABLE_NAME)
                .where("id = ?")
                .getSQL();

        try {
            return jdbcTemplate.queryForObject(sql, ExpectationRowMapper.INSTANCE, id);
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            return null;
        }
    }

    public void deleteById(Long id) {
        String sql = DefaultSQLBuilder.newBuilder()
                .delete()
                .from(TABLE_NAME)
                .where("id = ?")
                .getSQL();
        jdbcTemplate.update(sql, id);
    }

    public void updateById(Long id, Expectation expectation) {
        String kdcSql = DefaultSQLBuilder.newBuilder()
                .update(TABLE_NAME)
                .set("name",
                        "types",
                        "description",
                        "method",
                        "metrics_config",
                        "assertion_config",
                        "trigger",
                        "case_type",
                        "update_time",
                        "update_user")
                .asPrepared()
                .where("id = ?")
                .getSQL();
        jdbcTemplate.update(kdcSql,
                expectation.getName(),
                StringUtils.join(expectation.getTypes(), ","),
                expectation.getDescription(),
                JSONUtils.toJsonString(expectation.getMethod()),
                JSONUtils.toJsonString(expectation.getMetrics()),
                JSONUtils.toJsonString(expectation.getAssertion()),
                expectation.getTrigger().name(),
                expectation.getCaseType().name(),
                expectation.getUpdateTime(),
                expectation.getUpdateUser(),
                id);


    }

    public void updateTaskId(Long expectationId, Long taskId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .update(TABLE_NAME)
                .set("task_id")
                .asPrepared()
                .where("id = ?")
                .getSQL();

        jdbcTemplate.update(sql, taskId, expectationId);
    }

    public List<DataQualityHistoryRecords> getHistoryOfTheLastNTimes(List<Long> expectationIds, int n) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(expectationIds));
        Preconditions.checkArgument(n > 0);
        String sql = "select expectation_id, continuous_failing_count, update_time, execution_result, assertion_result " +
                "from (select *, ROW_NUMBER() OVER (PARTITION BY expectation_id ORDER BY update_time desc) AS row_number " +
                "from kun_dq_expectation_run " +
                "where expectation_id in " + "(" + expectationIds.stream().map(id -> "?")
                .collect(Collectors.joining(", ")) + ")" + ") kder " +
                "where kder.row_number <= " + n;

        return jdbcTemplate.query(sql, rs -> {
            Map<Long, DataQualityHistoryRecords> recordsMap = new HashMap<>();
            while (rs.next()) {
                Long caseId = rs.getLong("expectation_id");
                DataQualityHistoryRecords records = recordsMap.computeIfAbsent(caseId, key -> new DataQualityHistoryRecords());
                List<DataQualityHistory> historyList = records.getHistoryList();
                records.setCaseId(caseId);
                DataQualityHistory history = new DataQualityHistory();
                long continuousFailingCount = rs.getLong("continuous_failing_count");
                if (continuousFailingCount == 0) {
                    history.setStatus(DataQualityStatus.SUCCESS.name());
                } else {
                    history.setStatus(DataQualityStatus.FAILED.name());
                }
                history.setContinuousFailingCount(continuousFailingCount);
                history.setUpdateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp("update_time")));
                history.setErrorReason(rs.getString("execution_result"));
                Type type = new TypeToken<List<DataQualityRule>>() {
                }.getType();
                history.setRuleRecords(com.miotech.kun.common.utils.JSONUtils.toJavaObject(rs.getString("assertion_result"), type));
                historyList.add(history);
            }
            return Lists.newArrayList(recordsMap.values());
        }, expectationIds.toArray());
    }

    public void deleteAllRelatedDataset(Long id) {
        String deleteSql = DefaultSQLBuilder.newBuilder()
                .delete()
                .from("kun_dq_case_associated_dataset")
                .where("case_id = ?")
                .getSQL();
        jdbcTemplate.update(deleteSql, id);
    }

    public void createRelatedDataset(Long id, List<Long> datasetIds) {
        if (CollectionUtils.isNotEmpty(datasetIds)) {
            String insertSql = DefaultSQLBuilder.newBuilder()
                    .insert()
                    .into("kun_dq_case_associated_dataset")
                    .valueSize(3)
                    .getSQL();
            for (Long datasetId : datasetIds) {
                jdbcTemplate.update(insertSql,
                        IdGenerator.getInstance().nextId(),
                        id,
                        datasetId);
            }
        }
    }

    public List<DatasetBasic> getRelatedDatasets(Long expectationId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("dataset_id")
                .from("kun_dq_case_associated_dataset")
                .where("case_id = ?")
                .getSQL();

        List<Long> datasetIds = jdbcTemplate.query(sql, rs -> {
            List<Long> ids = new ArrayList<>();
            while (rs.next()) {
                ids.add(rs.getLong("dataset_id"));
            }
            return ids;
        }, expectationId);

        sql = DefaultSQLBuilder.newBuilder()
                .select("dataset_gid")
                .from(TABLE_NAME)
                .where("id = ?")
                .getSQL();
        Long primaryDatasetId = jdbcTemplate.queryForObject(sql, Long.class, expectationId);

        List<DatasetBasic> datasetBasics = new ArrayList<>();
        for (Long datasetId : datasetIds) {
            DatasetBasic datasetBasic = datasetRepository.findBasic(datasetId);
            if (datasetBasic.getGid().equals(primaryDatasetId)) {
                datasetBasic.setIsPrimary(true);
            }
            datasetBasics.add(datasetBasic);
        }
        return datasetBasics;
    }

    public ExpectationBasic fetchCaseBasicByTaskId(Long taskId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("id", "name", "task_id", "case_type")
                .from(TABLE_NAME)
                .where("task_id = ?")
                .getSQL();

        return jdbcTemplate.query(sql, rs -> {
            ExpectationBasic expectationBasic = new ExpectationBasic();
            if (rs.next()) {
                expectationBasic.setId(rs.getLong("id"));
                expectationBasic.setName(rs.getString("name"));
                expectationBasic.setTaskId(rs.getLong("task_id"));
                expectationBasic.setCaseType(CaseType.valueOf(rs.getString("case_type")));
            } else {
                return null;
            }
            return expectationBasic;
        }, taskId);
    }

    public List<CaseResult> fetchValidateResult(Long taskAttemptId, List<CaseType> caseTypeList) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("status","case_type")
                .from(CASE_RUN_TABLE, CASE_RUN_MODEL)
                .join("inner", "kun_dq_expectation", "kde")
                .on("kde.id = " + CASE_RUN_MODEL + ".case_id")
                .where(CASE_RUN_MODEL + ".task_attempt_id = ? and kde.case_type in ( " + StringUtils.repeat("?", ",", caseTypeList.size()) + " )" +
                        "and kde.dataset_gid = " + CASE_RUN_MODEL + ".validate_dataset_id")
                .asPrepared()
                .getSQL();
        List<Object> params = new ArrayList<>();
        params.add(taskAttemptId);
        params.addAll(caseTypeList.stream().map(Enum::name).collect(Collectors.toList()));
        return jdbcTemplate.query(sql, CheckResultMapper.INSTANCE, params.toArray());
    }

    public ExpectationBasics getExpectationBasics(ExpectationsRequest request) {
        SQLBuilder getSqlBuilder = DefaultSQLBuilder.newBuilder()
                .select("kde.id as case_id",
                        "kde.name as case_name",
                        "kde.update_user as case_update_user",
                        "kde.types as case_types",
                        "kde.description as case_desc",
                        "kde.task_id as case_task_id",
                        "kde.case_type as case_case_type",
                        "kde.create_time as create_time",
                        "kde.update_time as update_time",
                        "kde.dataset_gid as primary_dataset_id")
                .from("kun_dq_expectation kde")
                .join("inner", "kun_dq_case_associated_dataset", "kdcad").on("kde.id = kdcad.case_id")
                .where("kdcad.dataset_id = ?");

        String countSql = DefaultSQLBuilder.newBuilder()
                .select("count(1) as total_count")
                .from("(" + getSqlBuilder.getSQL() + ") temp")
                .getSQL();
        Integer totalCount = jdbcTemplate.queryForObject(countSql, Integer.class, request.getGid());

        int offset = (request.getPageNumber() - 1) * request.getPageSize();
        getSqlBuilder
                .orderBy("kde.create_time desc")
                .offset(offset)
                .limit(request.getPageSize());
        return jdbcTemplate.query(getSqlBuilder.getSQL(), rs -> {
            ExpectationBasics expectationBasics = new ExpectationBasics();
            while (rs.next()) {
                ExpectationBasic expectationBasic = new ExpectationBasic();
                expectationBasic.setId(rs.getLong("case_id"));
                expectationBasic.setName(rs.getString("case_name"));
                expectationBasic.setUpdater(rs.getString("case_update_user"));
                expectationBasic.setTypes(resolveDqCaseTypes(rs.getString("case_types")));
                expectationBasic.setDescription(rs.getString("case_desc"));
                expectationBasic.setTaskId(rs.getLong("case_task_id"));
                expectationBasic.setCaseType(CaseType.valueOf(rs.getString("case_case_type")));
                expectationBasic.setCreateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp("create_time")));
                expectationBasic.setUpdateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp("update_time")));
                Long primaryDatasetId = rs.getLong("primary_dataset_id");
                expectationBasic.setIsPrimary(request.getGid().equals(primaryDatasetId));
                expectationBasics.add(expectationBasic);
            }
            expectationBasics.setPageNumber(request.getPageNumber());
            expectationBasics.setPageSize(request.getPageSize());
            expectationBasics.setTotalCount(totalCount);
            return expectationBasics;
        }, request.getGid());
    }

    private static List<String> resolveDqCaseTypes(String types) {
        if (StringUtils.isNotEmpty(types)) {
            return Arrays.asList(types.split(","));
        }

        return null;
    }

    public Expectation fetchByTaskId(Long taskId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select(COLUMNS.toArray(new String[0]))
                .from(TABLE_NAME)
                .where("task_id = ?")
                .getSQL();

        try {
            return jdbcTemplate.queryForObject(sql, ExpectationRowMapper.INSTANCE, taskId);
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            return null;
        }
    }

    public static class ExpectationRowMapper implements RowMapper<Expectation> {
        public static final ExpectationDao.ExpectationRowMapper INSTANCE = new ExpectationDao.ExpectationRowMapper();

        @Override
        public Expectation mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Expectation.newBuilder()
                    .withExpectationId(rs.getLong("id"))
                    .withName(rs.getString("name"))
                    .withTypes(resolveDqCaseTypes(rs.getString("types")))
                    .withDescription(rs.getString("description"))
                    .withMethod(JSONUtils.jsonToObject(rs.getString("method"), ExpectationMethod.class))
                    .withMetrics(JSONUtils.jsonToObject(rs.getString("metrics_config"), Metrics.class))
                    .withAssertion(JSONUtils.jsonToObject(rs.getString("assertion_config"), Assertion.class))
                    .withTrigger(Expectation.ExpectationTrigger.valueOf(rs.getString("trigger")))
                    .withDataset(Dataset.builder().gid(rs.getLong("dataset_gid")).build())
                    .withTaskId(rs.getLong("task_id"))
                    .withCaseType(CaseType.valueOf(rs.getString("case_type")))
                    .withCreateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp("create_time")))
                    .withUpdateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp("update_time")))
                    .withCreateUser(rs.getString("create_user"))
                    .withUpdateUser(rs.getString("update_user"))
                    .build();
        }
    }

    public static class CheckResultMapper implements RowMapper<CaseResult>{

        public static final CheckResultMapper INSTANCE = new CheckResultMapper();

        @Override
        public CaseResult mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new CaseResult(DataQualityStatus.valueOf(rs.getString("status")),CaseType.valueOf(rs.getString("case_type")));
        }
    }

}
