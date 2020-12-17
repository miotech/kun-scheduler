package com.miotech.kun.dataquality.persistence;

import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;
import com.miotech.kun.common.BaseRepository;
import com.miotech.kun.common.utils.IdUtils;
import com.miotech.kun.common.utils.JSONUtils;
import com.miotech.kun.commons.db.sql.DefaultSQLBuilder;
import com.miotech.kun.commons.db.sql.SQLBuilder;
import com.miotech.kun.commons.utils.IdGenerator;
import com.miotech.kun.dataquality.model.DataQualityStatus;
import com.miotech.kun.dataquality.model.TemplateType;
import com.miotech.kun.dataquality.model.bo.DataQualitiesRequest;
import com.miotech.kun.dataquality.model.bo.DataQualityHistoryRequest;
import com.miotech.kun.dataquality.model.bo.DataQualityRequest;
import com.miotech.kun.dataquality.model.bo.DeleteCaseResponse;
import com.miotech.kun.dataquality.model.entity.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: Jie Chen
 * @created: 2020/7/16
 */
@Repository
public class DataQualityRepository extends BaseRepository {

    private static final Integer HISTORY_RECORD_LIMIT = 6;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    DatasetRepository datasetRepository;

    public List<DataQualityHistoryRecords> getHistory(DataQualityHistoryRequest request) {
        if (CollectionUtils.isEmpty(request.getCaseIds())) {
            return Lists.newArrayList();
        }
        String sql = "select * from (select *, ROW_NUMBER() OVER (PARTITION BY case_id ORDER BY update_time desc) AS row_number\n" +
                "from kun_dq_case_metrics\n" +
                "where case_id in " + toColumnSql(request.getCaseIds().size()) + ") kdcm\n" +
                "where kdcm.row_number <= " + HISTORY_RECORD_LIMIT;
        return jdbcTemplate.query(sql, rs -> {
            Map<Long, DataQualityHistoryRecords> recordsMap = new HashMap<>();
            while (rs.next()) {
                Long caseId = rs.getLong("case_id");
                DataQualityHistoryRecords records = recordsMap.computeIfAbsent(caseId, key -> new DataQualityHistoryRecords());
                List<DataQualityHistory> historyList = records.getHistoryList();
                records.setCaseId(caseId);
                DataQualityHistory history = new DataQualityHistory();
                if (rs.getLong("continuous_failing_count") == 0) {
                    history.setStatus(DataQualityStatus.SUCCESS.name());
                } else {
                    history.setStatus(DataQualityStatus.FAILED.name());
                }
                history.setContinuousFailingCount(rs.getLong("continuous_failing_count"));
                history.setUpdateTime(timestampToMillis(rs, "update_time"));
                history.setErrorReason(rs.getString("error_reason"));
                Type type = new TypeToken<List<DataQualityRule>>() {
                }.getType();
                history.setRuleRecords(JSONUtils.toJavaObject(rs.getString("rule_records"), type));
                historyList.add(history);
            }
            return Lists.newArrayList(recordsMap.values());
        }, request.getCaseIds().toArray());
    }

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

    public Long getLatestTaskId(Long caseId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("task_id")
                .from("kun_dq_case")
                .where("id = ?")
                .getSQL();

        return jdbcTemplate.queryForObject(sql, Long.class, caseId);
    }

    public void saveTaskId(Long caseId, Long taskId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .update("kun_dq_case")
                .set("task_id")
                .asPrepared()
                .where("id = ?")
                .getSQL();

        jdbcTemplate.update(sql, taskId, caseId);
    }

