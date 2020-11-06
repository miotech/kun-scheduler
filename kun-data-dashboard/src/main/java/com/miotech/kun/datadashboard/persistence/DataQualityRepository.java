package com.miotech.kun.datadashboard.persistence;

import com.google.gson.reflect.TypeToken;
import com.miotech.kun.common.BaseRepository;
import com.miotech.kun.common.model.RequestResult;
import com.miotech.kun.common.utils.JSONUtils;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.db.sql.SQLBuilder;
import com.miotech.kun.commons.query.datasource.DataSourceType;
import com.miotech.kun.datadashboard.model.bo.TestCasesRequest;
import com.miotech.kun.datadashboard.model.constant.TestCaseStatus;
import com.miotech.kun.datadashboard.model.entity.DataQualityRule;
import com.miotech.kun.datadashboard.model.entity.DatasetBasic;
import com.miotech.kun.datadashboard.model.entity.DataQualityCase;
import com.miotech.kun.datadashboard.model.entity.DataQualityCases;
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

    @Value("${data-dashboard.long-existing-threshold}")
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
                .from("kun_dq_case_metrics")
                .where("continuous_failing_count >= 30")
                .getSQL();

        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    public Long getSuccessCount() {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("count(1) as count")
                .from("kun_dq_case_metrics")
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
        SQLBuilder preSqlBuilder = DefaultSQLBuilder.newBuilder()
                .select("kdcm.error_reason as error_reason",
                        "kdcm.update_time as last_update_time",
                        "kdcm.continuous_failing_count as continuous_failing_count",
                        "kdcm.rule_records as rule_records",
                        "kdc.create_user as case_owner",
                        "kdc.id as case_id",
                        "kdc.name as case_name")
                .from("kun_dq_case_metrics kdcm")
                .join("inner", "kun_dq_case", "kdc").on("kdcm.case_id = kdc.id")
                .where("kdcm.continuous_failing_count > 0");

        String countSql = "select count(1) from (" + preSqlBuilder.getSQL() + ") as result";

        String sql = preSqlBuilder
                .orderBy(TEST_CASES_REQUEST_ORDER_MAP.get(testCasesRequest.getSortColumn()) + " " + testCasesRequest.getSortOrder())
                .offset(getOffset(testCasesRequest.getPageNumber(), testCasesRequest.getPageSize()))
                .limit(testCasesRequest.getPageSize())
                .getSQL();

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
