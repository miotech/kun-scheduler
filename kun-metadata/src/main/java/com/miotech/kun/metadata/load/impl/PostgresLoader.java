package com.miotech.kun.metadata.load.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.miotech.kun.metadata.load.Loader;
import com.miotech.kun.metadata.model.Dataset;
import com.miotech.kun.metadata.model.DatasetField;
import com.miotech.kun.metadata.model.DatasetFieldPO;
import com.miotech.kun.metadata.model.DatasetFieldStat;
import com.miotech.kun.metadata.service.gid.DataStoreJsonUtil;
import com.miotech.kun.metadata.service.gid.GidService;
import com.miotech.kun.workflow.db.DatabaseOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        long gid = gidGenerator.generate(dataset.getDataStore());
        boolean datasetExist = judgeDatasetExisted(gid);

        dbOperator.transaction(() -> {
            try {
                if (datasetExist) {
                    dbOperator.update("UPDATE kun_mt_dataset SET data_store = ?, `name` = ? WHERE gid = ?", DataStoreJsonUtil.toJson(dataset.getDataStore()), dataset.getName(), gid);
                } else {
                    dbOperator.update("INSERT INTO kun_mt_dataset(gid, `name`, cluster_id, data_store) VALUES(?, ?, ?, ?)", gid, dataset.getName(), dataset.getClusterId(), DataStoreJsonUtil.toJson(dataset.getDataStore()));
                }

                dbOperator.update("INSERT INTO kun_mt_dataset_stats(dataset_gid, `row_count`, stats_date) VALUES (?, ?, ?)", gid, dataset.getDatasetStat().getRowCount(), dataset.getDatasetStat().getStatDate());

                Map<String, DatasetFieldPO> fieldInfos = new HashMap<>();
                List<String> deletedFields = new ArrayList<>();
                List<String> survivorFields = new ArrayList<>();
                fill(dataset.getFields(), fieldInfos, deletedFields, survivorFields, gid);

                for (String deletedField : deletedFields) {
                    dbOperator.update("DELETE FROM kun_mt_dataset_field WHERE dataset_gid = ? and `name` = ?", gid, deletedField);
                }

                Map<String, DatasetFieldStat> fieldStatMap = new HashMap<>();
                for (DatasetFieldStat fieldStat : dataset.getFieldStats()) {
                    fieldStatMap.put(fieldStat.getName(), fieldStat);
                }

                for (DatasetField datasetField : dataset.getFields()) {
                    long id;
                    if (survivorFields.contains(datasetField.getName())) {
                        if (!fieldInfos.get(datasetField.getName()).getType().equals(datasetField.getType())) {
                            // update field type
                            dbOperator.update("UPDATE kun_mt_dataset_field SET `type` = ? WHERE dataset_gid = ? and `name` = ?", datasetField.getType(), gid, datasetField.getName());
                        }
                        id = fieldInfos.get(datasetField.getName()).getId();
                    } else {
                        // new field
                        id = dbOperator.create("INSERT INTO kun_mt_dataset_field(dataset_gid, `name`, `type`) VALUES(?, ?, ?)", gid, datasetField.getName(), datasetField.getType());
                    }

                    DatasetFieldStat datasetFieldStat = fieldStatMap.get(datasetField.getName());
                    if (datasetFieldStat != null) {
                        BigDecimal nonnullPercentage = new BigDecimal("100.00");
                        if (dataset.getDatasetStat().getRowCount() > 0) {
                            if (datasetFieldStat.getNonnullCount() > dataset.getDatasetStat().getRowCount()) {
                                logger.error("compute nonnullPercentage fail, nonnullCount > rowCount, nonnullCount: {}, rowCount: {}", datasetFieldStat.getNonnullCount(), dataset.getDatasetStat().getRowCount());
                                throw new RuntimeException("nonnullCount > rowCount");
                            }
                            nonnullPercentage = new BigDecimal(datasetFieldStat.getNonnullCount()).multiply(new BigDecimal("100")).divide(new BigDecimal(dataset.getDatasetStat().getRowCount()), 2, BigDecimal.ROUND_HALF_UP);
                        }
                        dbOperator.update("INSERT INTO kun_mt_dataset_field_stats(field_id, distinct_count, nonnull_count, nonnull_percentage) VALUES(?, ?, ?, ?)", id, datasetFieldStat.getDistinctCount(), datasetFieldStat.getNonnullCount(), nonnullPercentage);
                    }
                }
            } catch (JsonProcessingException jsonProcessingException) {
                throw new RuntimeException(jsonProcessingException);
            }
            return null;
        });

    }

    private boolean judgeDatasetExisted(long gid) {
        Long c = dbOperator.fetchOne("SELECT COUNT(*) FROM kun_mt_dataset WHERE gid = ?", rs -> rs.getLong(1), gid);
        return (c != null && c != 0);
    }

    private void fill(List<DatasetField> fields, Map<String, DatasetFieldPO> fieldInfos, List<String> deletedFields,
                      List<String> survivorFields, long gid) {
        List<String> extractFields = fields.stream().map(field -> field.getName()).collect(Collectors.toList());

        //TODO bug(use fetchAll) get all field name
        dbOperator.fetchAll("SELECT id, `name`, `type` FROM kun_mt_dataset_field WHERE dataset_gid = ?", (rs) -> {
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


        /*List<DatasetFieldPO> fieldPOS = dbOperator.fetchAll("SELECT id, `name`, `type` FROM kun_mt_dataset_field WHERE dataset_gid = ?", (rs) -> {
            long id = rs.getLong(1);
            String fieldName = rs.getString(2);
            String fieldType = rs.getString(3);
            survivorFields.add(fieldName);

            return new DatasetFieldPO(id, fieldName, fieldType);
        }, gid);
        deletedFields.removeAll(extractFields);
        survivorFields.retainAll(extractFields);*/

    }

}
