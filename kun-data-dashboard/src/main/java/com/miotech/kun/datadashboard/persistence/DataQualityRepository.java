package com.miotech.kun.datadashboard.persistence;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;
import com.miotech.kun.common.BaseRepository;
import com.miotech.kun.common.utils.JSONUtils;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.datadashboard.model.bo.TestCasesRequest;
import com.miotech.kun.datadashboard.model.constant.TestCaseStatus;
import com.miotech.kun.datadashboard.model.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Type;
import java.util.*;

/**
 * @author: Jie Chen
 * @created: 2020/9/15
 */
@Repository("DashboardDataQualityRepository")
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
                        "     )", "last_metrics").on("last_metrics.last_update_time = kdcm.update_time")
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
                        "     )", "last_metrics").on("last_metrics.last_update_time = kdcm.update_time")
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

    public AbnormalDatasets getAbnormalDatasets(TestCasesRequest testCasesRequest) {
        String sql = "select kdc.primary_dataset_id as gid, " +
                "max(kdcm.update_time) as last_update_time " +
                "from " +
                "(select case_id, update_time, continuous_failing_count, ROW_NUMBER() OVER (PARTITION BY case_id ORDER BY update_time desc) AS row_number from kun_dq_case_metrics) kdcm " +
                "inner join " +
                "kun_dq_case kdc on kdcm.case_id = kdc.id " +
                "where kdcm.row_number <= 1 and kdcm.continuous_failing_count > 0 " +
                "group by kdc.primary_dataset_id " +
                "union " +
                "select kdad.dataset_gid as gid, " +
                "max(kdad.update_time) as last_update_time " +
                "from kun_dq_abnormal_dataset kdad " +
                "where kdad.status = 'FAILED' and kdad.schedule_at = (select max(schedule_at) from kun_dq_abnormal_dataset where status is not null) " +
                "group by kdad.dataset_gid ";


        String countSql = "select count(1) from (" + sql + ") as result";

        sql += "order by " + TEST_CASES_REQUEST_ORDER_MAP.get(testCasesRequest.getSortColumn())
                + " " + testCasesRequest.getSortOrder() + " \n"
                + "offset " + getOffset(testCasesRequest.getPageNumber(), testCasesRequest.getPageSize()) + " \n"
                + "limit " + testCasesRequest.getPageSize();

        AbnormalDatasets abnormalDatasets = new AbnormalDatasets();
        Integer totalCount = jdbcTemplate.queryForObject(countSql, Integer.class);
        abnormalDatasets.setPageNumber(testCasesRequest.getPageNumber());
        abnormalDatasets.setPageSize(testCasesRequest.getPageSize());
        abnormalDatasets.setTotalCount(totalCount);
        return jdbcTemplate.query(sql, rs -> {
            while (rs.next()) {
                AbnormalDataset abnormalDataset = new AbnormalDataset();
                abnormalDataset.setDatasetGid(rs.getLong("gid"));
                DatasetBasic datasetBasic = getDatasetBasic(abnormalDataset.getDatasetGid());
                abnormalDataset.setDatasetName(datasetBasic.getDatasetName());
                abnormalDataset.setDatabaseName(datasetBasic.getDatabase());
                abnormalDataset.setDatasourceName(datasetBasic.getDataSource());
                abnormalDataset.setUpdateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp("last_update_time")));
                abnormalDataset.setTasks(getFailedTask(abnormalDataset.getDatasetGid()));
                List<AbnormalCase> cases = getCases(abnormalDataset.getDatasetGid());
                Collections.sort(cases, Comparator.comparing(AbnormalCase::getStatus));
                abnormalDataset.setCases(cases);
                abnormalDataset.setFailedCaseCount(abnormalDataset.getTasks().size() + (int) abnormalDataset.getCases().stream().filter(c -> c.getStatus().equals("FAILED")).count());
                abnormalDatasets.add(abnormalDataset);
            }
            return abnormalDatasets;
        });
    }

    private List<AbnormalCase> getCases(Long datasetGid) {
        String sql = "select kdc.id, kdc.name, kdcm.continuous_failing_count, kdc.create_user, kdcm.rule_records, kdcm.update_time " +
                "from " +
                "(select case_id, update_time, continuous_failing_count, rule_records, ROW_NUMBER() OVER (PARTITION BY case_id ORDER BY update_time desc) AS row_number from kun_dq_case_metrics) kdcm " +
                "inner join " +
                "kun_dq_case kdc on kdcm.case_id = kdc.id " +
                "where kdcm.row_number <= 1 and kdc.primary_dataset_id = ?";

        return jdbcTemplate.query(sql, rs -> {
            List<AbnormalCase> abnormalCases = Lists.newArrayList();
            while (rs.next()) {
                AbnormalCase abnormalCase = new AbnormalCase();
                abnormalCase.setCaseId(rs.getLong("id"));
                abnormalCase.setCaseName(rs.getString("name"));
                long continuousFailingCount = rs.getLong("continuous_failing_count");
                abnormalCase.setContinuousFailingCount(continuousFailingCount);
                abnormalCase.setStatus(continuousFailingCount > 0 ? "FAILED" : "SUCCESS");
                abnormalCase.setCaseOwner(rs.getString("create_user"));
                abnormalCase.setUpdateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp("update_time")));
                abnormalCase.setRuleRecords(JSONUtils.toJavaObject(rs.getString("rule_records"),
                        new TypeToken<List<DataQualityRule>>() {}.getType()));
                abnormalCases.add(abnormalCase);
            }
            return abnormalCases;
        }, datasetGid);
    }

    private List<AbnormalTask> getFailedTask(Long datasetGid) {
        String sql = "select kdad.task_id, kdad.task_name, kdad.task_run_id, kdad.update_time " +
                "from kun_dq_abnormal_dataset kdad " +
                "where kdad.dataset_gid = ? and kdad.status = 'FAILED' and kdad.schedule_at = (select max(schedule_at) from kun_dq_abnormal_dataset where status is not null)";
        return jdbcTemplate.query(sql, rs -> {
            List<AbnormalTask> abnormalTasks = Lists.newArrayList();
            while (rs.next()) {
                AbnormalTask abnormalTask = new AbnormalTask();
                abnormalTask.setTaskId(rs.getLong("task_id"));
                abnormalTask.setTaskName(rs.getString("task_name"));
                abnormalTask.setTaskRunId(rs.getLong("task_run_id"));
                abnormalTask.setUpdateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp("update_time")));
                abnormalTasks.add(abnormalTask);
            }
            return abnormalTasks;
        }, datasetGid);
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
        Integer totalCount = jdbcTemplate.queryForObject(countSql, Integer.class);
        dataQualityCases.setPageNumber(testCasesRequest.getPageNumber());
        dataQualityCases.setPageSize(testCasesRequest.getPageSize());
        dataQualityCases.setTotalCount(totalCount);
        return jdbcTemplate.query(sql, rs -> {
            while (rs.next()) {
                DataQualityCase dataQualityCase = new DataQualityCase();
                dataQualityCase.setStatus(TestCaseStatus.FAILED.name());
                dataQualityCase.setErrorReason(rs.getString("error_reason"));
                dataQualityCase.setUpdateTime(timestampToOffsetDateTime(rs, "last_update_time"));
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

    private DatasetBasic getDatasetBasic (Long datasetGid) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("kmd.name as dataset_name", "kmd.database_name as database_name", "kmda.name as datasource_name")
                .from("kun_mt_dataset kmd")
                .join("inner", "kun_mt_datasource", "kmds").on("kmd.datasource_id = kmds.id")
                .join("inner", "kun_mt_datasource_attrs", "kmda").on("kmda.datasource_id = kmd.datasource_id")
                .where("kmd.gid = ?")
                .limit(1)
                .getSQL();

        return jdbcTemplate.query(sql, rs -> {
            DatasetBasic datasetBasic = new DatasetBasic();
            String datasetName = null;
            String databaseName = null;
            String datasourceName = null;
            if (rs.next()) {
                datasetName = rs.getString("dataset_name");
                databaseName = rs.getString("database_name");
                datasourceName = rs.getString("datasource_name");
            }
            datasetBasic.setDatasetName(datasetName);
            datasetBasic.setDatabase(databaseName);
            datasetBasic.setDataSource(datasourceName);

            return datasetBasic;
        }, datasetGid);
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
