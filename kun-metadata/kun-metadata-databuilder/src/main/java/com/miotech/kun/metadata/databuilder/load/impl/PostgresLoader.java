package com.miotech.kun.metadata.databuilder.load.impl;

import com.beust.jcommander.internal.Lists;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.miotech.kun.commons.db.DatabaseOperator;
import com.miotech.kun.commons.utils.ExceptionUtils;
import com.miotech.kun.metadata.common.utils.DataStoreJsonUtil;
import com.miotech.kun.metadata.core.model.Dataset;
import com.miotech.kun.metadata.core.model.DatasetField;
import com.miotech.kun.metadata.core.model.DatasetFieldStat;
import com.miotech.kun.metadata.core.model.DatasetStat;
import com.miotech.kun.metadata.databuilder.load.Loader;
import com.miotech.kun.metadata.databuilder.model.DatasetFieldPO;
import com.miotech.kun.metadata.databuilder.service.gid.GidService;
import com.miotech.kun.workflow.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class PostgresLoader implements Loader {
    private static Logger logger = LoggerFactory.getLogger(PostgresLoader.class);

    private final DatabaseOperator dbOperator;
    private final GidService gidGenerator;

    @Inject
    public PostgresLoader(DatabaseOperator dbOperator) {
        this.dbOperator = dbOperator;
        this.gidGenerator = new GidService(dbOperator);
    }

    @Override
    public void load(Dataset dataset) {
        if (logger.isDebugEnabled()) {
            logger.debug("PostgresLoader load start. dataset: {}", JSONUtils.toJsonString(dataset));
        }

        if (dataset == null || dataset.getDataStore() == null) {
            return;
        }

        long gid = gidGenerator.generate(dataset.getDataStore());
        boolean datasetExist = judgeDatasetExisted(gid);

        dbOperator.transaction(() -> {
            try {
                if (datasetExist) {
                    dbOperator.update("UPDATE kun_mt_dataset SET data_store = CAST(? AS JSONB), name = ?, database_name = ? WHERE gid = ?",
                            DataStoreJsonUtil.toJson(dataset.getDataStore()), dataset.getName(), dataset.getDatabaseName(), gid);
                } else {
                    dbOperator.update("INSERT INTO kun_mt_dataset(gid, name, datasource_id, data_store, database_name) VALUES(?, ?, ?, CAST(? AS JSONB), ?)",
                            gid, dataset.getName(), dataset.getDatasourceId(), DataStoreJsonUtil.toJson(dataset.getDataStore()), dataset.getDatabaseName());
                }

                DatasetStat datasetStat = dataset.getDatasetStat();
                if (datasetStat != null) {
                    dbOperator.update("INSERT INTO kun_mt_dataset_stats(dataset_gid, row_count, stats_date) VALUES (?, ?, ?)",
                            gid, datasetStat.getRowCount(), datasetStat.getStatDate());
                }

                Map<String, DatasetFieldPO> fieldInfos = new HashMap<>();
                List<String> deletedFields = Lists.newArrayList();
                List<String> survivorFields = Lists.newArrayList();
                fill(dataset.getFields(), fieldInfos, deletedFields, survivorFields, gid);

                if (!deletedFields.isEmpty()) {
                    Object[][] params = deletedFields.stream().map(deletedField -> new Object[]{gid, deletedField}).toArray(Object[][]::new);
                    dbOperator.batch("DELETE FROM kun_mt_dataset_field WHERE dataset_gid = ? and name = ?", params);
                }

                Map<String, DatasetFieldStat> fieldStatMap = new HashMap<>();
                for (DatasetFieldStat fieldStat : dataset.getFieldStats()) {
                    fieldStatMap.put(fieldStat.getName(), fieldStat);
                }

                for (DatasetField datasetField : dataset.getFields()) {
                    long id;
                    if (survivorFields.contains(datasetField.getName())) {
                        if (!fieldInfos.get(datasetField.getName()).getType().equals(datasetField.getFieldType().getRawType())) {
                            // update field type
                            dbOperator.update("UPDATE kun_mt_dataset_field SET type = ?, raw_type = ? WHERE dataset_gid = ? and name = ?",
                                    datasetField.getFieldType().getType().toString(), datasetField.getFieldType().getRawType(), gid, datasetField.getName());
                        }
                        id = fieldInfos.get(datasetField.getName()).getId();
                    } else {
                        // new field
                        id = dbOperator.create("INSERT INTO kun_mt_dataset_field(dataset_gid, name, type, raw_type) VALUES(?, ?, ?, ?)",
                                gid, datasetField.getName(), datasetField.getFieldType().getType().toString(), datasetField.getFieldType().getRawType());
                    }

                    DatasetFieldStat datasetFieldStat = fieldStatMap.get(datasetField.getName());
                    if (datasetFieldStat != null) {
                        dbOperator.update("INSERT INTO kun_mt_dataset_field_stats(field_id, distinct_count, nonnull_count, stats_date) VALUES(?, ?, ?, ?)",
                                id, datasetFieldStat.getDistinctCount(), datasetFieldStat.getNonnullCount(), datasetFieldStat.getStatDate());
                    }
                }
            } catch (JsonProcessingException jsonProcessingException) {
                throw ExceptionUtils.wrapIfChecked(jsonProcessingException);
            }
            return null;
        });
        if (logger.isDebugEnabled()) {
            logger.debug("PostgresLoader load end. dataset: {}", JSONUtils.toJsonString(dataset));
        }
    }

    private boolean judgeDatasetExisted(long gid) {
        Long c = dbOperator.fetchOne("SELECT COUNT(*) FROM kun_mt_dataset WHERE gid = ?", rs -> rs.getLong(1), gid);
        return (c != null && c != 0);
    }

    private void fill(List<DatasetField> fields, Map<String, DatasetFieldPO> fieldInfos, List<String> deletedFields,
                      List<String> survivorFields, long gid) {
        List<String> extractFields = fields.stream().map(DatasetField::getName).collect(Collectors.toList());

        dbOperator.fetchAll("SELECT id, name, type FROM kun_mt_dataset_field WHERE dataset_gid = ?", rs -> {
            long id = rs.getLong(1);
            String fieldName = rs.getString(2);
            String fieldType = rs.getString(3);
            survivorFields.add(fieldName);
            deletedFields.add(fieldName);

            DatasetFieldPO fieldPO = new DatasetFieldPO(id, fieldName, fieldType);
            fieldInfos.put(fieldName, fieldPO);
            return null;
        }, gid);
        deletedFields.removeAll(extractFields);
        survivorFields.retainAll(extractFields);

    }

}
