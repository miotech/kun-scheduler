package com.miotech.kun.workflow.operator.client;

import com.google.common.collect.ImmutableList;
import com.miotech.kun.common.utils.DateUtils;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.query.datasource.MetadataDataSource;
import com.miotech.kun.commons.query.service.ConfigService;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataquality.core.Dataset;
import com.miotech.kun.dataquality.core.ExpectationMethod;
import com.miotech.kun.dataquality.core.ExpectationSpec;
import com.miotech.kun.dataquality.core.ValidationResult;
import com.miotech.kun.metadata.core.model.datasource.DataSource;
import com.miotech.kun.workflow.operator.DataQualityConfiguration;
import com.miotech.kun.workflow.operator.model.*;
import com.miotech.kun.workflow.operator.utils.DataSourceClient;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.postgresql.util.PGobject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/7/14
 */
public class DataQualityClient {

    private static final String EXPECTATION_TABLE_NAME = "kun_dq_expectation";
    private static final String EXPECTATION_RUN_TABLE_NAME = "kun_dq_expectation_run";
    private static final List<String> EXPECTATION_COLUMNS = ImmutableList.of("id", "name", "types", "description", "method", "trigger",
            "dataset_gid", "task_id", "is_blocking", "create_time", "update_time", "create_user", "update_user");
    private static final List<String> EXPECTATION_RUN_INSERT_COLUMNS = ImmutableList.of("expectation_id", "passed", "execution_result",
            "assertion_result", "continuous_failing_count", "update_time");


    private DatabaseOperator databaseOperator;

    private DataQualityClient() {
        databaseOperator = new DatabaseOperator(MetadataDataSource.getInstance().getMetadataDataSource());
    }

    public ExpectationSpec getExpectation(Long caseId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select(EXPECTATION_COLUMNS.toArray(new String[0]))
                .from(EXPECTATION_TABLE_NAME)
                .where("id = ?")
                .getSQL();

        return databaseOperator.query(sql, rs -> {
            ExpectationSpec.Builder builder = ExpectationSpec.newBuilder();
            if (rs.next()) {
                Long datasetGid = rs.getLong("dataset_gid");
                Long dataSourceId = fetchDataSourceIdByGid(datasetGid);
                builder
                        .withExpectationId(rs.getLong("id"))
                        .withName(rs.getString("name"))
                        .withDescription(rs.getString("description"))
                        .withMethod(com.miotech.kun.workflow.utils.JSONUtils.jsonToObject(rs.getString("method"), ExpectationMethod.class))
                        .withTrigger(ExpectationSpec.ExpectationTrigger.valueOf(rs.getString("trigger")))
                        .withTaskId(rs.getLong("task_id"))
                        .withIsBlocking(rs.getBoolean("is_blocking"))
                        .withCreateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp("create_time")))
                        .withUpdateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp("update_time")))
                        .withCreateUser(rs.getString("create_user"))
                        .withUpdateUser(rs.getString("update_user"));

