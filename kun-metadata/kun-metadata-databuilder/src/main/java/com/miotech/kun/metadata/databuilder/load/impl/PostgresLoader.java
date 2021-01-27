package com.miotech.kun.metadata.databuilder.load.impl;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.Maps;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.metadata.common.utils.DataStoreJsonUtil;
import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.core.model.DatasetField;
import com.miotech.kun.metadata.core.model.DatasetFieldStat;
import com.miotech.kun.metadata.core.model.TableStatistics;
import com.miotech.kun.metadata.databuilder.load.Loader;
import com.miotech.kun.metadata.databuilder.model.DatasetFieldInformation;
import com.miotech.kun.metadata.databuilder.service.gid.GidService;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class PostgresLoader implements Loader {
    private static Logger logger = LoggerFactory.getLogger(PostgresLoader.class);

    private final DatabaseOperator dbOperator;
    private final GidService gidGenerator;

    @Inject
    public PostgresLoader(DatabaseOperator dbOperator, GidService gidService) {
        this.dbOperator = dbOperator;
        this.gidGenerator = gidService;
    }

    @Override
    public void loadSchema(Long gid, List<DatasetField> fields) {
        if (logger.isDebugEnabled()) {
            logger.debug("PostgresLoader loadSchema, gid: {}, fields: {}", gid, JSONUtils.toJsonString(fields));
        }

        Map<String, DatasetFieldInformation> fieldInfos = Maps.newHashMap();
        List<String> survivorFields = Lists.newArrayList();
        List<String> dropFields = Lists.newArrayList();
        fill(fields, fieldInfos, dropFields, survivorFields, gid);

        if (!dropFields.isEmpty()) {
            Object[][] params = dropFields.stream().map(dropField -> new Object[]{gid, dropField}).toArray(Object[][]::new);
            dbOperator.batch("DELETE FROM kun_mt_dataset_field WHERE dataset_gid = ? and name = ?", params);
        }

        fields.stream().forEach(field -> {
            if (!survivorFields.contains(field.getName())) {
                // new field
                dbOperator.create("INSERT INTO kun_mt_dataset_field(dataset_gid, name, type, raw_type, is_primary_key, is_nullable) VALUES(?, ?, ?, ?, ?, ?)",
                        gid, field.getName(), field.getFieldType().getType().toString(), field.getFieldType().getRawType(), field.isPrimaryKey(), field.isNullable());
                return;
            }

            if (isFieldChanged(fieldInfos.get(field.getName()), field)) {
                // update field
                if (logger.isDebugEnabled()) {
                    logger.debug("Update field, oldField: {}, newField: {}", JSONUtils.toJsonString(fieldInfos.get(field.getName())), JSONUtils.toJsonString(field));
                }

                dbOperator.update("UPDATE kun_mt_dataset_field SET type = ?, raw_type = ?, is_primary_key = ?, is_nullable = ? WHERE dataset_gid = ? and name = ?",
                        field.getFieldType().getType().toString(), field.getFieldType().getRawType(), field.isPrimaryKey(), field.isNullable(), gid, field.getName());
            }
        });
    }

    @Override
    public long loadSchema(Dataset dataset) {
        if (dataset == null || dataset.getDataStore() == null) {
            return -1L;
        }

        long gid = gidGenerator.generate(dataset.getDataStore());
        if (!judgeDatasetExisted(gid)) {
            dbOperator.update("INSERT INTO kun_mt_dataset(gid, name, datasource_id, data_store, database_name, dsi, deleted) VALUES(?, ?, ?, CAST(? AS JSONB), ?, ?, ?)",
                    gid, dataset.getName(),
                    dataset.getDatasourceId(),
                    DataStoreJsonUtil.toJson(dataset.getDataStore()),
                    dataset.getDatabaseName(),
                    dataset.getDataStore().getDSI().toFullString(),
                    false
            );
        }

        loadSchema(gid, dataset.getFields());
        return gid;
    }

    @Override
    public void loadStatistics(Dataset dataset) {
        if (logger.isDebugEnabled()) {
            logger.debug("PostgresLoader loadStatistics, dataset: {}", JSONUtils.toJsonString(dataset));
        }

        TableStatistics tableStatistics = dataset.getTableStatistics();
        if (tableStatistics == null) {
            logger.warn("Dataset: {}, no statistics of table were extracted", dataset.getGid());
            return;
        }
        dbOperator.update("INSERT INTO kun_mt_dataset_stats(dataset_gid, row_count, stats_date, last_updated_time, total_byte_size) VALUES (?, ?, ?, ?, ?)",
                dataset.getGid(), tableStatistics.getRowCount(), tableStatistics.getStatDate(), tableStatistics.getLastUpdatedTime(), tableStatistics.getTotalByteSize());

        // key: fieldName, value: fieldId
        List<DatasetFieldStat> fieldStats = dataset.getFieldStats();
        if (CollectionUtils.isEmpty(fieldStats)) {
            logger.warn("Dataset: {}, no statistics of field were extracted", dataset.getGid());
            return;
        }

        Map<String, Long> fieldMap = buildFieldMap(dataset.getGid());
        for (DatasetFieldStat fieldStat : dataset.getFieldStats()) {
            if (!fieldMap.containsKey(fieldStat.getFieldName())) {
                logger.warn("Field: {} not found", dataset.getName() + "-" + fieldStat.getFieldName());
                continue;
            }

            dbOperator.update("INSERT INTO kun_mt_dataset_field_stats(field_id, distinct_count, nonnull_count, stats_date) VALUES(?, ?, ?, ?)",
                    fieldMap.get(fieldStat.getFieldName()), fieldStat.getDistinctCount(), fieldStat.getNonnullCount(), fieldStat.getStatDate());
        }
    }

    private Map<String, Long> buildFieldMap(Long gid) {
        Map<String, Long> fieldMap = Maps.newHashMap();
        String fieldSQL = "SELECT id, name FROM kun_mt_dataset_field WHERE dataset_gid = ?";
        dbOperator.fetchAll(fieldSQL, rs -> {
            Long id = rs.getLong(1);
            String name = rs.getString(2);
            fieldMap.put(name, id);
            return null;
        }, gid);
        return fieldMap;
    }

    private boolean judgeDatasetExisted(long gid) {
        Long c = dbOperator.fetchOne("SELECT COUNT(*) FROM kun_mt_dataset WHERE gid = ?", rs -> rs.getLong(1), gid);
        return (c != null && c != 0);
    }

    private void fill(List<DatasetField> fields, Map<String, DatasetFieldInformation> fieldInfos, List<String> dropFields,
                      List<String> survivorFields, long gid) {
        List<String> extractFields = fields.stream().map(DatasetField::getName).collect(Collectors.toList());

        dbOperator.fetchAll("SELECT id, name, type, description, raw_type, is_primary_key, is_nullable FROM kun_mt_dataset_field WHERE dataset_gid = ?", rs -> {
            long id = rs.getLong("id");
            String name = rs.getString("name");
            String type = rs.getString("type");
            String rawType = rs.getString("raw_type");
            boolean isPrimaryKey = rs.getBoolean("is_primary_key");
            boolean isNullable = rs.getBoolean("is_nullable");

            if (!extractFields.contains(name)) {
                dropFields.add(name);
            } else {
                survivorFields.add(name);
            }

            DatasetFieldInformation fieldPO = new DatasetFieldInformation(id, name, type, rawType, isPrimaryKey, isNullable);
            fieldInfos.put(name, fieldPO);
            return null;
        }, gid);
    }

    private boolean isFieldChanged(DatasetFieldInformation oldField, DatasetField newField) {
        return !oldField.getRawType().equals(newField.getFieldType().getRawType()) ||
                oldField.isPrimaryKey() != newField.isPrimaryKey() ||
                oldField.isNullable() != newField.isNullable();
    }

}