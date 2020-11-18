package com.miotech.kun.workflow.operator.client;

import com.miotech.kun.common.utils.DateUtils;
import com.miotech.kun.common.utils.JSONUtils;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.query.datasource.MetadataDataSource;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.workflow.operator.model.*;
import org.postgresql.util.PGobject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/7/14
 */
public class DataQualityClient {

    private DatabaseOperator databaseOperator;

    private DataQualityClient() {
        databaseOperator = new DatabaseOperator(MetadataDataSource.getInstance().getMetadataDataSource());
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

    public void recordCaseMetrics(DataQualityCaseMetrics metrics) {
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
                transferRuleRecordsToPGobject(metrics.getRuleRecords()),
                metrics.getCaseId());
    }

    private Long getLatestFailingCount(Long caseId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("continuous_failing_count")
                .from("kun_dq_case_metrics")
                .where("case_id = ?")
                .orderBy("update_time desc")
                .limit(1)
                .getSQL();

        Long latestFailingCount = databaseOperator.fetchOne(sql, rs -> rs.getLong("continuous_failing_count"), caseId);
        if (latestFailingCount == null) {
            return 0L;
        }
        return latestFailingCount;
    }

    private PGobject transferRuleRecordsToPGobject(List<DataQualityRule> ruleRecords) {
        PGobject jsonObject = new PGobject();
        jsonObject.setType("jsonb");
        try {
            jsonObject.setValue(JSONUtils.toJsonString(ruleRecords));
        } catch (SQLException e) {
            throw ExceptionUtils.wrapIfChecked(new RuntimeException(e));
        }
        return jsonObject;
    }
}
