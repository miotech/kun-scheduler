package com.miotech.kun.datadashboard.persistence;

import com.miotech.kun.common.BaseRepository;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.db.sql.SQLBuilder;
import com.miotech.kun.datadashboard.model.bo.TestCasesRequest;
import com.miotech.kun.datadashboard.model.constant.TestCaseStatus;
import com.miotech.kun.datadashboard.model.entity.TestCase;
import com.miotech.kun.datadashboard.model.entity.TestCases;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
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
    public TestCases getTestCases(TestCasesRequest testCasesRequest) {
        SQLBuilder preSqlBuilder = DefaultSQLBuilder.newBuilder()
                .select("kdcm.error_reason as error_reason",
                        "kdcm.update_time as last_update_time",
                        "kdcm.continuous_failing_count as continuous_failing_count",
                        "kdc.create_user as case_owner")
                .from("kun_dq_case_metrics kdcm")
                .join("inner", "kun_dq_case", "kdc").on("kdcm.dq_case_id = kdc.id")
                .where("kdcm.continuous_failing_count > 0");

        String countSql = "select count(1) from (" + preSqlBuilder.getSQL() + ") as result";

        String sql = preSqlBuilder
                .orderBy(TEST_CASES_REQUEST_ORDER_MAP.get(testCasesRequest.getSortColumn()) + " " + testCasesRequest.getSortOrder())
                .offset(getOffset(testCasesRequest.getPageNumber(), testCasesRequest.getPageSize()))
                .limit(testCasesRequest.getPageSize())
                .getSQL();

        TestCases testCases = new TestCases();
        Long totalCount = jdbcTemplate.queryForObject(countSql, Long.class);
        testCases.setPageNumber(testCasesRequest.getPageNumber());
        testCases.setPageSize(testCasesRequest.getPageSize());
        testCases.setTotalCount(totalCount);
        return jdbcTemplate.query(sql, rs -> {
            while (rs.next()) {
                TestCase testCase = new TestCase();
                testCase.setResult(TestCaseStatus.FAILED.name());
                testCase.setErrorReason(rs.getString("error_reason"));
                testCase.setUpdateTime(timestampToMillis(rs, "last_update_time"));
                testCase.setContinuousFailingCount(rs.getLong("continuous_failing_count"));
                testCase.setCaseOwner(rs.getString("case_owner"));
                testCases.add(testCase);
            }
            return testCases;
        });
    }
}
