package com.miotech.kun.metadata.databuilder.load.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.utils.DateTimeUtils;
import com.miotech.kun.metadata.common.service.gid.GidService;
import com.miotech.kun.metadata.common.utils.DataStoreJsonUtil;
import com.miotech.kun.metadata.core.model.dataset.*;
import com.miotech.kun.metadata.databuilder.constant.DatasetLifecycleStatus;
import com.miotech.kun.metadata.databuilder.load.Loader;
import com.miotech.kun.metadata.databuilder.model.DatasetFieldInformation;
import com.miotech.kun.metadata.databuilder.model.DatasetLifecycleSnapshot;
import com.miotech.kun.metadata.databuilder.model.LoadSchemaResult;
import com.miotech.kun.metadata.databuilder.utils.JSONUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
    public LoadSchemaResult loadSchema(Long gid, List<DatasetField> fields) {
        if (logger.isDebugEnabled()) {
            logger.debug("PostgresLoader loadSchema, gid: {}, fields: {}", gid, JSONUtils.toJsonString(fields));
        }

        writeSchemaWithOldMode(gid, fields);
        return writeSchemaWithSnapshot(gid, fields);
    }

    @Override
    public LoadSchemaResult loadSchema(Dataset dataset) {
        if (dataset == null || dataset.getDataStore() == null) {
            return new LoadSchemaResult(-1L, -1L);
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

            SchemaSnapshot schemaSnapshot = SchemaSnapshot.newBuilder().withFields(dataset.getFields().stream().map(field -> field.convert()).collect(Collectors.toList())).build();
            dbOperator.update("INSERT INTO kun_mt_dataset_lifecycle(dataset_gid, fields, status, create_at) VALUES(?, CAST(? AS JSONB), ?, ?)",
                    gid, JSONUtils.toJsonString(schemaSnapshot), DatasetLifecycleStatus.MANAGED.name(), DateTimeUtils.now());
        }

        return loadSchema(gid, dataset.getFields());
    }

    @Override
    public void loadStatistics(Long snapshotId, Dataset dataset) {
        if (logger.isDebugEnabled()) {
            logger.debug("PostgresLoader loadStatistics, dataset: {}", JSONUtils.toJsonString(dataset));
        }

        writeStatisticsWithOldMode(dataset);
        writeStatisticsWithSnapshot(snapshotId, dataset);

    }

    private void writeStatisticsWithSnapshot(Long snapshotId, Dataset dataset) {
        List<FieldStatistics> fieldStatistics = null;
        if (CollectionUtils.isNotEmpty(dataset.getFieldStatistics())) {
            fieldStatistics = dataset.getFieldStatistics().stream().map(field -> field.cloneBuilder().withStatDate(null).build()).collect(Collectors.toList());
        }

        StatisticsSnapshot statisticsSnapshot = new StatisticsSnapshot(dataset.getTableStatistics().cloneBuilder().withStatDate(null).build(), fieldStatistics);
        dbOperator.update("UPDATE kun_mt_dataset_snapshot SET statistics_snapshot = CAST(? AS JSONB), statistics_at = ? WHERE id = ?",
                JSONUtils.toJsonString(statisticsSnapshot), DateTimeUtils.now(), snapshotId);
    }

    private void writeStatisticsWithOldMode(Dataset dataset) {
        TableStatistics tableStatistics = dataset.getTableStatistics();
        if (tableStatistics == null) {
            logger.warn("Dataset: {}, no statistics of table were extracted", dataset.getGid());
            return;
        }
        dbOperator.update("INSERT INTO kun_mt_dataset_stats(dataset_gid, row_count, stats_date, last_updated_time, total_byte_size) VALUES (?, ?, ?, ?, ?)",
                dataset.getGid(), tableStatistics.getRowCount(), tableStatistics.getStatDate(), tableStatistics.getLastUpdatedTime(), tableStatistics.getTotalByteSize());

        List<FieldStatistics> fieldStats = dataset.getFieldStatistics();
        if (CollectionUtils.isEmpty(fieldStats)) {
            logger.warn("Dataset: {}, no statistics of field were extracted", dataset.getGid());
            return;
        }

        Map<String, Long> fieldMap = buildFieldMap(dataset.getGid());
        for (FieldStatistics fieldStat : dataset.getFieldStatistics()) {
            if (!fieldMap.containsKey(fieldStat.getFieldName())) {
                logger.warn("Field: {} not found", dataset.getName() + "-" + fieldStat.getFieldName());
                continue;
            }

            dbOperator.update("INSERT INTO kun_mt_dataset_field_stats(field_id, distinct_count, nonnull_count, stats_date) VALUES(?, ?, ?, ?)",
                    fieldMap.get(fieldStat.getFieldName()), fieldStat.getDistinctCount(), fieldStat.getNonnullCount(), fieldStat.getStatDate());
        }
    }

    private LoadSchemaResult writeSchemaWithSnapshot(Long gid, List<DatasetField> fields) {
        List<DatasetField> latestFields = searchLatestFields(gid);
        DatasetLifecycleSnapshot datasetLifecycleSnapshot = computeDifferent(fields, latestFields);

        SchemaSnapshot schemaSnapshot = SchemaSnapshot.newBuilder().withFields(fields.stream().map(field -> field.convert()).collect(Collectors.toList())).build();
        long snapshotId = dbOperator.create("INSERT INTO kun_mt_dataset_snapshot(dataset_gid, schema_snapshot, schema_at) VALUES(?, CAST(? AS JSONB), ?)",
                gid, JSONUtils.toJsonString(schemaSnapshot), DateTimeUtils.now());

        if (datasetLifecycleSnapshot.isChanged()) {
            dbOperator.update("INSERT INTO kun_mt_dataset_lifecycle(dataset_gid, changed, fields, status, create_at) VALUES(?, CAST(? AS JSONB), CAST(? AS JSONB), ?, ?)",
                    gid, JSONUtils.toJsonString(datasetLifecycleSnapshot), JSONUtils.toJsonString(schemaSnapshot), DatasetLifecycleStatus.CHANGED.name(), DateTimeUtils.now());
        }

        return new LoadSchemaResult(gid, snapshotId);
    }

    private void writeSchemaWithOldMode(Long gid, List<DatasetField> fields) {
        Map<String, DatasetFieldInformation> fieldInfos = Maps.newHashMap();
        List<String> survivorFields = Lists.newArrayList();
        List<DatasetLifecycleSnapshot.Column> dropFields = Lists.newArrayList();
        fill(fields, fieldInfos, dropFields, survivorFields, gid);

        if (!dropFields.isEmpty()) {
            Object[][] params = dropFields.stream().map(deletedField -> new Object[]{gid, deletedField.getName()}).toArray(Object[][]::new);
            dbOperator.batch("DELETE FROM kun_mt_dataset_field WHERE dataset_gid = ? and name = ?", params);
        }

        for (DatasetField field : fields) {
            if (!survivorFields.contains(field.getName())) {
                // new field
                dbOperator.create("INSERT INTO kun_mt_dataset_field(dataset_gid, name, type, raw_type, is_primary_key, is_nullable) VALUES(?, ?, ?, ?, ?, ?)",
                        gid, field.getName(), field.getFieldType().getType().toString(), field.getFieldType().getRawType(), field.getIsPrimaryKey(), field.getIsNullable());
                continue;
            }

            if (isFieldChanged(fieldInfos.get(field.getName()), field)) {
                // update field
                if (logger.isDebugEnabled()) {
                    logger.debug("Update field, oldField: {}, newField: {}", JSONUtils.toJsonString(fieldInfos.get(field.getName())), JSONUtils.toJsonString(field));
                }

                dbOperator.update("UPDATE kun_mt_dataset_field SET type = ?, raw_type = ?, is_primary_key = ?, is_nullable = ? WHERE dataset_gid = ? and name = ?",
                        field.getFieldType().getType().toString(), field.getFieldType().getRawType(), field.getIsPrimaryKey(), field.getIsNullable(), gid, field.getName());
            }
        }
    }

    private DatasetLifecycleSnapshot computeDifferent(List<DatasetField> fields, List<DatasetField> latestFields) {
        logger.info("fields: {}, latestFields: {}", JSONUtils.toJsonString(fields), JSONUtils.toJsonString(latestFields));
        DatasetLifecycleSnapshot.Builder snapshotBuilder = DatasetLifecycleSnapshot.newBuilder();

        Map<String, DatasetField> latestFieldsMap = latestFields.stream().collect(Collectors.toMap(DatasetField::getName, Function.identity()));
        Map<String, DatasetField> fieldsMap = fields.stream().collect(Collectors.toMap(DatasetField::getName, Function.identity()));
        List<DatasetLifecycleSnapshot.Column> addColumns = Lists.newArrayList();
        List<DatasetLifecycleSnapshot.Column> dropColumns = Lists.newArrayList();
        List<DatasetLifecycleSnapshot.ColumnChanged> modifyFields = Lists.newArrayList();
        for (DatasetField latestField : latestFields) {
            if (!fieldsMap.containsKey(latestField.getName())) {
                dropColumns.add(new DatasetLifecycleSnapshot.Column(latestField.getName(), latestField.getFieldType().getType().name(), latestField.getComment(), latestField.getFieldType().getRawType()));
            } else {
                DatasetField datasetField = fieldsMap.get(latestField.getName());
                if (isFieldChanged(latestField, datasetField)) {
                    modifyFields.add(new DatasetLifecycleSnapshot.ColumnChanged(datasetField.getName(), datasetField.getFieldType().getType().name(),
                            datasetField.getFieldType().getRawType(), datasetField.getIsPrimaryKey(), datasetField.getIsNullable(), latestField.getFieldType().getType().name(), latestField.getFieldType().getRawType(), latestField.getIsPrimaryKey(), latestField.getIsNullable()));
                }
            }
        }

        for (DatasetField field : fields) {
            if (!latestFieldsMap.containsKey(field.getName())) {
                addColumns.add(new DatasetLifecycleSnapshot.Column(field.getName(), field.getFieldType().getType().name(), field.getComment(), field.getFieldType().getRawType()));
            }
        }

        return snapshotBuilder.withAddColumns(addColumns)
                .withDropColumns(dropColumns)
                .withModifyColumns(modifyFields)
                .build();
    }

    private List<DatasetField> searchLatestFields(Long gid) {
        List<DatasetField> fields = dbOperator.fetchOne("SELECT schema_snapshot FROM kun_mt_dataset_snapshot WHERE dataset_gid = ? ORDER BY schema_at DESC LIMIT 1", rs ->
                JSONUtils.jsonToObject(rs.getString("schema_snapshot"), SchemaSnapshot.class).getFields().stream()
                        .map(field -> field.convert()).collect(Collectors.toList()), gid);

        if (CollectionUtils.isNotEmpty(fields)) {
            return fields;
        }

        return dbOperator.fetchAll("SELECT name, type, description, raw_type, is_primary_key, is_nullable FROM kun_mt_dataset_field WHERE dataset_gid = ?", rs ->
            DatasetField.newBuilder()
                    .withName(rs.getString("name"))
                    .withFieldType(new DatasetFieldType(DatasetFieldType.Type.valueOf(rs.getString("type")), rs.getString("raw_type")))
                    .withComment(rs.getString("description"))
                    .withIsPrimaryKey(rs.getBoolean("is_primary_key"))
                    .withIsNullable(rs.getBoolean("is_nullable"))
                    .build()
                , gid);
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

    private void fill(List<DatasetField> fields, Map<String, DatasetFieldInformation> fieldInfos, List<DatasetLifecycleSnapshot.Column> dropFields,
                      List<String> survivorFields, long gid) {
        List<String> extractFields = fields.stream().map(DatasetField::getName).collect(Collectors.toList());

        dbOperator.fetchAll("SELECT id, name, type, description, raw_type, is_primary_key, is_nullable FROM kun_mt_dataset_field WHERE dataset_gid = ?", rs -> {
            long id = rs.getLong("id");
            String name = rs.getString("name");
            String type = rs.getString("type");
            String description = rs.getString("description");
            String rawType = rs.getString("raw_type");
            boolean isPrimaryKey = rs.getBoolean("is_primary_key");
            boolean isNullable = rs.getBoolean("is_nullable");

            if (!extractFields.contains(name)) {
                dropFields.add(new DatasetLifecycleSnapshot.Column(name, type, description, rawType));
            } else {
                survivorFields.add(name);
            }

            DatasetFieldInformation fieldPO = new DatasetFieldInformation(id, name, type, rawType, isPrimaryKey, isNullable);
            fieldInfos.put(name, fieldPO);
            return null;
        }, gid);
    }

    private boolean isFieldChanged(DatasetField oldField, DatasetField newField) {
        return !oldField.getFieldType().getRawType().equals(newField.getFieldType().getRawType()) ||
                oldField.getIsPrimaryKey() != newField.getIsPrimaryKey() ||
                oldField.getIsNullable() != newField.getIsNullable();
    }

    private boolean isFieldChanged(DatasetFieldInformation oldField, DatasetField newField) {
        return !oldField.getRawType().equals(newField.getFieldType().getRawType()) ||
                oldField.isPrimaryKey() != newField.getIsPrimaryKey() ||
                oldField.isNullable() != newField.getIsNullable();
    }

}