package com.miotech.kun.metadata.databuilder.load.impl;

import com.beust.jcommander.internal.Lists;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Maps;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.common.utils.DataStoreJsonUtil;
import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.core.model.DatasetField;
import com.miotech.kun.metadata.core.model.DatasetFieldStat;
import com.miotech.kun.metadata.core.model.DatasetStat;
import com.miotech.kun.metadata.databuilder.constant.DatasetLifecycleStatus;
import com.miotech.kun.metadata.databuilder.load.Loader;
import com.miotech.kun.metadata.databuilder.model.DatasetFieldPO;
import com.miotech.kun.metadata.databuilder.model.DatasetLifecycleSnapshot;
import com.miotech.kun.metadata.databuilder.service.gid.GidService;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDateTime;
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
        Map<String, DatasetFieldPO> fieldInfos = Maps.newHashMap();
        List<String> survivorFields = Lists.newArrayList();
        List<DatasetLifecycleSnapshot.Column> dropFields = Lists.newArrayList();
        fill(fields, fieldInfos, survivorFields, gid, dropFields);

        boolean schemaChanged = false;
        if (!dropFields.isEmpty()) {
            Object[][] params = dropFields.stream().map(deletedField -> new Object[]{gid, deletedField.getName()}).toArray(Object[][]::new);
            dbOperator.batch("DELETE FROM kun_mt_dataset_field WHERE dataset_gid = ? and name = ?", params);

            schemaChanged = true;
        }

        List<DatasetLifecycleSnapshot.Column> addFields = Lists.newArrayList();
        List<DatasetLifecycleSnapshot.ColumnChanged> modifyFields = Lists.newArrayList();
        for (DatasetField field : fields) {
            if (survivorFields.contains(field.getName())) {
                if (!fieldInfos.get(field.getName()).getRawType().equals(field.getFieldType().getRawType())) {
                    // update field type
                    logger.info("Update field type, oldType: {}, newType: {}", fieldInfos.get(field.getName()).getType(), field.getFieldType().getType());
                    dbOperator.update("UPDATE kun_mt_dataset_field SET type = ?, raw_type = ? WHERE dataset_gid = ? and name = ?",
                            field.getFieldType().getType().toString(), field.getFieldType().getRawType(), gid, field.getName());

                    modifyFields.add(new DatasetLifecycleSnapshot.ColumnChanged(field.getName(), field.getFieldType().getType().name(),
                            field.getFieldType().getRawType(), fieldInfos.get(field.getName()).getType(), fieldInfos.get(field.getName()).getRawType()));
                    schemaChanged = true;
                }
            } else {
                // new field
                dbOperator.create("INSERT INTO kun_mt_dataset_field(dataset_gid, name, type, raw_type) VALUES(?, ?, ?, ?)",
                        gid, field.getName(), field.getFieldType().getType().toString(), field.getFieldType().getRawType());

                addFields.add(new DatasetLifecycleSnapshot.Column(field.getName(), field.getFieldType().getType().name(), field.getComment(), field.getFieldType().getRawType()));
                schemaChanged = true;
            }
        }

        DatasetLifecycleSnapshot datasetLifecycleSnapshot = DatasetLifecycleSnapshot.newBuilder()
                .withDropColumns(dropFields)
                .withAddColumns(addFields)
                .withModifyColumns(modifyFields)
                .build();

        if (schemaChanged) {
            dbOperator.update("INSERT INTO kun_mt_dataset_lifecycle(dataset_gid, changed, status, create_at) VALUES(?, CAST(? AS JSONB), ?, ?)",
                    gid, JSONUtils.toJsonString(datasetLifecycleSnapshot), DatasetLifecycleStatus.CHANGED.name(), LocalDateTime.now());
        }

    }

    @Override
    public void loadSchema(Dataset dataset) {
        if (dataset == null || dataset.getDataStore() == null) {
            return;
        }

        long gid = gidGenerator.generate(dataset.getDataStore());
        if (!judgeDatasetExisted(gid)) {
            try {
                dbOperator.update("INSERT INTO kun_mt_dataset(gid, name, datasource_id, data_store, database_name, dsi, deleted) VALUES(?, ?, ?, CAST(? AS JSONB), ?, ?, ?)",
                        gid, dataset.getName(),
                        dataset.getDatasourceId(),
                        DataStoreJsonUtil.toJson(dataset.getDataStore()),
                        dataset.getDatabaseName(),
                        dataset.getDataStore().getDSI().toFullString(),
                        false
                );
            } catch (JsonProcessingException e) {
                throw ExceptionUtils.wrapIfChecked(e);
            }
        }

        loadSchema(gid, dataset.getFields());
    }

    @Override
    public void loadStat(Dataset dataset) {
        DatasetStat datasetStat = dataset.getDatasetStat();
        if (datasetStat == null) {
            logger.warn("Dataset: {}, no statistics of table were extracted", dataset.getGid());
            return;
        }
        dbOperator.update("INSERT INTO kun_mt_dataset_stats(dataset_gid, row_count, stats_date, last_updated_time) VALUES (?, ?, ?, ?)",
                dataset.getGid(), datasetStat.getRowCount(), datasetStat.getStatDate(), datasetStat.getLastUpdatedTime());

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

    private void fill(List<DatasetField> fields, Map<String, DatasetFieldPO> fieldInfos,
                      List<String> survivorFields, long gid, List<DatasetLifecycleSnapshot.Column> deleted) {
        List<String> extractFields = fields.stream().map(DatasetField::getName).collect(Collectors.toList());

        dbOperator.fetchAll("SELECT id, name, type, description, raw_type FROM kun_mt_dataset_field WHERE dataset_gid = ? AND deleted is false", rs -> {
            long id = rs.getLong(1);
            String name = rs.getString(2);
            String type = rs.getString(3);
            String description = rs.getString(4);
            String rawType = rs.getString(5);

            if (!extractFields.contains(name)) {
                deleted.add(new DatasetLifecycleSnapshot.Column(name, type, description, rawType));
            } else {
                survivorFields.add(name);
            }

            DatasetFieldPO fieldPO = new DatasetFieldPO(id, name, type, rawType);
            fieldInfos.put(name, fieldPO);
            return null;
        }, gid);
    }

}