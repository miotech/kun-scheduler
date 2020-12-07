package com.miotech.kun.datadashboard.persistence;

import com.google.gson.reflect.TypeToken;
import com.miotech.kun.common.BaseRepository;
import com.miotech.kun.common.utils.JSONUtils;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.datadashboard.model.bo.TestCasesRequest;
import com.miotech.kun.datadashboard.model.constant.TestCaseStatus;
import com.miotech.kun.datadashboard.model.entity.DataQualityCase;
import com.miotech.kun.datadashboard.model.entity.DataQualityCases;
import com.miotech.kun.datadashboard.model.entity.DataQualityRule;
import com.miotech.kun.datadashboard.model.entity.DatasetBasic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Jie Chen
 * @created: 2020/9/15
 */
@Repository
public class DataQualityRepository extends BaseRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Value("${data-dashboard.long-existing-threshold:30}")
    Integer longExistingThreshold;

    public Long getCoveredDatasetCount() {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("count(distinct dataset_id) as count")
                .from("kun_dq_case_associated_dataset kdcad")
                .join("inner", "kun_mt_dataset", "kmd").on("kmd.gid = kdcad.dataset_id")
                .getSQL();

        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    public Long getLongExistingCount() {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("count(1) as count")
                .from("kun_dq_case_metrics kdcm")
                .join("inner", "(\n" +
                        "         select case_id, max(update_time) as last_update_time\n" +
                        "         from kun_dq_case_metrics\n" +
                        "         group by case_id\n" +
                        "     )", "last_metrics")
                .where("continuous_failing_count >= " + longExistingThreshold)
                .getSQL();

        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    public Long getSuccessCount() {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("count(1) as count")
                .from("kun_dq_case_metrics kdcm")
                .join("inner", "(\n" +
                        "         select case_id, max(update_time) as last_update_time\n" +
                        "         from kun_dq_case_metrics\n" +
                        "         group by case_id\n" +
                        "     )", "last_metrics")
                .where("continuous_failing_count = 0")
                .getSQL();

        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    public Long getTotalCaseCount() {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("count(1) as count")
                .from("kun_dq_case")
                .getSQL();

        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    private static final Map<String, String> TEST_CASES_REQUEST_ORDER_MAP = new HashMap<>();

    static {
        TEST_CASES_REQUEST_ORDER_MAP.put("continuousFailingCount", "continuous_failing_count");
        TEST_CASES_REQUEST_ORDER_MAP.put("updateTime", "last_update_time");
    }

    public DataQualityCases getTestCases(TestCasesRequest testCasesRequest) {
        String sql = "select kdcm.error_reason as error_reason, \n" +
                "kdcm.update_time as last_update_time, \n" +
                "kdcm.continuous_failing_count as continuous_failing_count, \n" +
                "kdcm.rule_records as rule_records, \n" +
                "kdcm.row_number as row_number, \n" +
                "kdc.create_user as case_owner, \n" +
                "kdc.id as case_id, \n" +
                "kdc.name as case_name from \n" +
                "(select *, ROW_NUMBER() OVER (PARTITION BY case_id ORDER BY update_time desc) AS row_number \n" +
                "from kun_dq_case_metrics) kdcm \n" +
                "inner join \n" +
                "kun_dq_case kdc on kdcm.case_id = kdc.id \n" +
                "where kdcm.row_number <= 1 AND kdcm.continuous_failing_count > 0";

        String countSql = "select count(1) from (" + sql + ") as result";

        sql += "order by " + TEST_CASES_REQUEST_ORDER_MAP.get(testCasesRequest.getSortColumn())
                + " " + testCasesRequest.getSortOrder() + " \n"
                + "offset " + getOffset(testCasesRequest.getPageNumber(), testCasesRequest.getPageSize()) + " \n"
                + "limit " + testCasesRequest.getPageSize();

        DataQualityCases dataQualityCases = new DataQualityCases();
        Long totalCount = jdbcTemplate.queryForObject(countSql, Long.class);
        dataQualityCases.setPageNumber(testCasesRequest.getPageNumber());
        dataQualityCases.setPageSize(testCasesRequest.getPageSize());
        dataQualityCases.setTotalCount(totalCount);
        return jdbcTemplate.query(sql, rs -> {
            while (rs.next()) {
                DataQualityCase dataQualityCase = new DataQualityCase();
                dataQualityCase.setStatus(TestCaseStatus.FAILED.name());
                dataQualityCase.setErrorReason(rs.getString("error_reason"));
                dataQualityCase.setUpdateTime(timestampToMillis(rs, "last_update_time"));
                dataQualityCase.setContinuousFailingCount(rs.getLong("continuous_failing_count"));
                dataQualityCase.setCaseId(rs.getLong("case_id"));
                dataQualityCase.setCaseOwner(rs.getString("case_owner"));
                dataQualityCase.setCaseName(rs.getString("case_name"));
                DatasetBasic datasetBasic = getPrimaryDataset(rs.getLong("case_id"));
                dataQualityCase.setDatasetGid(datasetBasic.getGid());
                dataQualityCase.setDatasetName(datasetBasic.getDatasetName());
                Type type = new TypeToken<List<DataQualityRule>>() {
                }.getType();
                dataQualityCase.setRuleRecords(JSONUtils.toJavaObject(rs.getString("rule_records"), type));
                dataQualityCases.add(dataQualityCase);
            }
            return dataQualityCases;
        });
    }

    private DatasetBasic getPrimaryDataset(Long caseId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("kmd.gid as dataset_id",
                        "kmd.name as dataset_name")
                .from("kun_mt_dataset kmd")
                .join("inner", "kun_dq_case_associated_dataset", "kdcad").on("kdcad.dataset_id = kmd.gid")
                .join("inner", "kun_dq_case", "kdc").on("kdc.primary_dataset_id is null or kdc.primary_dataset_id = kmd.gid")
                .where("kdcad.case_id = ?")
                .limit(1)
                .getSQL();

        return jdbcTemplate.query(sql, rs -> {
            DatasetBasic datasetBasic = new DatasetBasic();
            if (rs.next()) {
                datasetBasic.setGid(rs.getLong("dataset_id"));
                datasetBasic.setDatasetName(rs.getString("dataset_name"));
            }
            return datasetBasic;
        }, caseId);
    }
}
