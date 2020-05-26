package com.miotech.kun.metadata.extract.factory;

import com.google.common.collect.Iterators;
import com.google.gson.Gson;
import com.miotech.kun.metadata.extract.Extractor;
import com.miotech.kun.metadata.model.Dataset;
import com.miotech.kun.metadata.model.DatasetField;
import com.miotech.kun.metadata.model.DatasetFieldStat;
import com.miotech.kun.metadata.model.DatasetStat;
import com.miotech.kun.metadata.service.gid.DataStoreJsonUtil;
import com.miotech.kun.workflow.core.model.lineage.DataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class ExtractorTemplate implements Extractor {
    private static final Logger logger = LoggerFactory.getLogger(ExtractorTemplate.class);

    protected abstract List<DatasetField> getSchema();
    protected abstract DatasetFieldStat getFieldStats(DatasetField datasetField);
    protected abstract DatasetStat getTableStats();
    protected abstract DataStore getDataStore();

    @Override
    public Iterator<Dataset> extract() {
        Dataset.Builder datasetBuilder = Dataset.newBuilder();

        try {
            List<DatasetField> schema = getSchema();
            logger.info("extract schema: {}", new Gson().toJson(schema));
            List<DatasetFieldStat> fieldStats = new ArrayList<>();
            for (DatasetField datasetField : schema) {
                fieldStats.add(getFieldStats(datasetField));
            }

            DatasetStat tableStats = getTableStats();
            logger.info("extract tableStats: {}", new Gson().toJson(tableStats));

            DataStore dataStore = getDataStore();
            logger.info("extract dataStore: {}", DataStoreJsonUtil.toJson(dataStore));
            datasetBuilder.withFields(schema)
                    .withFieldStats(fieldStats)
                    .withDatasetStat(tableStats)
                    .withDataStore(dataStore);

            return Iterators.forArray(datasetBuilder.build());
        } catch (Exception e) {
            logger.error("extract error:", e);
            throw new RuntimeException(e);
        }

    }
}