    @Transactional(rollbackFor = Exception.class)
    public DeleteCaseResponse deleteCase(Long id) {
        String kdcDeleteSql = DefaultSQLBuilder.newBuilder()
                .delete()
                .from("kun_dq_case")
                .where("id = ?")
                .getSQL();
        jdbcTemplate.update(kdcDeleteSql, id);

        String kdcadDeleteSql = DefaultSQLBuilder.newBuilder()
                .delete()
                .from("kun_dq_case_associated_dataset")
                .where("case_id = ?")
                .getSQL();
        jdbcTemplate.update(kdcadDeleteSql, id);

        String kdcadfDeleteSql = DefaultSQLBuilder.newBuilder()
                .delete()
                .from("kun_dq_case_associated_dataset_field")
                .where("case_id = ?")
                .getSQL();
        jdbcTemplate.update(kdcadfDeleteSql, id);

        String kdcmDeleteSql = DefaultSQLBuilder.newBuilder()
                .delete()
                .from("kun_dq_case_metrics")
                .where("case_id = ?")
                .getSQL();
        jdbcTemplate.update(kdcmDeleteSql, id);

        String kdcrDeleteSql = DefaultSQLBuilder.newBuilder()
                .delete()
                .from("kun_dq_case_rules")
                .where("case_id = ?")
                .getSQL();
        jdbcTemplate.update(kdcrDeleteSql, id);

        DeleteCaseResponse response = new DeleteCaseResponse();
        response.setId(id);
        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long updateCase(Long id, DataQualityRequest dataQualityRequest) {
        String kdcSql = DefaultSQLBuilder.newBuilder()
                .update("kun_dq_case")
                .set("name",
                        "description",
                        "template_id",
                        "execution_string",
                        "update_user",
                        "update_time",
                        "types")
                .asPrepared()
                .where("id = ?")
                .getSQL();
        String types = resolveDqCaseTypes(dataQualityRequest.getTypes());
        if (TemplateType.CUSTOMIZE.name().equals(dataQualityRequest.getDimension())) {
            jdbcTemplate.update(kdcSql, dataQualityRequest.getName(),
                    dataQualityRequest.getDescription(),
                    null,
                    dataQualityRequest.getDimensionConfig().get("sql"),
                    dataQualityRequest.getUpdateUser(),
                    millisToTimestamp(dataQualityRequest.getUpdateTime()),
                    types,
                    id);
        } else {
            jdbcTemplate.update(kdcSql, dataQualityRequest.getName(),
                    dataQualityRequest.getDescription(),
                    Long.valueOf(dataQualityRequest.getDimensionConfig().get("templateId").toString()),
                    null,
                    dataQualityRequest.getUpdateUser(),
                    millisToTimestamp(dataQualityRequest.getUpdateTime()),
                    types,
                    id);
            if (TemplateType.FIELD.name().equals(dataQualityRequest.getDimension())) {
                List<Long> fieldIds = ((List<?>) dataQualityRequest.getDimensionConfig().get("applyFieldIds")).stream()
                        .map(o -> Long.valueOf(o.toString())).collect(Collectors.toList());
                overwriteRelatedDatasetField(id, fieldIds);
            }
        }
        overwriteRelatedDataset(id, dataQualityRequest.getRelatedTableIds(), dataQualityRequest.getPrimaryDatasetGid());
        overwriteRelatedRules(id, dataQualityRequest.getValidateRules());
        return id;
    }

    @Transactional(rollbackFor = Exception.class)
    public void overwriteRelatedRules(Long caseId, List<DataQualityRule> rules) {
        String deleteSql = DefaultSQLBuilder.newBuilder()
                .delete()
                .from("kun_dq_case_rules")
                .where("case_id = ?")
                .getSQL();
        jdbcTemplate.update(deleteSql, caseId);

        if (CollectionUtils.isNotEmpty(rules)) {
            String insertSql = DefaultSQLBuilder.newBuilder()
                    .insert()
                    .into("kun_dq_case_rules")
                    .valueSize(6)
                    .getSQL();
            for (DataQualityRule rule : rules) {
                jdbcTemplate.update(insertSql, IdGenerator.getInstance().nextId(),
                        caseId,
                        rule.getField(),
                        rule.getOperator(),
                        rule.getExpectedType(),
                        rule.getExpectedValue());
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void overwriteRelatedDataset(Long caseId, List<Long> datasetIds, Long primaryDatasetId) {
        String deleteSql = DefaultSQLBuilder.newBuilder()
                .delete()
                .from("kun_dq_case_associated_dataset")
                .where("case_id = ?")
                .getSQL();
        jdbcTemplate.update(deleteSql, caseId);

        if (CollectionUtils.isNotEmpty(datasetIds)) {
            String insertSql = DefaultSQLBuilder.newBuilder()
                    .insert()
                    .into("kun_dq_case_associated_dataset")
                    .valueSize(3)
                    .getSQL();
            for (Long datasetId : datasetIds) {
                jdbcTemplate.update(insertSql, IdGenerator.getInstance().nextId(),
                        caseId,
                        datasetId);
            }
        }

        if (IdUtils.isNotEmpty(primaryDatasetId)) {
            updatePrimaryTable(caseId, primaryDatasetId);
        }
    }

    private void updatePrimaryTable(Long caseId, Long datasetGid) {
        String sql = DefaultSQLBuilder.newBuilder()
                .update("kun_dq_case")
                .set("primary_dataset_id")
                .asPrepared()
                .where("id = ?")
                .getSQL();

        jdbcTemplate.update(sql, datasetGid, caseId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void overwriteRelatedDatasetField(Long caseId, List<Long> fieldIds) {
        String deleteSql = DefaultSQLBuilder.newBuilder()
                .delete()
                .from("kun_dq_case_associated_dataset_field")
                .where("case_id = ?")
                .getSQL();
        jdbcTemplate.update(deleteSql, caseId);

        if (CollectionUtils.isNotEmpty(fieldIds)) {
            String insertSql = DefaultSQLBuilder.newBuilder()
                    .insert()
                    .into("kun_dq_case_associated_dataset_field")
                    .valueSize(3)
                    .getSQL();
            for (Long fieldId : fieldIds) {
                jdbcTemplate.update(insertSql, IdGenerator.getInstance().nextId(),
                        caseId,
                        fieldId);
            }
        }
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

    public DimensionConfig getDimensionConfig(String dsType) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("kdct.name as name",
                        "kdct.type as type",
                        "kdcdt.id as id")
                .from("kun_dq_case_template kdct")
                .join("inner", "kun_dq_case_datasource_template", "kdcdt").on("kdcdt.template_id = kdct.id")
                .join("inner", "kun_mt_datasource_type", "kmdt").on("kmdt.id = kdcdt.datasource_type_id")
                .where("kmdt.name = ?")
                .getSQL();

        return jdbcTemplate.query(sql, rs -> {
            DimensionConfig dimensionConfig = new DimensionConfig();
            Map<String, JSONObject> dimensionMap = new HashMap<>();
            for (TemplateType value : TemplateType.values()) {
                if (value == TemplateType.CUSTOMIZE) {
                    continue;
                }
                JSONObject templateDimension = new JSONObject();
                templateDimension.put("dimension", value.name());
                templateDimension.put("templates", new JSONArray());
                dimensionMap.put(value.name(), templateDimension);
                dimensionConfig.getDimensionConfigs().add(templateDimension);
            }
            while (rs.next()) {
                String type = rs.getString("type");
                JSONObject templateDimension = dimensionMap.get(type);

                JSONObject template = new JSONObject();
                template.put("id", rs.getString("id"));
                template.put("name", rs.getString("name"));
                ((JSONArray) templateDimension.get("templates")).add(template);
            }

            dimensionConfig.getDimensionConfigs().add(getCustomDimensionConfig());
            return dimensionConfig;
        }, dsType);
    }

    private JSONObject getCustomDimensionConfig() {
        JSONObject customDimension = new JSONObject();
        customDimension.put("dimension", TemplateType.CUSTOMIZE);
        JSONArray customFields = new JSONArray();
        JSONObject sqlField = createCustomField("sql", 1, "SQL", true);
        customFields.add(sqlField);
        customDimension.put("fields", customFields);
        return customDimension;
    }

    private JSONObject createCustomField(String key,
                                         Integer order,
                                         String format,
                                         Boolean require) {
        JSONObject field = new JSONObject();
        field.put("key", key);
        field.put("order", order);
        field.put("format", format);
        field.put("require", require);
        return field;
    }

    private String resolveDqCaseTypes(List<String> types) {
        if (CollectionUtils.isNotEmpty(types)) {
            StringJoiner stringJoiner = new StringJoiner(",");
            for (String type : types) {
                stringJoiner.add(type);
            }
            return stringJoiner.toString();
        }
        return "";
    }

    @Transactional(rollbackFor = Exception.class)
    public Long addCase(DataQualityRequest dataQualityRequest) {
        String kdcSql = DefaultSQLBuilder.newBuilder()
                .insert().into("kun_dq_case")
                .valueSize(12)
                .getSQL();

        String dimension = dataQualityRequest.getDimension();
        Long caseId = IdGenerator.getInstance().nextId();
        String typesStr = resolveDqCaseTypes(dataQualityRequest.getTypes());
        if (TemplateType.CUSTOMIZE.name().equals(dimension)) {
            String customSql = (String) dataQualityRequest.getDimensionConfig().get("sql");
            jdbcTemplate.update(kdcSql, caseId,
                    dataQualityRequest.getName(),
                    dataQualityRequest.getDescription(),
                    null,
                    customSql,
                    dataQualityRequest.getCreateUser(),
                    millisToTimestamp(dataQualityRequest.getCreateTime()),
                    dataQualityRequest.getUpdateUser(),
                    millisToTimestamp(dataQualityRequest.getUpdateTime()),
                    dataQualityRequest.getTaskId(),
                    typesStr,
                    null);
        } else {
            Long templateId = Long.valueOf((String) dataQualityRequest.getDimensionConfig().get("templateId"));
            jdbcTemplate.update(kdcSql, caseId,
                    dataQualityRequest.getName(),
                    dataQualityRequest.getDescription(),
                    templateId,
                    null,
                    dataQualityRequest.getCreateUser(),
                    millisToTimestamp(dataQualityRequest.getCreateTime()),
                    dataQualityRequest.getUpdateUser(),
                    millisToTimestamp(dataQualityRequest.getUpdateTime()),
                    dataQualityRequest.getTaskId(),
                    typesStr,
                    null);
            if (TemplateType.FIELD.name().equals(dimension)) {
                String kdcadfSql = DefaultSQLBuilder.newBuilder()
                        .insert().into("kun_dq_case_associated_dataset_field")
                        .valueSize(3)
                        .getSQL();
                List<?> fieldIds = (List<?>) dataQualityRequest.getDimensionConfig().get("applyFieldIds");
                for (Object fieldId : fieldIds) {
                    jdbcTemplate.update(kdcadfSql, IdGenerator.getInstance().nextId(),
                            caseId,
                            Long.valueOf(fieldId.toString()));
                }
            }
        }

        overwriteRelatedDataset(caseId, dataQualityRequest.getRelatedTableIds(), dataQualityRequest.getPrimaryDatasetGid());
        String kdcrSql = DefaultSQLBuilder.newBuilder()
                .insert()
                .into("kun_dq_case_rules")
                .valueSize(6)
                .getSQL();
        for (DataQualityRule validateRule : dataQualityRequest.getValidateRules()) {
            jdbcTemplate.update(kdcrSql, IdGenerator.getInstance().nextId(),
                    caseId,
                    validateRule.getField(),
                    validateRule.getOperator(),
                    validateRule.getExpectedType(),
                    validateRule.getExpectedValue());
        }
        return caseId;
    }

    public DataQualityCaseBasic getCaseBasic(Long id) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("id", "name")
                .from("kun_dq_case")
                .where("id = ?")
                .getSQL();

        return jdbcTemplate.query(sql, rs -> {
            DataQualityCaseBasic caseBasic = new DataQualityCaseBasic();
            if (rs.next()) {
                caseBasic.setId(rs.getLong("id"));
                caseBasic.setName(rs.getString("name"));
            }
            return caseBasic;
        }, id);
    }

    private List<String> resolveDqCaseTypes(String types) {
        if (StringUtils.isNotEmpty(types)) {
            return Arrays.asList(types.split(","));
        }
        return null;
    }

    public DataQualityCaseBasics getCaseBasics(DataQualitiesRequest request) {
        SQLBuilder getSqlBuilder = DefaultSQLBuilder.newBuilder()
                .select("kdc.id as case_id",
                        "kdc.name as case_name",
                        "kdc.update_user as case_update_user",
                        "kdc.types as case_types",
                        "kdc.task_id as case_task_id",
                        "kdc.create_time as create_time",
                        "kdc.update_time as update_time",
                        "kdc.primary_dataset_id as primary_dataset_id")
                .from("kun_dq_case kdc")
                .join("inner", "kun_dq_case_associated_dataset", "kdcad").on("kdc.id = kdcad.case_id")
                .where("kdcad.dataset_id = ?");

        String countSql = DefaultSQLBuilder.newBuilder()
                .select("count(1) as total_count")
                .from("(" + getSqlBuilder.getSQL() + ") temp")
                .getSQL();
        Long totalCount = jdbcTemplate.queryForObject(countSql, Long.class, request.getGid());

        getSqlBuilder
                .orderBy("kdc.create_time desc")
                .offset(getOffset(request.getPageNumber(), request.getPageSize()))
                .limit(request.getPageSize());
        return jdbcTemplate.query(getSqlBuilder.getSQL(), rs -> {
            DataQualityCaseBasics caseBasics = new DataQualityCaseBasics();
            while (rs.next()) {
                DataQualityCaseBasic caseBasic = new DataQualityCaseBasic();
                caseBasic.setId(rs.getLong("case_id"));
                caseBasic.setName(rs.getString("case_name"));
                caseBasic.setUpdater(rs.getString("case_update_user"));
                caseBasic.setTypes(resolveDqCaseTypes(rs.getString("case_types")));
                caseBasic.setTaskId(rs.getLong("case_task_id"));
                caseBasic.setCreateTime(timestampToMillis(rs, "create_time"));
                caseBasic.setUpdateTime(timestampToMillis(rs, "update_time"));
                Long primaryDatasetId = rs.getLong("primary_dataset_id");
                caseBasic.setIsPrimary(request.getGid().equals(primaryDatasetId));
                caseBasics.add(caseBasic);
            }
            caseBasics.setPageNumber(request.getPageNumber());
            caseBasics.setPageSize(request.getPageSize());
            caseBasics.setTotalCount(totalCount);
            return caseBasics;
        }, request.getGid());
    }

    public List<DataQualityCase> getAllCases() {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("kdc.id as case_id",
                        "kdc.execution_string as custom_string")
                .from("kun_dq_case kdc")
                .getSQL();
        return jdbcTemplate.query(sql, rs -> {
            List<DataQualityCase> result = Lists.newArrayList();
            while (rs.next()) {
                DataQualityCase dqCase = new DataQualityCase();
                String dimension = TemplateType.CUSTOMIZE.name();
                JSONObject dimensionConfig = new JSONObject();
                dimensionConfig.put("sql", rs.getString("custom_string"));
                dqCase.setId(rs.getLong("case_id"));
                dqCase.setDimension(dimension);
                dqCase.setDimensionConfig(dimensionConfig);
                result.add(dqCase);
            }
            return result;
        });
    }

    public DataQualityCase getCase(Long id) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("kdc.id as case_id",
                        "kdc.name as case_name",
                        "kdc.description as case_description",
                        "kdc.template_id as case_temp_id",
                        "kdc.execution_string as custom_string",
                        "kdc.types as case_types",
                        "kdcdt.execution_string as temp_string",
                        "kdct.type as temp_type")
                .from("kun_dq_case kdc")
                .join("left", "kun_dq_case_datasource_template", "kdcdt").on("kdcdt.id = kdc.template_id")
                .join("left", "kun_dq_case_template", "kdct").on("kdct.id = kdcdt.template_id")
                .where("kdc.id = ?")
                .getSQL();

        return jdbcTemplate.query(sql, rs -> {
            DataQualityCase dqCase = new DataQualityCase();
            String dimension;
            JSONObject dimensionConfig = new JSONObject();
            if (rs.next()) {
                Long tempId = rs.getLong("case_temp_id");
                if (tempId.equals(0L)) {
                    dimension = TemplateType.CUSTOMIZE.name();
                    dimensionConfig.put("sql", rs.getString("custom_string"));
                } else {
                    dimension = rs.getString("temp_type");
                    dimensionConfig.put("templateId", String.valueOf(tempId));
                    if (TemplateType.FIELD.name().equals(dimension)) {
                        dimensionConfig.put("applyFieldIds", getDatasetFieldIdsByCaseId(rs.getLong("case_id")).stream()
                                .map(String::valueOf).collect(Collectors.toList()));
                    }
                }
                dqCase.setId(rs.getLong("case_id"));
                dqCase.setName(rs.getString("case_name"));
                dqCase.setDescription(rs.getString("case_description"));
                dqCase.setDimension(dimension);
                dqCase.setDimensionConfig(dimensionConfig);
                dqCase.setValidateRules(getRulesByCaseId(id));
                dqCase.setRelatedTables(getDatasetBasicsByCaseId(id));
                dqCase.setTypes(resolveDqCaseTypes(rs.getString("case_types")));
            }
            return dqCase;
        }, id);
    }

    private List<DatasetBasic> getDatasetBasicsByCaseId(Long caseId) {
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
        }, caseId);

        sql = DefaultSQLBuilder.newBuilder()
                .select("primary_dataset_id")
                .from("kun_dq_case")
                .where("id = ?")
                .getSQL();
        Long primaryDatasetId = jdbcTemplate.queryForObject(sql, Long.class, caseId);

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

    private List<DataQualityRule> getRulesByCaseId(Long caseId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("field",
                        "operator",
                        "expected_value_type",
                        "expected_value")
                .from("kun_dq_case_rules")
                .where("case_id = ?")
                .getSQL();

        return jdbcTemplate.query(sql, rs -> {
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

    private List<Long> getDatasetFieldIdsByCaseId(Long caseId) {
        String sql = DefaultSQLBuilder.newBuilder()
                .select("dataset_field_id")
                .from("kun_dq_case_associated_dataset_field")
                .where("case_id = ?")
                .getSQL();

        return jdbcTemplate.query(sql, rs -> {
            List<Long> fieldIds = new ArrayList<>();
            while (rs.next()) {
                fieldIds.add(rs.getLong("dataset_field_id"));
            }
            return fieldIds;
        }, caseId);
    }

}
