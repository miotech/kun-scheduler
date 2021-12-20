package com.miotech.kun.datadashboard.persistence;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.reflect.TypeToken;
import com.miotech.kun.common.BaseRepository;
import com.miotech.kun.common.utils.JSONUtils;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.datadashboard.model.bo.TestCasesRequest;
import com.miotech.kun.datadashboard.model.entity.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

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
        String sql = "select gid, max(last_update_time) last_update_time from (" +
                "select kdc.primary_dataset_id as gid, " +
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
                "group by kdad.dataset_gid) t " +
                "group by gid ";


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
        AbnormalDatasets datasets = jdbcTemplate.query(sql, rs -> {
            while (rs.next()) {
                AbnormalDataset abnormalDataset = new AbnormalDataset();
                abnormalDataset.setDatasetGid(rs.getLong("gid"));
                DatasetBasic datasetBasic = getDatasetBasic(abnormalDataset.getDatasetGid());
                abnormalDataset.setDatasetName(datasetBasic.getDatasetName());
                abnormalDataset.setDatabaseName(datasetBasic.getDatabase());
                abnormalDataset.setDatasourceName(datasetBasic.getDataSource());
                abnormalDataset.setGlossaries(datasetBasic.getGlossaries());
                abnormalDataset.setUpdateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp("last_update_time")));
                abnormalDatasets.add(abnormalDataset);
            }
            return abnormalDatasets;
        });

        List<Long> datasetGids = datasets.getAbnormalDatasets().stream().map(ad -> ad.getDatasetGid()).collect(Collectors.toList());
        Map<Long, List<AbnormalCase>> abnormalCasesMap = getCases(datasetGids);
        Map<Long, List<AbnormalTask>> abnormalTasksMap = getFailedTask(datasetGids);
        datasets.getAbnormalDatasets().stream().forEach(ad -> {
            List<AbnormalCase> abnormalCases = abnormalCasesMap.getOrDefault(ad.getDatasetGid(), Lists.newArrayList());
            Collections.sort(abnormalCases, Comparator.comparing(AbnormalCase::getStatus));
            ad.setCases(abnormalCases);
            ad.setTasks(abnormalTasksMap.getOrDefault(ad.getDatasetGid(), Lists.newArrayList()));
            ad.setFailedCaseCount(ad.getTasks().size() + (int) ad.getCases().stream().filter(c -> c.getStatus().equals("FAILED")).count());
        });
        return datasets;
    }

    private Map<Long, List<AbnormalCase>> getCases(List<Long> datasetGids) {
        String sql = "select kdc.id, kdc.name, kdcm.continuous_failing_count, kdc.create_user, kdc.primary_dataset_id, kdcm.rule_records, kdcm.update_time " +
                "from " +
                "(select case_id, update_time, continuous_failing_count, rule_records, ROW_NUMBER() OVER (PARTITION BY case_id ORDER BY update_time desc) AS row_number from kun_dq_case_metrics) kdcm " +
                "inner join " +
                "kun_dq_case kdc on kdcm.case_id = kdc.id " +
                "where kdcm.row_number <= 1 and kdc.primary_dataset_id in " + toColumnSql(datasetGids.size());

        return jdbcTemplate.query(sql, rs -> {
            Map<Long, List<AbnormalCase>> result = Maps.newHashMap();
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
                long primaryDatasetId = rs.getLong("primary_dataset_id");
                if (result.containsKey(primaryDatasetId)) {
                    result.get(primaryDatasetId).add(abnormalCase);
                } else {
                    List<AbnormalCase> abnormalCases = Lists.newArrayList();
                    abnormalCases.add(abnormalCase);
                    result.put(primaryDatasetId, abnormalCases);
                }
            }
            return result;
        }, datasetGids.toArray());
    }

    private Map<Long, List<AbnormalTask>> getFailedTask(List<Long> datasetGids) {
        String sql = "select kdad.task_id, kdad.task_name, kdad.task_run_id, kdad.update_time, kdad.dataset_gid " +
                "from kun_dq_abnormal_dataset kdad " +
                "where kdad.dataset_gid in " + toColumnSql(datasetGids.size()) + " " +
                "and kdad.status = 'FAILED' and kdad.schedule_at = (select max(schedule_at) from kun_dq_abnormal_dataset where status is not null)";
        return jdbcTemplate.query(sql, rs -> {
            Map<Long, List<AbnormalTask>> result = Maps.newHashMap();
            while (rs.next()) {
                AbnormalTask abnormalTask = new AbnormalTask();
                abnormalTask.setTaskId(rs.getLong("task_id"));
                abnormalTask.setTaskName(rs.getString("task_name"));
                abnormalTask.setTaskRunId(rs.getLong("task_run_id"));
                abnormalTask.setUpdateTime(DateTimeUtils.fromTimestamp(rs.getTimestamp("update_time")));
                long datasetGid = rs.getLong("dataset_gid");
                if (result.containsKey(datasetGid)) {
                    result.get(datasetGid).add(abnormalTask);
                } else {
                    List<AbnormalTask> abnormalTasks = Lists.newArrayList();
                    abnormalTasks.add(abnormalTask);
                    result.put(datasetGid, abnormalTasks);
                }
            }
            return result;
        }, datasetGids.toArray());
    }

    private DatasetBasic getDatasetBasic(Long datasetGid) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("kmd.name as dataset_name", "kmd.database_name as database_name", "kmda.name as datasource_name", "string_agg(concat(cast(kmg.id as varchar), ',', kmg.name), ';') as glossaries")
                .from("kun_mt_dataset kmd")
                .join("inner", "kun_mt_datasource", "kmds").on("kmd.datasource_id = kmds.id")
                .join("inner", "kun_mt_datasource_attrs", "kmda").on("kmda.datasource_id = kmd.datasource_id")
                .join("left", "kun_mt_glossary_to_dataset_ref", "kmgtdr").on("kmd.gid = kmgtdr.dataset_id")
                .join("left", "kun_mt_glossary", "kmg").on("kmg.id = kmgtdr.glossary_id")
                .where("kmd.gid = ?")
                .groupBy("kmd.name, kmd.database_name, kmda.name")
                .getSQL();

        return jdbcTemplate.query(sql, rs -> {
            DatasetBasic datasetBasic = new DatasetBasic();
            String datasetName = null;
            String databaseName = null;
            String datasourceName = null;
            List<GlossaryBasic> glossaries = Lists.newArrayList();
            if (rs.next()) {
                datasetName = rs.getString("dataset_name");
                databaseName = rs.getString("database_name");
                datasourceName = rs.getString("datasource_name");
                String glossariesStr = rs.getString("glossaries");
                if (StringUtils.isNotBlank(glossariesStr)) {
                    Arrays.stream(glossariesStr.split(";"))
                            .filter(info -> info.split(",").length == 2)
                            .forEach(info -> {
                        String[] infoArr = info.split(",");
                        GlossaryBasic glossary = new GlossaryBasic(Long.parseLong(infoArr[0]), infoArr[1]);
                        glossaries.add(glossary);
                    });
                }
            }
            datasetBasic.setDatasetName(datasetName);
            datasetBasic.setDatabase(databaseName);
            datasetBasic.setDataSource(datasourceName);
            datasetBasic.setGlossaries(glossaries);

            return datasetBasic;
        }, datasetGid);
    }

}