                DataSourceClient client = new DataSourceClient(ConfigService.getInstance().getProperties().get(DataQualityConfiguration.INFRA_BASE_URL));
                DataSource dataSourceById = client.getDataSourceById(dataSourceId);
                builder.withDataset(Dataset.builder().gid(datasetGid).dataSource(dataSourceById).build());
            }
            return builder.build();
        }, caseId);
    }

    private Long fetchDataSourceIdByGid(Long datasetGid) {
        String sql = "select datasource_id from kun_mt_dataset where gid = ?";
        return databaseOperator.fetchOne(sql, rs -> rs.getLong("datasource_id"), datasetGid);
    }

    public void record(ValidationResult vr, Long caseRunId) {
        long failedCount = 0;
        if (!vr.isPassed()) {
            failedCount = getLatestFailingCount(vr.getExpectationId()) + 1;
        }

        String sql = DefaultSQLBuilder.newBuilder()
                .insert(EXPECTATION_RUN_INSERT_COLUMNS.toArray(new String[0]))
                .into(EXPECTATION_RUN_TABLE_NAME)
                .asPrepared()
                .getSQL();

        databaseOperator.create(sql,
                vr.getExpectationId(),
                vr.isPassed(),
                vr.getExecutionResult(),
                transferRuleRecordsToPGObject(vr.getAssertionResults()),
                failedCount,
                vr.getUpdateTime()
                );

        String updateStatusSql = DefaultSQLBuilder.newBuilder()
                .update("kun_dq_case_run")
                .set("status")
                .where("case_run_id = ?")
                .asPrepared()
                .getSQL();
        String status = vr.isPassed() ? "SUCCESS" : "FAILED";
        databaseOperator.update(updateStatusSql, status, caseRunId);
    }

    private static class SingletonHolder {
        private static DataQualityClient instance = new DataQualityClient();
    }

    public static DataQualityClient getInstance() {
        return DataQualityClient.SingletonHolder.instance;
    }

    public DataQualityCase getCase(Long caseId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("kdc.id as case_id",
                        "kdc.name as case_name",
                        "kdc.description as case_description",
                        "kdc.template_id as case_temp_id",
                        "kdc.execution_string as custom_string",
                        "kdcdt.execution_string as temp_string",
                        "kdct.type as temp_type")
                .from("kun_dq_case kdc")
                .join("left", "kun_dq_case_datasource_template", "kdcdt").on("kdcdt.id = kdc.template_id")
                .join("left", "kun_dq_case_template", "kdct").on("kdct.id = kdcdt.template_id")
                .where("kdc.id = ?")
                .getSQL();

        return databaseOperator.query(sql, rs -> {
            DataQualityCase dqCase = new DataQualityCase();
            if (rs.next()) {
                Long tempId = rs.getLong("case_temp_id");
                if (tempId.equals(0L)) {
                    dqCase.setDimension(TemplateType.CUSTOMIZE);
                    dqCase.setExecutionString(rs.getString("custom_string"));
                } else {
                    dqCase.setDimension(TemplateType.valueOf(rs.getString("temp_type")));
                    dqCase.setExecutionString(rs.getString("temp_string"));
                }
                dqCase.setRules(getRulesByCaseId(caseId));
                dqCase.setDatasetIds(getDatasetIdsByCaseId(caseId));
            }

            return dqCase;
        }, caseId);
    }

    private List<DataQualityRule> getRulesByCaseId(Long caseId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("field",
                        "operator",
                        "expected_value_type",
                        "expected_value")
                .from("kun_dq_case_rules")
                .where("case_id = ?")
                .getSQL();

        return databaseOperator.query(sql, rs -> {
            List<DataQualityRule> rules = new ArrayList<>();
            while (rs.next()) {
                DataQualityRule rule = new DataQualityRule();
                rule.setField(rs.getString("field"));
                rule.setOperator(rs.getString("operator"));
                rule.setExpectedType(rs.getString("expected_value_type"));
                rule.setExpectedValue(rs.getString("expected_value"));
                rules.add(rule);
            }
            return rules;
        }, caseId);
    }

    public List<Long> getDatasetIdsByCaseId(Long caseId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("dataset_id")
                .from("kun_dq_case_associated_dataset")
                .where("case_id = ?")
                .getSQL();

        return databaseOperator.query(sql, rs -> {
            List<Long> ids = new ArrayList<>();
            while (rs.next()) {
                ids.add(rs.getLong("dataset_id"));
            }
            return ids;
        }, caseId);
    }

    public List<Long> getDatasetFieldIdsByCaseId(Long caseId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("dataset_field_id")
                .from("kun_dq_case_associated_dataset_field")
                .where("case_id = ?")
                .getSQL();

        return databaseOperator.query(sql, rs -> {
            List<Long> fieldIds = new ArrayList<>();
            while (rs.next()) {
                fieldIds.add(rs.getLong("dataset_field_id"));
            }
            return fieldIds;
        }, caseId);
    }

    public void recordCaseMetrics(DataQualityCaseMetrics metrics, Long caseRunId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .insert()
                .into("kun_dq_case_metrics")
                .valueSize(6)
                .getSQL();

        long failedCount = 0;
        if (CaseStatus.FAILED == metrics.getCaseStatus()) {
            long latestFailingCount = getLatestFailingCount(metrics.getCaseId());
            failedCount = latestFailingCount + 1;
        }

        databaseOperator.update(sql, IdGenerator.getInstance().nextId(),
                metrics.getErrorReason(),
                failedCount,
                DateUtils.millisToLocalDateTime(System.currentTimeMillis()),
                transferRuleRecordsToPGObject(metrics.getRuleRecords()),
                metrics.getCaseId());

        String updateStatusSql = DefaultSQLBuilder.newBuilder()
                .update("kun_dq_case_run")
                .set("status")
                .where("case_run_id = ?")
                .asPrepared()
                .getSQL();
        databaseOperator.update(updateStatusSql, metrics.getCaseStatus().name(), caseRunId);
    }

    private Long getLatestFailingCount(Long expectationId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("continuous_failing_count")
                .from(EXPECTATION_RUN_TABLE_NAME)
                .where("expectation_id = ?")
                .orderBy("update_time desc")
                .limit(1)
                .getSQL();

        Long latestFailingCount = databaseOperator.fetchOne(sql, rs -> rs.getLong("continuous_failing_count"), expectationId);
        if (latestFailingCount == null) {
            return 0L;
        }
        return latestFailingCount;
    }

    private PGobject transferRuleRecordsToPGObject(Object obj) {
        PGobject jsonObject = new PGobject();
        jsonObject.setType("jsonb");
        try {
            jsonObject.setValue(JSONUtils.toJsonString(obj));
        } catch (SQLException e) {
            throw ExceptionUtils.wrapIfChecked(new RuntimeException(e));
        }
        return jsonObject;
    }
}
