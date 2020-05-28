package com.miotech.kun.metadata.extract.template;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.Iterators;
import com.google.gson.Gson;
import com.miotech.kun.metadata.extract.Extractor;
import com.miotech.kun.metadata.model.*;
import com.miotech.kun.metadata.service.gid.DataStoreJsonUtil;
import com.miotech.kun.workflow.core.model.lineage.DataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

public abstract class ExtractorTemplate implements Extractor {
    private static final Logger logger = LoggerFactory.getLogger(ExtractorTemplate.class);

    protected final Cluster cluster;
    private static final Gson gson = new Gson();

    public ExtractorTemplate(Cluster cluster) {
        this.cluster = cluster;
    }

    protected abstract List<DatasetField> getSchema();
    protected abstract DatasetFieldStat getFieldStats(DatasetField datasetField);
    protected abstract DatasetStat getTableStats();
    protected abstract DataStore getDataStore();
    protected abstract String getName();
    protected long getClusterId() {
        return cluster.getClusterId();
    };

    @Override
    public Iterator<Dataset> extract() {
        Dataset.Builder datasetBuilder = Dataset.newBuilder();

        try {
            List<DatasetField> schema = getSchema();
            logger.debug("extract schema: {}", gson.toJson(schema));
            List<DatasetFieldStat> fieldStats = Lists.newArrayList();

            if (schema != null) {
                for (DatasetField datasetField : schema) {
                    DatasetFieldStat fieldStat = getFieldStats(datasetField);
                    if (fieldStat != null) {
                        fieldStats.add(fieldStat);
                    }
                }
            }

            DatasetStat tableStats = getTableStats();
            logger.debug("extract tableStats: {}", gson.toJson(tableStats));

            DataStore dataStore = getDataStore();
            logger.debug("extract dataStore: {}", DataStoreJsonUtil.toJson(dataStore));
            datasetBuilder.withName(getName())
                    .withClusterId(getClusterId())
                    .withFields(schema)
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
